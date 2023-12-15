#!/bin/bash

cd $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

CONTAINER_BUILD_TOOL=$(nerdctl --namespace=k8s.io)

# Build liberty
cd ../services/liberty
mvn -pl models install
mvn -DskipTests package 
$CONTAINER_BUILD_TOOL build -t space-rover/leaderboard ./leaderboard
$CONTAINER_BUILD_TOOL build -t space-rover/messenger ./messenger
$CONTAINER_BUILD_TOOL build -t space-rover/game ./game
$CONTAINER_BUILD_TOOL build -t space-rover/mock-board ./mock/board
$CONTAINER_BUILD_TOOL build -t space-rover/mock-rover ./mock/rover

# Build monitoring
cd ../monitoring
$CONTAINER_BUILD_TOOL build -t space-rover/grafana ./grafana
$CONTAINER_BUILD_TOOL build -t space-rover/prometheus ./prometheus

# Build client
cd ../
#$CONTAINER_BUILD_TOOL 
cd ../services
nerdctl --namespace=k8s.io build -t space-rover/client \
    --build-arg GAME_URL=http://localhost/game-backend/game \
    --build-arg GAME_SOCKET_URL=ws://localhost/rover-backend/ \
    --build-arg GAME_DURATION_SECONDS=120 \
    --build-arg LEADERBOARD_URL=http://localhost/mongo/leaderboard ./client
cd ../yaml
