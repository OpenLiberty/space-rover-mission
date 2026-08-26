#!/usr/bin/env bash
#################################################################################
# run-trick.sh
#
# Executes the "Forward Then Circle" rover trick.
#
# Pre-requisite (once, on normal internet):
#   docker compose -f services/docker-compose.yml build
#
# Usage:
#   1. Switch your Mac Wi-Fi to OL_DEMO manually
#   2. chmod +x run-trick.sh && ./run-trick.sh
#
# Requirements:
#   - docker / docker compose v2
#   - websocat  (brew install websocat)
#################################################################################

set -euo pipefail

ROVER_IP="192.168.0.115"
ROVER_PORT="5045"
GAME_WS="ws://localhost:9070/roversocket"
COMPOSE_FILE="services/docker-compose.yml"

# Script timings (milliseconds → seconds via sleep)
FORWARD_MS=4800
CIRCLE_MS=4400

# ─── Helpers ──────────────────────────────────────────────────────────────────

spinner() {
  local msg="$1" pid="$2"
  local frames='⠋⠙⠹⠸⠼⠴⠦⠧⠇⠏'
  local i=0
  while kill -0 "$pid" 2>/dev/null; do
    printf "\r  %s %s" "${frames:$((i % ${#frames})):1}" "$msg"
    sleep 0.1
    i=$((i + 1))
  done
  printf "\r"
}

sleep_countdown() {
  local total="$1" label="$2"
  local remaining="$total"
  while [ "$remaining" -gt 0 ]; do
    printf "\r  ⏱  %s — %ss remaining..." "$label" "$remaining"
    sleep 1
    remaining=$((remaining - 1))
  done
  printf "\r  ✓  %s done.                    \n" "$label"
}

# ─── Step 1: start containers from cached images ─────────────────────────────
echo ""
echo "══════════════════════════════════════════════"
echo "  Step 1: Starting containers from cache"
echo "══════════════════════════════════════════════"
docker compose -f "$COMPOSE_FILE" up -d
echo "  ✓ Containers started."

# ─── Step 2: wait for rover to be reachable (ping + WebSocket) ───────────────
echo ""
echo "══════════════════════════════════════════════"
echo "  Step 2: Waiting for rover connectivity"
echo "  (switch your Mac to OL_DEMO now if not done)"
echo "══════════════════════════════════════════════"

(
  until docker exec services-client-1 ping -c1 -W1 "$ROVER_IP" > /dev/null 2>&1; do
    sleep 2
  done
  until docker exec services-gameservice-1 \
    curl -sf --max-time 2 \
      -H "Upgrade: websocket" \
      -H "Connection: Upgrade" \
      -H "Sec-WebSocket-Key: test" \
      -H "Sec-WebSocket-Version: 13" \
      "http://$ROVER_IP:$ROVER_PORT/" > /dev/null 2>&1; do
    sleep 2
  done
) &
WAIT_PID=$!
spinner "Waiting for ping and WebSocket to rover..." "$WAIT_PID"
wait "$WAIT_PID"
echo "  ✓ Rover is reachable and WebSocket is live."

# ─── Step 3: execute forward-then-circle script ──────────────────────────────
echo ""
echo "══════════════════════════════════════════════"
echo "  Step 3: Executing Forward Then Circle"
echo "  (forward ${FORWARD_MS}ms → stop → spin ${CIRCLE_MS}ms → stop)"
echo "══════════════════════════════════════════════"

FORWARD_S=$(echo "scale=3; $FORWARD_MS / 1000" | bc)
CIRCLE_S=$(echo "scale=3; $CIRCLE_MS / 1000" | bc)

(
  echo "connectGUI|"
  sleep 3
  echo "startGame|bob,5"
  sleep 1
  echo "F"
  sleep "$FORWARD_S"
  echo "S"
  sleep 0.1
  echo "L"
  sleep "$CIRCLE_S"
  echo "S"
  sleep 0.2
  echo "endGame|"
  sleep 0.5
) | websocat --no-close "$GAME_WS" &
WS_PID=$!

sleep 3   && printf "\r  ✓ Connected, starting game...        \n"
sleep 1   && printf "\r  ✓ Game started (Free Roam mode 5)    \n"
echo "  → Sending FORWARD..."
sleep_countdown "$((FORWARD_MS / 1000))" "Moving forward"
echo "  → Sending STOP..."
sleep 0.1
echo "  → Sending SPIN LEFT..."
sleep_countdown "$((CIRCLE_MS / 1000))" "Spinning circle"
echo "  → Sending STOP..."
sleep 0.7

wait "$WS_PID" || true

echo ""
echo "══════════════════════════════════════════════"
echo "  ✓ Forward Then Circle complete!"
echo "══════════════════════════════════════════════"
