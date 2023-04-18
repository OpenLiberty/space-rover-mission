#!/bin/bash
# Build all the images
# client
docker build -t space-rover/client \
--build-arg GAME_URL=http://localhost:9080/game \
--build-arg GAME_SOCKET_URL=ws://localhost:9080/roversocket \
--build-arg GAME_DURATION_SECONDS=120 \
--build-arg LEADERBOARD_URL=http://localhost:9070/mongo/leaderboard ../services/client

# gameservice
docker build -t space-rover/gameservice ../services/game

# leaderboard
docker build -t space-rover/leaderboard ../services/leaderboard

# mockboard (if applicable)
docker build -t space-rover/mockboard ../services/mock/board

# mockrover (if applicable)
docker build -t space-rover/mockrover ../services/mock/rover

# mongo
docker build -t space-rover/mongo ../services/leaderboard/assets

# prometheus 
docker build -t space-rover/prometheus ../services/prometheus

# grafana
docker build -t space-rover/grafana ../services/grafana

echo Your Images are Complete!
echo You can now apply your kubernetes files.
