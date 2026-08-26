---
name: rover-spec
description: Use when the user describes rover behaviour they want the rover to perform — guides Bob through reading the move vocabulary and calibration constants from the spec and writing a valid CommandScript JSON to specs/scripts/.
---

# Rover Script Authoring

Follow these steps in order when a user describes rover behaviour in chat.

## Step 1 — Read the move vocabulary and calibration constants

Use `read_file` to read `specs/free-roam-spec.md`.

Extract:
- **Move Vocabulary** table — the allowed semantic move names and their parameters
- **Calibration Constants** — `ms_per_degree`, `ms_per_cm`, `max_duration_ms`
- **Safety Rules** — `max_step_duration_ms`, `max_duration_ms`, `soft_boundary_radius_cm`

## Step 2 — Know the schema

The JSON schema you must produce is:
```json
{
  "name": "string",
  "description": "string",
  "steps": [
    { "command": "F" | "B" | "L" | "R" | "S", "durationMs": number }
  ]
}
```

The allowed commands are:
- `F` — move forward
- `B` — move backward
- `L` — spin left (rotate left in place)
- `R` — spin right (rotate right in place)
- `S` — stop (always use `durationMs: 0`)

Always end the steps array with `{ "command": "S", "durationMs": 0 }`.

## Step 3 — Translate the user's description

Convert the user's plain-English description into a valid `CommandScript` JSON using the calibration constants:
- Forward/backward distance: `durationMs = distance_cm * ms_per_cm` (cap at `max_step_duration_ms`)
- Turn by degrees: `durationMs = degrees * ms_per_degree` (cap at `max_step_duration_ms`)
- Spin for duration: use the requested `duration_ms` directly (cap at `max_step_duration_ms`)
- Total script duration (sum of all `durationMs` values) must not exceed `max_duration_ms`

Choose a short `name` in Title Case and write a one-sentence `description`.

## Step 4 — Write the script file

1. Convert the `name` to kebab-case (e.g. `"Square Patrol"` → `square-patrol`).
2. Use `write_file` to write the JSON to `specs/scripts/<kebab-case-name>.json`.
3. Use `write_file` to write the **same** JSON to `services/client/src/specs/scripts/<kebab-case-name>.json` (this is the client-side copy that can be statically imported).

## Step 5 — Register the script in the client registry

Use `read_file` to read `services/client/src/specs/scripts/index.ts`.

Then use `apply_diff` to:
1. Add an import line at the top of the imports block:
   ```ts
   import <camelCaseName> from "./<kebab-case-name>.json";
   ```
2. Add an entry to the `scripts` array:
   ```ts
   <camelCaseName> as ScriptEntry,
   ```

## Step 6 — Confirm

Tell the user:
- The file written to `specs/scripts/<kebab-case-name>.json`
- The client copy written to `services/client/src/specs/scripts/<kebab-case-name>.json`
- The entry added to `services/client/src/specs/scripts/index.ts`
- The script will appear in the Free Roam dropdown on the next client build
