# Specs

This folder stores the Markdown specifications and rover scripts that drive the Space Rover Mission Free Roam mode.

---

## How It Works — The Full Picture

```
You describe a trick in Bob IDE
        ↓
Bob reads specs/free-roam-spec.md (move vocabulary + calibration constants)
        ↓
Bob writes a CommandScript JSON to specs/scripts/ and services/client/src/specs/scripts/
        ↓
Rebuild the client — the script appears in the dropdown in the browser
        ↓
User selects the script, hits Execute
        ↓
Browser sends F/B/L/R/S commands over WebSocket → Game Service → Rover
```

---

## Step 1 — Describe a Trick to Bob

Open Bob IDE and describe what you want the rover to do in plain English. For example:

> _"Do a donut, then move forward for 3 seconds, then do a 180 turn"_

Bob will activate the `rover-spec` skill automatically, then:
1. Read `specs/free-roam-spec.md` to understand the allowed moves and calibration constants
2. Translate your description into a valid `CommandScript` JSON using the timing math:
   - Forward/backward: `durationMs = distance_cm × 18`
   - Turn by degrees: `durationMs = degrees × 8`
   - Spin for time: use the requested duration directly
3. Write the script to `specs/scripts/<name>.json`
4. Copy the same file to `services/client/src/specs/scripts/<name>.json`
5. Add the import and entry to `services/client/src/specs/scripts/index.ts`

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

### Without hardware (mock mode)

```bash
cd services
docker compose -f docker-compose-test.yml up
```

This starts 4 containers:
| Container | Role |
|---|---|
| `client` | Browser UI at http://localhost:3000 |
| `gameservice` | WebSocket hub — routes commands between browser and rover |
| `mockrover` | Simulates the rover — logs every command it receives |
| `mockboard` | Simulates the game board — handles startup ACK only |

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

## Step 3 — Rebuild the Client

After Bob writes a new script, the client needs to be rebuilt so it picks up the new file:

```bash
cd services
docker compose -f docker-compose-test.yml build client
docker compose -f docker-compose-test.yml up
```

Or if the stack is already running, restart just the client:

```bash
docker compose -f docker-compose-test.yml up --build client
```

---

## Step 4 — Execute in the Browser

1. Go to **http://localhost:3000**
2. Enter a player name
3. The game mode is **Free Roam** (the only mode)
4. Hit **Start Mission**
5. The in-game screen shows:
   - A **script dropdown** listing all available scripts
   - The selected script's **description** below the dropdown
   - An **Execute** button — sends the script to the rover
   - A **Stop** button — immediately halts the rover mid-script
6. Pick your script, hit **Execute**, watch the rover go

---

## Step 5 — Verify on Mock Rover

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

| File | Trick | What it does |
|---|---|---|
| `move-forward.json` | Move Forward | Drive straight forward ~100 cm |
| `move-backward.json` | Move Backward | Drive straight backward ~100 cm |
| `turn-left.json` | Turn Left | Forward → pivot left 90° → forward |
| `turn-right.json` | Turn Right | Forward → pivot right 90° → forward |
| `three-point-turn.json` | Three Point Turn | Forward → pivot 180° → reverse |
| `figure-8.json` | Figure 8 | Full left circle → full right circle |
| `donut.json` | Donut | Spin left 3 full rotations |

Use these as reference when asking Bob to build new tricks — Bob can combine and chain them.

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
