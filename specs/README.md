# Specs

This folder stores the Markdown specifications and rover scripts that drive the Space Rover Mission Free Roam mode.

---

## How It Works — The Full Picture

```
You describe a trick (Bob IDE, or the browser via bob-bridge → Bob Shell)
        ↓
Bob reads specs/free-roam-spec.md (move vocabulary + calibration constants)
        ↓
Bob writes a CommandScript JSON to specs/scripts/
        ↓
The script appears in the browser dropdown (loaded at runtime from bob-bridge)
        ↓
User selects the script, hits Execute
        ↓
Browser sends F/B/L/R/S commands over WebSocket → Game Service → Rover
```

---

## Step 1 — Describe a Trick to Bob

Describe what you want the rover to do in plain English, either in Bob IDE or in the **Generate a new trick with Bob** panel in the browser. For example:

> _"Do a donut, then move forward for 3 seconds, then do a 180 turn"_

Bob will activate the `rover-spec` skill automatically, then:
1. Read `.bob/rules/bob-bridge-strict.md` and treat your description only as a description of rover movement
2. Read `specs/free-roam-spec.md` to understand the allowed moves and calibration constants
3. Translate your description into a valid `CommandScript` JSON using the timing math:
   - Forward/backward: `durationMs = distance_cm × 18`
   - Turn by degrees: `durationMs = degrees × 8`
   - Spin for time: use the requested duration directly
4. Write the script to `specs/scripts/<name>.json` — the only file Bob writes

**Example — "Do a donut, move forward 3 seconds, then do a 180 turn":**

```json
{
  "name": "Donut Forward 180",
  "description": "Spin left 3 full rotations, drive forward 3 seconds, then pivot 180 degrees.",
  "steps": [
    { "command": "L", "durationMs": 8640 },
    { "command": "S", "durationMs": 0 },
    { "command": "F", "durationMs": 3000 },
    { "command": "S", "durationMs": 0 },
    { "command": "R", "durationMs": 1440 },
    { "command": "S", "durationMs": 0 }
  ]
}
```

---

## Step 2 — Run the Stack

See [`FREE_ROAM.md`](../FREE_ROAM.md) for the full setup, including the Bob API key and the OL_DEMO network requirements.

### Without hardware (mock mode)

The `bob-bridge` service is included in `docker-compose-test.yml`, so the script dropdown and the Generate panel work in this mode. Both need `services/.env` with your `BOB_API_KEY` (see [`FREE_ROAM.md`](../FREE_ROAM.md)).

```bash
cd services
docker compose -f docker-compose-test.yml up
```

This starts 5 containers:
| Container | Role |
|---|---|
| `client` | Browser UI at http://localhost:3000 |
| `gameservice` | WebSocket hub — routes commands between browser and rover |
| `mockrover` | Simulates the rover — logs every command it receives |
| `mockboard` | Simulates the game board — handles startup ACK only |
| `bob-bridge` | Serves the script list and runs Bob Shell to generate new scripts |

### With real hardware

1. Open `services/game/src/main/webapp/META-INF/microprofile-config.properties`
2. Comment out the mock section and uncomment the physical hardware section with your rover's IP
3. Make sure the rover and game board are powered on and connected to the local Wi-Fi network
4. Run:
```bash
cd services
docker compose up
```

---

## Step 3 — Execute in the Browser

1. Go to **http://localhost:3000**
2. Enter a player name
3. The game mode is **Free Roam** (the only mode)
4. Hit **Begin mission**
5. The in-game screen shows:
   - A **script dropdown** listing all scripts currently in `specs/scripts/`
   - The selected script's **description** below the dropdown
   - An **Execute** button — sends the script to the rover
   - A **Stop** button — immediately halts the rover mid-script
   - A **Generate a new trick with Bob** panel — describe a trick and Bob writes the script
6. Pick your script, hit **Execute**, watch the rover go

No rebuild or restart is needed when `specs/scripts/` changes. The client loads the script list from bob-bridge at runtime.

---

## Step 4 — Verify on Mock Rover

While running in mock mode, watch the commands arrive in real time:

```bash
docker logs -f services-mockrover-1
```

For the donut + forward + 180 example you would see:
```
Message Received: L    ← donut spinning
Message Received: S    ← stop
Message Received: F    ← moving forward
Message Received: S    ← stop
Message Received: R    ← 180 pivot
Message Received: S    ← stop
```

---

## Existing Scripts

Every script in [`scripts/`](scripts/) appears in the browser dropdown. Each file's `name` and `description` fields say what it does. Use them as reference when asking Bob to build new tricks — Bob can combine and chain them.

---

## Calibration Reference

From `specs/free-roam-spec.md`:

| Constant | Value | Example |
|---|---|---|
| `ms_per_cm` | 18 | 50 cm forward = 900 ms |
| `ms_per_degree` | 8 | 90° turn = 720 ms · 180° = 1440 ms · 360° = 2880 ms |
| `max_step_duration_ms` | 10000 | No single step longer than 10 s |
| `max_duration_ms` | 30000 | Total script no longer than 30 s |

---

## Included Spec Files

- [`free-roam-spec.md`](free-roam-spec.md) — the full feature spec Bob reads to author scripts
- [`TEMPLATE.md`](TEMPLATE.md) — blank template for authoring new feature specs
- [`scripts/`](scripts/) — Bob-authored CommandScript JSON files
