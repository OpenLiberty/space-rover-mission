#!/bin/bash
# Build all the images
# client
podman build -t space-rover/client \
--build-arg GAME_URL=http://localhost:9080/game \
--build-arg GAME_SOCKET_URL=ws://localhost:9080/roversocket \
--build-arg GAME_DURATION_SECONDS=120 \
--build-arg LEADERBOARD_URL=http://localhost:9070/mongo/leaderboard ../services/client

# gameservice
podman build -t space-rover/gameservice ../services/game

# leaderboard
podman build -t space-rover/leaderboard ../services/leaderboard

# mockboard (if applicable)
podman build -t space-rover/mockboard ../services/mock/board

# mockrover (if applicable)
podman build -t space-rover/mockrover ../services/mock/rover

# mongo
podman build -t space-rover/mongo ../services/leaderboard/assets

# prometheus 
podman build -t space-rover/prometheus ../services/prometheus

# grafana
podman build -t space-rover/grafana ../services/grafana

# inventory
podman build -t space-rover/inventory ../services/inventory

# system
podman build -t space-rover/system ../services/system


echo Your Images are Complete!
echo You can now apply your kubernetes files.
