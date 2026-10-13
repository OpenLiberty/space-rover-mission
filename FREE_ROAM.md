# Free Roam Mode

Free Roam is a new game mode added to the Space Rover Mission that lets you drive the rover freely in open space — no physical game board required. Instead of navigating planets and avoiding obstacles on a map, you describe what you want the rover to do and it just does it.

---

## How it differs from the original game

| | Original Game | Free Roam |
|---|---|---|
| **Board** | Required — physical LED game board with RFID tiles | Not used — rover runs in open space |
| **Control** | Hand gestures via webcam (OpenCV) | Pre-authored or Bob-generated scripts, picked and run from the browser |
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

When you describe a trick to Bob in plain English (e.g. _"go forward then do a circle"_), Bob reads the spec and translates your description into a `CommandScript` JSON file — a sequence of timed commands (`F`, `B`, `L`, `R`, `S`) that the rover executes in order. You can describe the trick in Bob IDE or directly in the browser, which talks to Bob Shell through the `bob-bridge` service.

```
You describe a trick (Bob IDE, or the browser via bob-bridge → Bob Shell)
        ↓
Bob reads specs/free-roam-spec.md (move vocabulary + calibration)
        ↓
Bob writes a CommandScript JSON to specs/scripts/
        ↓
Script appears in the browser dropdown (loaded at runtime from bob-bridge)
        ↓
F / B / L / R / S commands sent over WebSocket → Game Service → Rover
```

The `specs/` directory is the source of truth. Changing a calibration constant in the spec changes how Bob authors every future script, and any script in `specs/scripts/` is available in the browser without rebuilding or restarting containers.

---

## Initial setup

### 1. Clone the repository

```bash
git clone https://github.com/OpenLiberty/space-rover-mission.git
cd space-rover-mission
```

### 2. Open the repo in Bob IDE

Open the `space-rover-mission` folder in Bob. Bob will automatically detect the workspace and load the rover-spec skill from `.bob/skills/rover-spec/SKILL.md`.

### 3. Add your Bob API key

Browser-based trick generation needs a Bob API key. Generate one at https://bob.ibm.com (API Keys section), then:

```bash
cp services/.env.example services/.env
```

Edit `services/.env` and set `BOB_API_KEY`. The file is gitignored — never commit your key.

### 4. Build the Docker images (requires internet)

Do this once on IBM Wi-Fi or any internet connection. You only need to repeat this if the service code changes (not when `specs/` changes).

```bash
docker compose -f services/docker-compose.yml build
```

---

## Authoring a new script with Bob

There are two ways to ask Bob for a new trick. Both use the same rover-spec skill and produce the same result: a `CommandScript` JSON file in `specs/scripts/`.

Examples of what you can say:
- _"Go forward and then do a circle"_
- _"Do a figure 8"_
- _"Spin right for 3 seconds then reverse"_
- _"Do a three point turn"_

### Option 1: Bob IDE

With the repo open in Bob IDE, describe the rover behaviour in chat. Bob reads [`specs/free-roam-spec.md`](specs/free-roam-spec.md) and writes the JSON for you.

### Option 2: Describing a trick in the browser

In the Free Roam screen, open **Generate a new trick with Bob**, type a description, and click **Generate**. The browser sends your description to the `bob-bridge` service (`http://localhost:4000/generate`), which runs Bob Shell inside its container. Bob Shell uses the same rover-spec skill and writes the script to `specs/scripts/`. The generated script is then shown in the browser, where you can run it on the rover.

This requires:
- A Bob API key in `services/.env` (see [Initial setup](#initial-setup))
- Internet access from your Mac, since Bob Shell calls the Bob API (see [Known limitations](#known-limitations))

Generation can take a while. Bob Shell has a 10-minute timeout.

### What Bob does

1. Reads [`.bob/rules/bob-bridge-strict.md`](.bob/rules/bob-bridge-strict.md) and treats your description only as a description of rover movement
2. Translates it into a valid `CommandScript` JSON using the calibration constants in the spec
3. Writes the file to `specs/scripts/<name>.json` — the only place Bob is allowed to write

### The specs directory is the source of truth

The spec and the scripts live in [`specs/`](specs/). The `bob-bridge` container mounts the repo as `/workspace` **read-only**, with `specs/scripts/` overlaid as the only **read-write** path. Bob Shell can read the spec, rules, and skill, but can only write to `specs/scripts/`. The client loads the script list from `bob-bridge` at runtime (`GET /scripts`), reading whatever is in `specs/scripts/` at that moment. This means:

- You do **not** need to rebuild the client or run `docker compose down` and `up` when scripts or specs change.
- A script added by Bob (from the IDE or the browser), or edited by hand, shows up the next time the client loads its script list. Reload the page if it doesn't appear.

See [`specs/scripts/README.md`](specs/scripts/README.md) for the full JSON schema and calibration reference.

### Pre-authored scripts

The repo ships with a set of ready-made scripts in [`specs/scripts/`](specs/scripts/), such as Move Forward, Turn Left, Three Point Turn, Figure 8, Donut, and Forward Then Circle. Whatever is in that directory appears in the dropdown.

---

## Running Free Roam

Use the browser UI to pick, generate, and run scripts.

#### 1. Connect to the rover network

The rover is only reachable on the OL_DEMO network. To also keep internet access for Bob, connect **Ethernet to OL_DEMO** and **Wi-Fi to a public network** (see [Known limitations](#known-limitations)). If you only want to run existing scripts, being on OL_DEMO alone is enough.

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
- A **script dropdown** — every script currently in `specs/scripts/`, listed by name
- The selected script's **description** below the dropdown
- An **Execute** button — sends the script to the rover (shows _"Executing rover script..."_ while running, _"Rover script completed."_ when done)
- A **Stop** button — immediately halts the rover mid-script
- A **Generate a new trick with Bob** panel — describe a trick in plain English and Bob writes it for you (see [Option 2](#option-2-describing-a-trick-in-the-browser))
- An **End mission** button — exits back to the home screen

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

This swaps in a `mockrover` container that logs every command it receives instead of driving real hardware. The `bob-bridge` service is included, so the script dropdown and the Generate panel work here too (they need `services/.env`, see [Initial setup](#initial-setup)). Watch commands arrive in real time:

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

- **OL_DEMO has no internet access, so a single Wi-Fi connection can't do both jobs.** The rover is only reachable on OL_DEMO, but generating a trick with Bob requires reaching the Bob API over the internet. If your Mac is on OL_DEMO Wi-Fi only, the rover works but **Generate** will fail. If you're on a public network only, Bob works but the rover is unreachable.

  **Fix: use two connections at once.** Connect your Mac to OL_DEMO over **Ethernet**, and connect **Wi-Fi** to a public network. The rover traffic goes over Ethernet and Bob's traffic goes over Wi-Fi, so you can generate tricks and run them without switching networks.

- **The game service will hang if started without a route to the rover.** The game service connects to the rover at `192.168.0.115` on startup. If your Mac has no connection to OL_DEMO when `docker compose up` runs, the service will wait indefinitely for the rover connection. Connect to OL_DEMO first.
