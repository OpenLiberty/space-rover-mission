# Rover Scripts

This folder holds Bob-authored rover scripts for Free Roam mode.

## JSON Schema

Each file is a `CommandScript` JSON with the following schema:

```json
{
  "name": "string",
  "description": "string",
  "steps": [
    { "command": "F" | "B" | "L" | "R" | "S", "durationMs": number }
  ]
}
```

| Command | Meaning |
| --- | --- |
| `F` | Move forward |
| `B` | Move backward |
| `L` | Spin left (rotate left in place) |
| `R` | Spin right (rotate right in place) |
| `S` | Stop |

Always end the `steps` array with `{ "command": "S", "durationMs": 0 }`.

## Calibration Constants (from `specs/free-roam-spec.md`)

| Constant | Value | Usage |
| --- | --- | --- |
| `ms_per_degree` | 8 | `durationMs = degrees * 8` for turns |
| `ms_per_cm` | 18 | `durationMs = distance_cm * 18` for forward/backward |
| `max_duration_ms` | 30000 | Total script duration must not exceed 30 000 ms |
| `max_step_duration_ms` | 10000 | Each individual step must not exceed 10 000 ms |

## Authoring a New Script

Open Bob and describe the rover behaviour you want in plain English.
Bob will:
1. Read `specs/free-roam-spec.md` for the allowed move vocabulary and calibration constants
2. Translate your description into a valid `CommandScript` JSON
3. Write the file to `specs/scripts/<kebab-case-name>.json`
4. Copy the same file to `services/client/src/specs/scripts/<kebab-case-name>.json`
5. Add an import and entry to `services/client/src/specs/scripts/index.ts` so the script appears in the Free Roam dropdown

Scripts folder starts empty — Bob authors scripts here as users describe what they want the rover to do.

## Running a Script on the Physical Rover

Use `run-trick.sh` (in the repo root) to execute the **Forward Then Circle** trick on the real rover automatically.

### Step 1 — One-time build (do this on IBM Wi-Fi or any internet connection)

```bash
docker compose -f services/docker-compose.yml build
```

Builds and caches all Docker images locally. Only needed again if the code changes.

### Step 2 — Switch your Mac Wi-Fi to `OL_DEMO`

Do this manually before running the script. The game service connects directly to the rover at `192.168.0.115` — that IP is only reachable on OL_DEMO. If you run the script while still on IBM Wi-Fi the game service will hang waiting for the rover.

### Step 3 — Run the trick

```bash
./run-trick.sh
```

The script will:
1. Start the containers from the local cache (no internet required)
2. Wait for the game service to be ready (Liberty `CWWKF0011I`)
3. Ping the rover and probe its WebSocket
4. Execute the Forward Then Circle script over the game service WebSocket
