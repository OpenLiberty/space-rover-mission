# Free Roam Mode

Free Roam is a new game mode added to the Space Rover Mission that lets you drive the rover freely in open space — no physical game board required. Instead of navigating planets and avoiding obstacles on a map, you describe what you want the rover to do and it just does it.

---

## How it differs from the original game

| | Original Game | Free Roam |
|---|---|---|
| **Board** | Required — physical LED game board with RFID tiles | Not used — rover runs in open space |
| **Control** | Hand gestures via webcam (OpenCV) | Plain-English commands typed in the browser, or automated scripts |
| **Objective** | Navigate the map, collect planets, avoid obstacles | Execute choreographed tricks and movements |
| **Game modes** | Classic, Planet Hop, Guided, Sudden Death | Free Roam (mode 5) |
| **Scoring** | Health and score tracked against the board | No scoring — pure movement execution |

The rover hardware is identical. The game service still routes all commands over WebSocket. The difference is that the game board is replaced by a mock service that handles only the startup handshake, and all movement decisions come from a pre-authored script rather than real-time gesture input.

---

## What is spec-driven development?

Free Roam was built using **spec-driven development** — the feature behaviour is defined in a Markdown spec file before any code is written, and an AI coding assistant (Bob) reads that spec to author rover scripts on demand.

The spec lives at [`specs/free-roam-spec.md`](specs/free-roam-spec.md) and defines:
- The **move vocabulary** — the set of named moves the rover understands (`forward`, `spin_left`, `turn_right`, etc.)
- **Calibration constants** — how milliseconds map to real-world distance and angle (`18 ms/cm`, `8 ms/degree`)
- **Safety rules** — max step duration (10 s), max total script duration (30 s), soft boundary radius (150 cm)

When you describe a trick to Bob in plain English (e.g. _"go forward then do a circle"_), Bob reads the spec and translates your description into a `CommandScript` JSON file — a sequence of timed commands (`F`, `B`, `L`, `R`, `S`) that the rover executes in order.

```
You describe a trick in Bob IDE
        ↓
Bob reads specs/free-roam-spec.md (move vocabulary + calibration)
        ↓
Bob writes a CommandScript JSON to specs/scripts/ and services/client/src/specs/scripts/
        ↓
Script appears in the browser dropdown or is run via run-trick.sh
        ↓
F / B / L / R / S commands sent over WebSocket → Game Service → Rover
```

The spec is the source of truth. Changing a calibration constant in the spec changes how Bob authors every future script.

---

## Initial setup

### 1. Clone the repository

```bash
git clone https://github.com/OpenLiberty/space-rover-mission.git
cd space-rover-mission
```

### 2. Open the repo in Bob IDE

Open the `space-rover-mission` folder in Bob. Bob will automatically detect the workspace and load the rover-spec skill from `.bob/skills/rover-spec/SKILL.md`.

### 3. Build the Docker images (requires internet)

Do this once on IBM Wi-Fi or any internet connection. You only need to repeat this if the code changes.

```bash
docker compose -f services/docker-compose.yml build
```

---

## Authoring a new script with Bob

With the repo open in Bob IDE, describe the rover behaviour you want in plain English. Bob will read [`specs/free-roam-spec.md`](specs/free-roam-spec.md) and write the JSON for you.

Examples:
- _"Go forward and then do a circle"_
- _"Do a figure 8"_
- _"Spin right for 3 seconds then reverse"_
- _"Do a three point turn"_

Bob will:
1. Translate your description into a valid `CommandScript` JSON using the calibration constants
2. Write the file to `specs/scripts/<name>.json`
3. Copy it to `services/client/src/specs/scripts/<name>.json`
4. Register it in `services/client/src/specs/scripts/index.ts` so it appears in the browser dropdown

After Bob writes a new script, rebuild the client to pick it up:
```bash
docker compose -f services/docker-compose.yml build client
```

See [`specs/scripts/README.md`](specs/scripts/README.md) for the full JSON schema and calibration reference.

### Pre-authored scripts

These scripts are already included in the repo:

| Script | What it does |
|---|---|
| Move Forward | Drive straight forward ~100 cm |
| Move Backward | Drive straight backward ~100 cm |
| Turn Left | Forward → pivot left 90° → forward |
| Turn Right | Forward → pivot right 90° → forward |
| Three Point Turn | Forward → pivot 180° → reverse |
| Figure 8 | Full left circle → full right circle |
| Donut | Spin left 3 full rotations |
| Forward Then Circle | Drive forward ~100 cm → spin one full rotation |

---

## Running Free Roam

### Mode A: Browser UI

Use this when you want to interactively pick and run scripts from the web interface.

#### 1. Switch Mac Wi-Fi to OL_DEMO

The rover is only reachable on the OL_DEMO network. Switch manually via the macOS Wi-Fi menu bar before starting the stack.

#### 2. Start the stack

```bash
docker compose -f services/docker-compose.yml up -d
```

#### 3. Open the browser

Go to **http://localhost:3000**.

#### 4. Enter your name and select Free Roam

- Enter your player name in the text field
- Select **Free Roam** from the game mode dropdown
- Hit **Begin mission**

#### 5. Execute a script

The in-game screen shows:
- A **script dropdown** — all pre-authored scripts listed by name
- The selected script's **description** below the dropdown
- An **Execute** button — sends the script to the rover (shows _"Executing rover script..."_ while running, _"Rover script completed."_ when done)
- A **Stop** button — immediately halts the rover mid-script
- An **End mission** button — exits back to the home screen

---

### Mode B: Automated script via `run-trick.sh`

Use this to run the **Forward Then Circle** trick on the physical rover end-to-end from a single command, with no browser interaction.

#### Switch Mac Wi-Fi to OL_DEMO, then run:

```bash
./run-trick.sh
```

The script does the following automatically:

| Step | What happens |
|---|---|
| **1** | Starts the containers from the local Docker cache — no internet required |
| **2** | Spins with a live indicator, polling ping and WebSocket until the rover responds |
| **3** | Pings the rover from inside the client container to confirm network reachability |
| **4** | Probes the rover WebSocket — confirms `101 Switching Protocols` |
| **5** | Sends the Forward Then Circle script over the game service WebSocket with live countdown output |

Example terminal output:
```
══════════════════════════════════════════════
  Step 1: Starting containers from cache
══════════════════════════════════════════════
  ✓ Containers started.

══════════════════════════════════════════════
  Step 2: Waiting for rover connectivity
  (switch your Mac to OL_DEMO now if not done)
══════════════════════════════════════════════
  ⠸ Waiting for ping and WebSocket to rover...
  ✓ Rover is reachable and WebSocket is live.

══════════════════════════════════════════════
  Step 3: Executing Forward Then Circle
  (forward 4800ms → stop → spin 4400ms → stop)
══════════════════════════════════════════════
  ✓ Connected, starting game...
  ✓ Game started (Free Roam mode 5)
  → Sending FORWARD...
  ⏱  Moving forward — 3s remaining...
  ✓  Moving forward done.
  → Sending SPIN LEFT...
  ⏱  Spinning circle — 2s remaining...
  ✓  Spinning circle done.
  → Sending STOP...

══════════════════════════════════════════════
  ✓ Forward Then Circle complete!
══════════════════════════════════════════════
```

---

## Stopping the stack

```bash
docker compose -f services/docker-compose.yml down
```

---

## Troubleshooting

### Mock mode (no physical rover)

If you don't have hardware available, you can run the full stack with a simulated rover to verify the UI and script execution end-to-end:

```bash
docker compose -f services/docker-compose-test.yml up --build
```

This swaps in a `mockrover` container that logs every command it receives instead of driving real hardware. Watch commands arrive in real time:

```bash
docker logs -f services-mockrover-1
```

For a Forward Then Circle script you would see:
```
Message Received: F
Message Received: S
Message Received: L
Message Received: S
```

Use mock mode to confirm your script logic is correct before running on the physical rover.

---

## Known limitations

- **Rover movements are approximate.** The calibration constants (`18 ms/cm`, `8 ms/degree`) are tuned to the specific rover hardware and surface. On different floors or battery levels the rover may overshoot or undershoot. If a turn or distance is off, ask Bob to adjust the `durationMs` values and re-run.

- **Circle and turning angles may not be exact.** A 360° circle requires the spin timing to be dialled in per-rover. The current values are tuned empirically — if the rover doesn't complete a full rotation, increase the `L`/`R` `durationMs` on the relevant script step.

- **Switching to OL_DEMO via the script is unreliable.** macOS `networksetup -setairportnetwork` does not always join the network successfully, especially if the SSID is not currently broadcasting or the password is cached differently. It is more reliable to switch Wi-Fi to `OL_DEMO` manually in the macOS menu bar before running `run-trick.sh`.

- **The game service will hang if started before switching to OL_DEMO.** The game service connects to the rover at `192.168.0.115` on startup. If you are still on IBM Wi-Fi when `docker compose up` runs, the service will wait indefinitely for the rover connection. Always switch to OL_DEMO first.
