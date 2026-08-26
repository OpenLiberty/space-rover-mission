# Spec-Driven Rover Control — Plan

## Overview

Add a **Free Roam mode** (mode 5) to the Space Rover Mission where a user types plain-English
instructions into Bob, Bob fills out a structured spec template, and that spec drives code
injection into both the Java game service and the React client.

The flow is:
1. User describes what they want the rover to do inside Bob
2. Bob fills out a Markdown spec template under `specs/`
3. Bob reads the completed spec and writes a `CommandScript` JSON to `specs/scripts/`
4. The client presents a script picker dropdown — user selects a script and hits Execute
5. The rover executes the script in Free Roam mode — no physical game board required

**Scope:** `specs/` folder, Java game service, React client, one-line NodeMCU firmware change,
Bob skill, mock developer environment.
**Non-goals:** Leaderboard integration, prompt history/replay, camera or sensor feedback, runtime LLM calls.

---

## Sub-Tasks

---

### Sub-Task 1 — Create the `specs/` folder and spec template

**Status:** `[x] done`

**Intent**  
Establish the `specs/` directory at the repo root and create a reusable Markdown template
that a user fills out in Bob to describe the rover behaviour they want. This template is the
single source of truth that all downstream code injection reads from. It must capture every
value that needs to flow into both the Java service and the React client so nothing is
hardcoded in two places.

**Expected Outcomes**
- `specs/` directory exists at repo root (sibling to `services/` and `devices/`)
- `specs/TEMPLATE.md` exists with clearly labelled, fill-in-the-blank sections
- `specs/free-roam-spec.md` exists as a fully filled-out example for the Free Roam feature,
  demonstrating how a user would complete the template with Bob's help
- A `specs/README.md` exists explaining the purpose of the folder, how to use the template
  with Bob, and how Bob reads the spec to inject code

**Todo List**
1. Create `specs/README.md` — explain the spec-driven workflow: user fills template in Bob → Bob reads spec → Bob injects code into game service and client
2. Create `specs/TEMPLATE.md` with the following sections (each clearly marked as fill-in):
   - `## Feature Name` — short name used in UI labels and Java constants
   - `## Game Mode ID` — numeric ID (next available: 5)
   - `## Description` — one sentence shown in the game mode selector UI
   - `## Move Vocabulary` — table of supported semantic move names, their parameters, and plain-English aliases the LLM should recognise
   - `## Calibration Constants` — ms per degree of turn, ms per cm forward, max total script duration in ms
   - `## LLM System Prompt` — the exact system prompt to send to the LLM, referencing the move vocabulary above
   - `## Safety Rules` — max duration per step, max total duration, soft boundary radius in cm
   - `## UI Labels` — prompt input placeholder text, execute button label, cancel button label, status messages (executing / cancelled / error)
   - `## Board Required` — yes/no flag controlling whether the state machine skips board connection
3. Create `specs/free-roam-spec.md` by copying the template and filling every section with the Free Roam values discussed in the brainstorm (mode 5, spin_left/spin_right/forward/backward/turn_left/turn_right/figure_8/square/patrol, calibration constants, the constrained LLM system prompt, UI label text)

**Relevant Context**
- Repo root structure: `services/`, `devices/`, `documentation/`, `gestures/` — `specs/` goes here
- Existing game mode pattern: `Constants.GAME_MODE_NAME_CLASSIC`, `GAME_MODE_DESC_CLASSIC` in
  [`services/game/src/main/java/io/openliberty/spacerover/game/models/Constants.java`]
- Existing mode IDs: 1 = Classic, 2 = Planet Hop, 3 = Guided, 4 = Sudden Death → next is 5
- Board connection is controlled by `GameServerStateMachine` — the `## Board Required` flag
  tells the injection step whether to patch the state machine

---

### Sub-Task 2 — Inject into the Java game service

**Status:** `[x] done`

**Intent**  
Read `specs/free-roam-spec.md` and generate / patch all Java files needed to support the
Free Roam mode and the `POST /api/commands/interpret` endpoint. Every string, constant, and
calibration value must come from the spec — nothing hardcoded independently.

**Expected Outcomes**
- `Constants.java` has `INIT_GAME_FREE_ROAM`, `GAME_MODE_NAME_FREE_ROAM`, `GAME_MODE_DESC_FREE_ROAM`
- `CommandStep.java` and `CommandScript.java` POJOs exist in the `models/` package
- `MoveExpander.java` exists and translates each semantic move from the spec's Move Vocabulary
  into a `List<CommandStep>` using calibration constants from config
- `CommandInterpreterResource.java` exists as a JAX-RS `@POST /api/commands/interpret` resource
  that calls the LLM, parses the semantic move JSON, validates it, expands it, and returns the
  `CommandScript` to the client
- `microprofile-config.properties` has LLM endpoint, LLM API key placeholder, and all three
  calibration constants (`ms_per_degree`, `ms_per_cm`, `max_duration_ms`) from the spec
- `server.xml` CORS block covers the `/api` path
- `GameResource.java` returns Free Roam as a 5th game mode
- `GameServerStateMachine.java` handles mode 5 by skipping board connection (because
  `## Board Required = no` in the spec)

**Todo List**
1. Read `specs/free-roam-spec.md` — extract Feature Name, Mode ID, Description, Move Vocabulary,
   Calibration Constants, LLM System Prompt, Safety Rules, Board Required flag
2. Patch `Constants.java` — add `INIT_GAME_FREE_ROAM = "5"`, `GAME_MODE_NAME_FREE_ROAM`, and
   `GAME_MODE_DESC_FREE_ROAM` using the exact strings from the spec
3. Create `CommandStep.java` in `models/` — fields: `String command`, `int durationMs`;
   include a validation method that checks command is in whitelist `{F, B, L, R, S}` and
   `durationMs` is between 0 and the spec's max-per-step value
4. Create `CommandScript.java` in `models/` — field: `List<CommandStep> steps`; include a
   validation method that checks total duration does not exceed spec's `max_duration_ms`
5. Create `MoveExpander.java` in the `game/` package — one method per semantic move in the
   spec's Move Vocabulary table; reads calibration constants via `@Inject @ConfigProperty`
6. Create `CommandInterpreterResource.java` in `websocket/server/` — JAX-RS `@POST /api/commands/interpret`,
   accepts `{ "prompt": "..." }` JSON, injects the spec's LLM System Prompt as the system
   message, calls the LLM via MicroProfile REST Client, parses response into semantic moves,
   calls `MoveExpander`, validates `CommandScript`, returns it; returns HTTP 400 if validation fails
7. Patch `microprofile-config.properties` — add LLM endpoint URL, `llm.api.key=CHANGEME`,
   and the three calibration constants from the spec
8. Patch `server.xml` — add or extend CORS to cover `POST /api/commands/interpret`
9. Patch `GameResource.java` — add Free Roam to the list of returned game modes, using
   the constants added in step 2
10. Patch `GameServerStateMachine.java` — in the `START_GAME` handling, if the game mode is
    `INIT_GAME_FREE_ROAM`, skip `attachGameBoard()` and transition directly to `ALL_CONNECTED`

**Relevant Context**
- Existing JAX-RS resource pattern: [`services/game/src/main/java/io/openliberty/spacerover/game/websocket/server/GameResource.java`]
- Existing models pattern: [`services/game/src/main/java/io/openliberty/spacerover/game/models/Constants.java`],
  `GameMode.java`, `GameScore.java` in the same package
- Config injection pattern: `@Inject @ConfigProperty` — already used throughout the game service
- MicroProfile REST Client feature already enabled in `server.xml` (`mpRestClient-3.0`)
- State machine board-skip logic lives in: [`services/game/src/main/java/io/openliberty/spacerover/game/GameServerStateMachine.java`]
  specifically `connectGamePieces()` in `GameServer.java` at line 352
- CORS currently only covers `/game` path in `server.xml` line 28

---

### Sub-Task 3 — Inject into the React client

**Status:** `[x] done`

**Intent**  
Read `specs/free-roam-spec.md` and generate / patch all React/TypeScript files needed to
render the prompt input UI and execute command scripts over the existing WebSocket. UI label
strings, calibration constants (for dead reckoning), and game mode metadata all come from
the spec — the same values used in Sub-Task 2 — so both layers stay in sync.

**Expected Outcomes**
- `usePromptControls.ts` hook exists and can replay a `CommandScript` over a WebSocket with
  correct timing, an `isExecuting` flag, and a `cancelScript()` escape hatch
- `PromptInput.tsx` component exists and renders a textarea, Execute button, Cancel/Stop button,
  and a status message — all using the label strings from the spec
- `GameScreen.tsx` conditionally renders `<PromptInput />` when `gameMode === "5"` and disables
  keyboard controls while `isExecuting` is true
- `useGame.ts` exposes the WebSocket ref to the prompt input path
- `useGameModes.ts` includes Free Roam in the hardcoded fallback list so the UI works even if
  the server call fails
- Dead reckoning state (`x`, `y`, `headingDeg`) is tracked inside `usePromptControls.ts` and
  a soft-boundary warning is shown in `PromptInput.tsx` if the simulated path exceeds the
  spec's boundary radius

**Todo List**
1. Read `specs/free-roam-spec.md` — extract UI Labels, Calibration Constants, Safety Rules,
   Feature Name, Mode ID, Description
2. Create `usePromptControls.ts` in `hooks/` — accepts `(websocket, gameState)`; exposes
   `executeScript(script)`, `cancelScript()`, `isExecuting`; replays steps with `setTimeout`
   chains; cancels on `gameState !== InGame`; updates dead reckoning state after each step
   using calibration constants from the spec; pattern mirrors `useKeyboardControls.ts`
3. Create `PromptInput.tsx` in `components/` — textarea with placeholder from spec,
   Execute button and Cancel button using spec label strings, status line showing
   executing/cancelled/error states, soft-boundary warning message if dead reckoning
   estimate exceeds spec radius; pattern mirrors `BatteryStatus.tsx`
4. Patch `useGame.ts` — expose `socket` (already a `useRef`) in the return object so
   `PlayPage` can pass it to `PromptInput`
5. Patch `GameScreen.tsx` — accept `socket` and `gameMode` as additional props; mount
   `<PromptInput socket={socket} />` conditionally when `gameMode === "5"`; pass
   `isExecuting` to `useKeyboardControls` to suppress keyboard input during execution
6. Patch `useGameModes.ts` — add Free Roam to the `defaultGameModes` fallback array using
   Mode ID 5, Feature Name, and Description from the spec
7. Patch `PlayPage.tsx` — pass `socket` and `gameMode` down to `<GameScreen />`

**Relevant Context**
- Existing hook pattern: [`services/client/src/hooks/useKeyboardControls.ts`] — same
  `(websocket, gameState)` signature; same `websocket.send()` pattern
- Existing component pattern: [`services/client/src/components/BatteryStatus.tsx`],
  `HealthBar.tsx` — simple functional components with Tailwind styling
- `useGame.ts` already holds `socket` as `useRef<WebSocket | null>` at line 55 —
  just needs to be returned
- `GameScreen.tsx` is the InGame render branch in `PlayPage.tsx` at line 54
- `useGameModes.ts` has a hardcoded `defaultGameModes` fallback at line 26 — Free Roam
  entry goes here
- Dead reckoning math: `x += cos(headingRad) * (durationMs / MS_PER_CM)` for F/B,
  `headingDeg += (durationMs / MS_PER_DEGREE)` for L/R — constants from spec

---

### Sub-Task 4 — NodeMCU firmware one-line update

**Status:** `[x] done`

**Intent**  
Add mode 5 support to the rover firmware so that when the game service sends `'5'` on
WebSocket connect, the rover sets `isGameStarted = true` and enables movement commands.
Without this, the firmware ignores all F/B/L/R/S commands during a Free Roam session.

**Expected Outcomes**
- `SpaceRover_NodeMCU.ino` accepts payload `'5'` the same way it accepts `'1'` and `'4'`
- No other firmware behaviour changes

**Todo List**
1. Read `specs/free-roam-spec.md` — confirm Mode ID is 5 and Board Required is no
2. Patch `devices/space-rover/src/SpaceRover_NodeMCU.ino` — in `webSocketEvent()` at the
   `WStype_TEXT` block, add `|| payload[0] == '5'` to the existing condition on line 151
   that handles modes 1 and 4 (`isGameStarted = true; Serial.println("<GS>");`)

**Relevant Context**
- Firmware condition at line 151: `if (payload[0] == '1' || payload[0] == '4')`
  in [`devices/space-rover/src/SpaceRover_NodeMCU.ino`]
- Mode 2 (Planet Hop) and mode 3 (Guided) have their own branches with different serial
  messages — Free Roam behaves like Classic (mode 1), no special serial needed

---

### Sub-Task 5 — Developer experience: Bob skill, script folder, script picker UI, mock config

**Status:** `[x] done`

**Intent**  
Complete the Option A spec-driven developer experience so someone with no physical hardware
can clone the repo, run one docker compose command, author a script in Bob, and execute it
in the browser against the mock rover. Four things need to connect: Bob needs a skill that
tells it how to author scripts; there needs to be a `specs/scripts/` folder with a working
example for Bob to pattern-match from; the `PromptInput.tsx` component needs to become a
script-picker dropdown instead of a textarea that POSTs to a now-removed LLM endpoint; and
the mock config needs to be a first-class toggle so `docker-compose-test.yml` works without
editing any source files.

**Expected Outcomes**
- `.bob/skills/rover-spec.md` exists — a Bob skill that instructs Bob to read
  `specs/free-roam-spec.md` and write a valid `CommandScript` JSON to `specs/scripts/`
  whenever a user describes rover behaviour in chat
- `specs/scripts/README.md` exists — explains the folder purpose and the JSON schema
  Bob must follow
- `specs/scripts/example-donuts.json` exists — a fully valid `CommandScript` JSON
  Bob can use as a pattern reference
- `PromptInput.tsx` is replaced — the textarea + fetch to `/api/commands/interpret`
  is replaced by a dropdown that lists scripts from a static TypeScript registry,
  plus an Execute button and Stop button (the `usePromptControls` hook stays unchanged)
- `specs/scripts/index.ts` exists in the client source — a static registry that imports
  each script JSON and exports a typed array Bob can add new entries to
- `docker-compose-test.yml` is updated — the `gameservice` entry passes environment
  overrides so it uses `mockrover` and `mockboard` hostnames instead of the physical
  hardware IPs in `microprofile-config.properties`; no source file edits required to
  switch between hardware and mock modes
- `specs/README.md` is updated — adds a "Running without hardware" section pointing to
  `docker-compose-test.yml` and explaining the full Bob → script → browser → mock rover flow
- `CommandInterpreterResource.java` and `ApiApplication.java` are deleted — they were
  the runtime LLM endpoint from the earlier implementation, now superseded by Option A

**Todo List**
1. Create `.bob/skills/rover-spec.md` — a Bob skill with:
   - Trigger: user describes rover behaviour in plain English
   - Instructions: read `specs/free-roam-spec.md` for the allowed move vocabulary,
     calibration constants, and safety rules; translate the description into a valid
     `CommandScript` JSON using the same schema as `specs/scripts/example-donuts.json`;
     write the file to `specs/scripts/<kebab-case-name>.json`; then add an entry to
     `services/client/src/specs/scripts/index.ts` so it appears in the client dropdown
2. Create `specs/scripts/README.md` — explain: this folder holds Bob-authored rover
   scripts; each file is a `CommandScript` JSON; the schema is `{ name, description, steps: [{ command, durationMs }] }`;
   all values must respect the calibration constants and safety rules in `free-roam-spec.md`
3. Create `specs/scripts/example-donuts.json` — a complete valid example:
   `{ "name": "Donuts", "description": "Spin left for 5 seconds", "steps": [{ "command": "L", "durationMs": 5000 }, { "command": "S", "durationMs": 0 }] }`
4. Create `services/client/src/specs/scripts/index.ts` — a static registry:
   import each script JSON and export a typed `ScriptEntry[]` array with `name`,
   `description`, and `steps` fields; start with just the donuts example imported
5. Replace `PromptInput.tsx` — rewrite the component:
   - Remove the textarea and the `fetch` call to `/api/commands/interpret`
   - Import the `ScriptEntry[]` registry from `services/client/src/specs/scripts/index.ts`
   - Render a `<select>` dropdown listing all scripts by name
   - Keep the Execute button (calls `executeScript` with the selected script's steps),
     Stop button (calls `cancelScript`), status line, and boundary warning — all
     unchanged from the existing implementation
   - Keep the same Props interface shape so `GameScreen.tsx` needs no changes
6. Delete `services/game/src/main/java/io/openliberty/spacerover/game/websocket/server/CommandInterpreterResource.java`
7. Delete `services/game/src/main/java/io/openliberty/spacerover/game/websocket/server/ApiApplication.java`
8. Update `services/docker-compose-test.yml` — add an `environment:` block to the
   `gameservice` service that sets:
   `IO_OPENLIBERTY_SPACEROVER_IP=mockrover`, `IO_OPENLIBERTY_SPACEROVER_PORT=5045`,
   `IO_OPENLIBERTY_GAMEBOARD_IP=mockboard`, `IO_OPENLIBERTY_GAMEBOARD_PORT=5045`
   (MicroProfile Config picks up env vars automatically, overriding the properties file)
9. Update `specs/README.md` — add a "Running without hardware" section that says:
   run `docker compose -f services/docker-compose-test.yml up`, open the browser,
   select Free Roam mode, pick a script from the dropdown, hit Execute, and watch
   the mock rover log the commands in its container output

**Relevant Context**
- Bob skills live in `.bob/skills/` — see existing skills for the frontmatter schema
- `specs/scripts/` is a new folder at repo root level under `specs/`
- The client registry `index.ts` lives inside the React source tree so it can be
  statically imported — `services/client/src/specs/scripts/index.ts`
- `PromptInput.tsx` currently has a textarea and a fetch to `/api/commands/interpret`
  at [`services/client/src/components/PromptInput.tsx`] — full rewrite required
- `usePromptControls.ts` hook is unchanged — it already accepts a `CommandScript`
  and replays it; only the UI layer changes
- `GameScreen.tsx` passes props to `PromptInput` — the Props interface must stay
  compatible so `GameScreen.tsx` needs no changes
- Mock rover: [`services/mock/rover/src/main/java/io/openliberty/mockrover/websocket/server/MockServer.java`]
  already logs received messages — no change needed
- MicroProfile Config env var override: env var names are the property key uppercased
  with dots replaced by underscores — e.g. `io.openliberty.spacerover.ip` →
  `IO_OPENLIBERTY_SPACEROVER_IP`
- `docker-compose-test.yml` already exists at [`services/docker-compose-test.yml`]
  and already includes `mockrover` and `mockboard` services — only the `gameservice`
  env block is missing

---

### Sub-Task 6 — Strip unused services, game modes, and UI down to Free Roam only

**Status:** `[x] done`

**Intent**  
Remove everything that is not required for the spec-driven Free Roam demo: leaderboard,
MongoDB, Prometheus, Grafana, gesture control, all non-Free-Roam game modes, health/score
UI, and the board mock. The result is a minimal three-container stack (client + gameservice +
mockrover) backed by a simplified game service and a stripped React client. Every removal
must leave the remaining code compiling and the Free Roam flow fully functional.

**Expected Outcomes**
- `docker-compose-test.yml` only starts: `gameservice`, `client`, `mockrover`
- Java game service has no leaderboard, metrics, health-check, or non-Free-Roam mode code
- React client has no leaderboard page, health bar, score display, timer, or sounds tied
  to game events — only the Free Roam in-game screen with the script picker remains
- `gestures/`, `services/leaderboard/`, `services/prometheus/`, `services/grafana/`
  directories are deleted
- The application still starts, the WebSocket handshake completes, Free Roam mode starts,
  a script executes, and the mock rover logs the commands

**Todo List — 6a: Java game service**
1. Read `services/game/src/main/java/io/openliberty/spacerover/game/websocket/server/GameServer.java`
   in full before making any changes
2. Delete the following files entirely:
   - `services/game/src/main/java/io/openliberty/spacerover/client/LeaderboardClient.java`
   - `services/game/src/main/java/io/openliberty/spacerover/game/GameLeaderboard.java`
   - `services/game/src/main/java/io/openliberty/spacerover/game/websocket/server/GameServerHealth.java`
   - `services/game/src/main/java/io/openliberty/spacerover/game/GuidedGame.java`
   - `services/game/src/main/java/io/openliberty/spacerover/game/SpaceHop.java`
   - `services/game/src/main/java/io/openliberty/spacerover/game/SuddenDeathGame.java`
   - `services/game/src/main/java/io/openliberty/spacerover/game/models/GameScore.java`
3. Patch `GameServer.java`:
   - Remove `leaderboardHost` and `leaderboardPort` `@Inject @ConfigProperty` fields
   - Remove `import` lines for `GameLeaderboard`, `GameScore`, `GuidedGame`, `SpaceHop`,
     `SuddenDeathGame` and any other now-deleted classes
   - Remove `gestureSession` field and all references to it
   - Remove all `@Gauge` metric annotation methods at the bottom of the file
   - In `startGame()`: remove the `else if` branches for modes 1, 2, 3, 4 — keep only
     the Free Roam (mode 5) branch; remove `registerSpaceHopEventManager()` call
   - In `endGameFromServer()`: remove `getLeaderboard().updateLeaderboard(leaderboardEntry)`,
     `aggregateDamage`, `totalScorePoints`, `totalGameTimeInSeconds` lines — keep only
     `sendTextToGuiSocket(Constants.END_GAME)`
   - Remove `incrementGamesPlayed()` method and its call in `endGameFromServer()`
   - Remove `numberOfGamesPlayed`, `aggregateDamage`, `totalScorePoints`,
     `totalGameTimeInSeconds`, `numberOfClassicGamesPlayed`, `numberOfPlanetHopGamesPlayed`,
     `numberOfGuidedGamesPlayed`, `numberOfSuddenDeathGamesPlayed` fields
   - Remove `registerSpaceHopEventManager()` method
   - Remove `getLeaderboard()` method and `testLeaderboard()` method
   - Remove `getLeaderboard()` call from `connectGamePieces()`
   - In `handleMessage()`: remove `connectGesture` case, colour cases (BLU/GRN/PUR/YW/RED),
     `updateBoardAndGame()` calls — keep connectGUI, ROVER_ACK, GAMEBOARD_ACK (needed for
     state machine even if board is mock), START_GAME, END_GAME, directions, GAME_HEALTH_TEST
   - Remove `sendBoardColour()` calls and the `update()` handler branches for
     FIVE_SECONDS_LEFT and PLANET_CHANGED
   - Remove the `LOGGER.log` in `onOpen` that prints leaderboard host/port
4. Patch `Constants.java` — remove: colour score values map, `COLOURS` arrays,
   `COLOUR_RED`/`COLOUR_BLUE`/`COLOUR_GREEN`/`COLOUR_YELLOW`/`COLOUR_PURPLE`/`COLOUR_RED_SUN`
   constants, `SUN_RFID_IDENTIFIERS` set, mode names/descs for Classic/Guided/PlanetHop/SuddenDeath,
   `INIT_GAME_CLASSIC/HOP/GUIDED/SUDDEN_DEATH`, `CONNECT_GESTURE`, `ROVER_CONNECTION_FAILED`
   — keep: directions (F/B/L/R/S), ROVER_ACK, GAMEBOARD_ACK, SERVER_READY, START_GAME,
   END_GAME, INIT_GAME_FREE_ROAM, GAME_MODE_NAME/DESC_FREE_ROAM, delimiters, ERROR_MESSAGE,
   GAME_HEALTH_TEST/ACK, ROVER_SOCKET_NAME, BOARD_SOCKET_NAME, GUI_BATTERY_PCT
5. Patch `GameResource.java` — remove modes 1–4 entirely; return only Free Roam
6. Patch `microprofile-config.properties` — remove `leaderboard` hostname/port lines
7. Patch `server.xml` — remove `mpMetrics-4.0` and `mpHealth-4.0` feature entries;
   remove `<mpMetrics authentication="false" />` element
8. Delete `services/game/src/main/java/io/openliberty/spacerover/game/models/GameScore.java`
   if not already deleted in step 2 — verify and skip if done

**Todo List — 6b: React client**
1. Read `services/client/src/App.tsx` and `services/client/src/hooks/useGame.ts` in full
2. Delete the following files entirely:
   - `services/client/src/pages/LeaderboardPage.tsx`
   - `services/client/src/hooks/useLeaderboard.ts`
   - `services/client/src/components/LeaderboardTable.tsx`
   - `services/client/src/components/PlacementDisplay.tsx`
   - `services/client/src/components/HealthBar.tsx`
   - `services/client/src/hooks/useKeyboardColours.ts`
3. Patch `App.tsx` — remove the `/leaderboard` route and its import
4. Patch `useGame.ts`:
   - Remove `useTimer` import and usage — remove `formattedTime`, `timeRemaining`,
     `startTimer`, `stopTimer`, `timerSound`, `shortTimerSound`
   - Remove `useKeyboardColours` import and call
   - Remove all `useSound` imports and sound variables (crash, score, timer, shortTimer)
   - Remove score state and `Event.Score` / `Event.PlanetChange` handling
   - Remove health state and `Event.Health` handling
   - Remove `Event.ConnectGesture` send in `onopen`
   - Remove the `useEffect` watching `timeRemaining`
   - Remove `endGame()` function — game ends from server only in Free Roam
   - Keep: gameState, battery, error, socket, `startGame()`, `Event.End` handling,
     `Event.ServerReady`, `Event.Battery`, `Event.Error`
   - Update return object to remove `formattedTime`, `health`, `score`, `endGame`
5. Patch `PlayPage.tsx` — remove `health`, `score`, `formattedTime` destructuring;
   remove `endGame` reference; update `<GameScreen>` props accordingly
6. Patch `GameScreen.tsx` — remove `health`, `score`, `time` props and their display
   elements (`<HealthBar>`, `<Stat>` for score, `<Stat>` for time); keep player name,
   game mode label, `<PromptInput>`, and End Mission button
7. Patch `useGameModes.ts` — remove modes 1–4 from the `defaultGameModes` fallback;
   keep only Free Roam (id: 5)
8. Patch `lib/config.ts` — remove `leaderboardURL` export
9. After all deletions, check for any remaining import errors for deleted files and
   remove those import lines

**Todo List — 6c: docker-compose and directories**
1. Rewrite `services/docker-compose-test.yml` — keep only `gameservice`, `client`,
   `mockrover`; remove `mongo`, `leaderboard`, `prometheus`, `grafana`, `mockboard`;
   keep the `gameservice` environment block with mock rover IP overrides; remove the
   `LEADERBOARD_URL` build arg from the `client` service
2. Delete `gestures/` directory
3. Delete `services/leaderboard/` directory
4. Delete `services/prometheus/` directory
5. Delete `services/grafana/` directory

**Relevant Context**
- `GameServer.java` uses `gestureSession` alongside `guiSession` — removing gesture
  means only `guiSession` remains; `connectGesture` handling in the state machine should
  also be removed from `GameServerStateMachine.java` to avoid the state machine waiting
  for a gesture connection that will never come
- `GameServerStateMachine.java` has states GUI_CONNECTED, GESTURE_CONNECTED,
  GUI_AND_GESTURE_CONNECTED — with gesture removed, `connectGUI` should transition
  directly to `GUI_AND_GESTURE_CONNECTED` so the connection sequence still completes
- The `GAME_DURATION_SECONDS` build arg in the client docker-compose can stay — it is
  used by `useTimer` but since we're removing the timer from `useGame.ts` it becomes
  unused; remove it from both the compose arg and `lib/config.ts`
- `LeaderboardPage` is the redirect target after `GameEnded` state in `PlayPage.tsx` —
  after removing it, `GameEnded` should render a simple "Game complete" message instead
  of navigating away
- `useGame.ts` currently calls `useKeyboardControls` — this was moved to `GameScreen.tsx`
  in Sub-Task 3; double-check before removing to avoid double-removal
- The `GameServerStateMachine` `isReadyToConnectGamePieces()` currently requires both
  GUI and Gesture connected — after stripping gesture, this must fire after GUI alone
