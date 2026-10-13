/**
 * bob-bridge/server.js
 *
 * Runs Bob Shell directly inside this container to generate rover trick scripts,
 * and serves the script list to the browser at runtime — no docker rebuild needed.
 *
 * GET  /health    → { status: "ok" }
 * GET  /scripts   → ScriptEntry[]  (reads all *.json from SCRIPTS_DIR)
 * POST /generate  → { name, description, steps: [...] }
 *   Body: { "prompt": "do a figure 8" }
 *
 * Bob Shell must be installed in the container image (see Dockerfile).
 * BOBSHELL_API_KEY must be set for headless auth.
 * SCRIPTS_DIR must point to the mounted specs/scripts directory.
 */

"use strict";

const express = require("express");
const cors = require("cors");
const fs = require("fs");
const path = require("path");
const { spawn } = require("child_process");

const app = express();
app.use(express.json());
app.use(cors());

const PORT = process.env.BOB_BRIDGE_PORT || 4000;
const SCRIPTS_DIR = process.env.SCRIPTS_DIR || "/workspace/specs/scripts";
// Bob Shell must be run from the workspace root so it loads .bob/rules/ and skills
const WORKSPACE = process.env.WORKSPACE || "/workspace";

// ─── Health check ─────────────────────────────────────────────────────────────
app.get("/health", (_req, res) => res.json({ status: "ok" }));

// ─── GET /scripts ─────────────────────────────────────────────────────────────
// Reads all *.json files from SCRIPTS_DIR and returns them as an array.
app.get("/scripts", (_req, res) => {
  let files;
  try {
    files = fs.readdirSync(SCRIPTS_DIR).filter((f) => f.endsWith(".json"));
  } catch (err) {
    console.error("[bob-bridge] Could not read scripts dir:", err.message);
    return res.status(500).json({ error: "Could not read scripts directory" });
  }

  const scripts = [];
  for (const file of files) {
    try {
      const raw = fs.readFileSync(path.join(SCRIPTS_DIR, file), "utf8");
      const parsed = JSON.parse(raw);
      if (parsed.name && Array.isArray(parsed.steps)) {
        scripts.push(parsed);
      }
    } catch {
      // Skip files that fail to parse — don't let one bad file break the list
      console.warn(`[bob-bridge] Skipping unparseable script: ${file}`);
    }
  }

  return res.json(scripts);
});

// ─── POST /generate ───────────────────────────────────────────────────────────
// Runs Bob Shell with the given prompt, waits for it to finish, then finds and
// returns the newly written script JSON.
app.post("/generate", (req, res) => {
  const prompt = ((req.body && req.body.prompt) || "").trim();
  if (!prompt) {
    return res.status(400).json({ error: "prompt is required" });
  }

  console.log(`[bob-bridge] Generating script for: "${prompt}"`);

  // Snapshot existing JSON mtimes so we can identify the new file afterwards
  let preMtimes = {};
  try {
    const existing = fs.readdirSync(SCRIPTS_DIR).filter((f) => f.endsWith(".json"));
    for (const f of existing) {
      preMtimes[f] = fs.statSync(path.join(SCRIPTS_DIR, f)).mtimeMs;
    }
  } catch {
    preMtimes = {};
  }

  const bobPrompt =
    `You are operating under the rover-spec skill. ` +
    `Follow the rover-spec skill instructions exactly. ` +
    `Generate a rover trick script for this description: ${prompt}`;

  const bobProc = spawn("bob", ["-p", bobPrompt], {
    cwd: WORKSPACE,
    env: { ...process.env },
    stdio: ["ignore", "pipe", "pipe"],
  });

  // Stream Bob output to container logs for visibility
  bobProc.stdout.on("data", (d) => process.stdout.write(`[bob] ${d}`));
  bobProc.stderr.on("data", (d) => process.stderr.write(`[bob] ${d}`));

  // Bob Shell may take several minutes for script generation
  const timeout = setTimeout(() => {
    bobProc.kill();
    console.error("[bob-bridge] Bob Shell timed out");
    if (!res.headersSent) {
      res.status(504).json({ error: "Bob Shell timed out after 10 minutes" });
    }
  }, 600_000);

  bobProc.on("close", (code) => {
    clearTimeout(timeout);

    if (code !== 0) {
      console.error(`[bob-bridge] Bob Shell exited with code ${code}`);
      if (!res.headersSent) {
        res.status(500).json({ error: `Bob Shell exited with code ${code}` });
      }
      return;
    }

    // Find the file that is new or newer than the pre-run snapshot
    let newFile = null;
    try {
      const current = fs.readdirSync(SCRIPTS_DIR).filter((f) => f.endsWith(".json"));
      for (const f of current) {
        const mtime = fs.statSync(path.join(SCRIPTS_DIR, f)).mtimeMs;
        if (!(f in preMtimes) || mtime > preMtimes[f]) {
          newFile = f;
          break;
        }
      }
    } catch (err) {
      console.error("[bob-bridge] Could not scan scripts dir:", err.message);
    }

    if (!newFile) {
      console.error("[bob-bridge] Bob finished but no new script file was found");
      if (!res.headersSent) {
        res.status(500).json({ error: "Bob finished but no new script file was found" });
      }
      return;
    }

    let script;
    try {
      script = JSON.parse(fs.readFileSync(path.join(SCRIPTS_DIR, newFile), "utf8"));
    } catch (err) {
      console.error("[bob-bridge] Could not parse new script:", err.message);
      if (!res.headersSent) {
        res.status(500).json({ error: "Generated script could not be parsed" });
      }
      return;
    }

    if (!script.name || !Array.isArray(script.steps)) {
      if (!res.headersSent) {
        res.status(500).json({
          error: "Generated script is missing required fields (name, steps)",
          script,
        });
      }
      return;
    }

    console.log(`[bob-bridge] Generated "${script.name}" (${script.steps.length} steps)`);
    if (!res.headersSent) {
      res.json(script);
    }
  });
});

const server = app.listen(PORT, "0.0.0.0", () => {
  console.log(`[bob-bridge] Listening on http://0.0.0.0:${PORT}`);
  console.log(`[bob-bridge] Scripts dir: ${SCRIPTS_DIR}`);
  console.log(`[bob-bridge] Workspace: ${WORKSPACE}`);
});

// Graceful shutdown — exit cleanly on SIGTERM so podman stop -t 10 succeeds
process.on("SIGTERM", () => {
  server.close(() => process.exit(0));
});
