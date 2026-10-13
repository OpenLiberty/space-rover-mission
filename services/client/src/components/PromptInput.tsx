/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import React, { useState, useEffect } from "react";
import { CommandScript } from "hooks/usePromptControls";
import useScripts from "hooks/useScripts";

// UI label strings from specs/free-roam-spec.md
const EXECUTE_BUTTON_LABEL = "Execute";
const CANCEL_BUTTON_LABEL = "Stop";
const STATUS_EXECUTING = "Executing rover script...";
const STATUS_COMPLETED = "Rover script completed.";
const STATUS_CANCELLED = "Rover script cancelled.";
const WARNING_BOUNDARY =
  "Warning: this script may take the rover near the boundary.";

// bob-bridge sidecar — exposed on localhost:4000 (see services/docker-compose.yml)
const BOB_BRIDGE_BASE = "http://localhost:4000";
const BOB_BRIDGE_URL = `${BOB_BRIDGE_BASE}/generate`;

type Props = {
  gameSocketURL: string;
  isExecuting: boolean;
  nearBoundary: boolean;
  executeScript: (script: CommandScript) => void;
  cancelScript: () => void;
};

const PromptInput = ({
  isExecuting,
  nearBoundary,
  executeScript,
  cancelScript,
}: Props) => {
  const { scripts, loading: scriptsLoading, refetch: refetchScripts } = useScripts();
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [status, setStatus] = useState<string | null>(null);
  const [wasExecuting, setWasExecuting] = useState(false);

  // ── Bob generate panel state ──────────────────────────────────────────────
  const [bobPanelOpen, setBobPanelOpen] = useState(false);
  const [bobPrompt, setBobPrompt] = useState("");
  const [bobGenerating, setBobGenerating] = useState(false);
  const [bobError, setBobError] = useState<string | null>(null);
  const [generatedScript, setGeneratedScript] = useState<CommandScript & { name?: string; description?: string } | null>(null);

  useEffect(() => {
    if (isExecuting) {
      setWasExecuting(true);
    } else if (wasExecuting) {
      setWasExecuting(false);
      setStatus(STATUS_COMPLETED);
    }
  }, [isExecuting]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleExecute = () => {
    if (isExecuting || scripts.length === 0) {
      return;
    }
    setStatus(STATUS_EXECUTING);
    executeScript({ steps: scripts[selectedIndex].steps });
  };

  const handleStop = () => {
    cancelScript();
    setStatus(STATUS_CANCELLED);
  };

  // ── Bob generate handlers ─────────────────────────────────────────────────

  const handleBobGenerate = async () => {
    if (!bobPrompt.trim() || bobGenerating) return;
    setBobGenerating(true);
    setBobError(null);
    setGeneratedScript(null);

    try {
      const res = await fetch(BOB_BRIDGE_URL, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ prompt: bobPrompt.trim() }),
      });
      const data = await res.json();
      if (!res.ok) {
        setBobError(data.error || "Bob returned an error.");
      } else {
        setGeneratedScript(data);
        refetchScripts();
      }
    } catch {
      setBobError("Could not reach the Bob bridge. Is the bob-bridge container running?");
    } finally {
      setBobGenerating(false);
    }
  };

  const handleRunGenerated = () => {
    if (!generatedScript || isExecuting) return;
    setStatus(STATUS_EXECUTING);
    executeScript({ steps: generatedScript.steps });
  };

  return (
    <div className="flex flex-col gap-4 w-full">
      {/* ── Pre-authored script selector ────────────────────────────────── */}
      <label className="text-gray-300 text-sm" htmlFor="rover-script-select">
        Select a rover script:
      </label>
      <select
        id="rover-script-select"
        className="bg-gray-800 text-gray-100 rounded-lg p-3 w-full"
        value={selectedIndex}
        onChange={(e) => setSelectedIndex(Number(e.target.value))}
        disabled={isExecuting || scriptsLoading}
      >
        {scriptsLoading ? (
          <option>Loading scripts…</option>
        ) : (
          scripts.map((script, index) => (
            <option key={index} value={index}>
              {script.name}
            </option>
          ))
        )}
      </select>
      {!scriptsLoading && scripts.length > 0 && (
        <p className="text-gray-500 text-sm">
          {scripts[selectedIndex].description}
        </p>
      )}
      {nearBoundary && (
        <p className="text-yellow-400 text-sm">{WARNING_BOUNDARY}</p>
      )}
      <div className="flex flex-row gap-4">
        <button
          className="bg-blue-600 hover:bg-blue-500 disabled:opacity-50 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg"
          onClick={handleExecute}
          disabled={isExecuting || scripts.length === 0}
        >
          {EXECUTE_BUTTON_LABEL}
        </button>
        <button
          className="bg-red-600 hover:bg-red-500 text-white px-6 py-3 rounded-lg"
          onClick={handleStop}
        >
          {CANCEL_BUTTON_LABEL}
        </button>
      </div>
      {status && <p className="text-gray-300 text-sm">{status}</p>}

      {/* ── Generate with Bob panel ──────────────────────────────────────── */}
      <div className="border border-gray-700 rounded-lg overflow-hidden mt-2">
        <button
          className="w-full flex items-center justify-between px-4 py-3 bg-gray-800 hover:bg-gray-700 text-gray-200 text-sm"
          onClick={() => setBobPanelOpen((v) => !v)}
          aria-expanded={bobPanelOpen}
        >
          <span>Generate a new trick with Bob</span>
          <span className="text-gray-400">{bobPanelOpen ? "▲" : "▼"}</span>
        </button>

        {bobPanelOpen && (
          <div className="p-4 bg-gray-900 flex flex-col gap-3">
            <p className="text-gray-400 text-xs">
              Describe any trick in plain English. Bob will generate the
              command script and send it straight to the rover.
            </p>
            <textarea
              className="bg-gray-800 text-gray-100 rounded-lg p-3 w-full text-sm resize-none"
              rows={3}
              placeholder='e.g. "spin right twice then reverse 50 cm"'
              value={bobPrompt}
              onChange={(e) => setBobPrompt(e.target.value)}
              disabled={bobGenerating || isExecuting}
              onKeyDown={(e) => {
                if (e.key === "Enter" && (e.metaKey || e.ctrlKey)) {
                  handleBobGenerate();
                }
              }}
            />
            <div className="flex flex-row gap-3 items-center">
              <button
                className="bg-purple-600 hover:bg-purple-500 disabled:opacity-50 disabled:cursor-not-allowed text-white px-5 py-2 rounded-lg text-sm"
                onClick={handleBobGenerate}
                disabled={!bobPrompt.trim() || bobGenerating || isExecuting}
              >
                {bobGenerating ? "Generating…" : "Generate"}
              </button>
              {bobGenerating && (
                <span className="text-gray-400 text-xs animate-pulse">
                  Bob is writing your script…
                </span>
              )}
            </div>

            {bobError && (
              <p className="text-red-400 text-xs">{bobError}</p>
            )}

            {generatedScript && (
              <div className="flex flex-col gap-2 border border-gray-700 rounded-lg p-3 bg-gray-800">
                <p className="text-gray-200 text-sm font-semibold">
                  {generatedScript.name}
                </p>
                {generatedScript.description && (
                  <p className="text-gray-400 text-xs">
                    {generatedScript.description}
                  </p>
                )}
                <p className="text-gray-500 text-xs">
                  {generatedScript.steps.length} steps ·{" "}
                  {generatedScript.steps.reduce((t, s) => t + s.durationMs, 0)} ms total
                </p>
                <div className="flex flex-row gap-2">
                  <button
                    className="bg-blue-600 hover:bg-blue-500 disabled:opacity-50 disabled:cursor-not-allowed text-white px-5 py-2 rounded-lg text-sm"
                    onClick={handleRunGenerated}
                    disabled={isExecuting}
                  >
                    Run this trick
                  </button>
                  <button
                    className="bg-gray-600 hover:bg-gray-500 text-white px-5 py-2 rounded-lg text-sm"
                    onClick={() => { setGeneratedScript(null); setBobPrompt(""); }}
                    disabled={isExecuting}
                  >
                    Generate another
                  </button>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default PromptInput;
