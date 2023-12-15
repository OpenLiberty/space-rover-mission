#!/bin/bash

cd $( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )

CONTAINER_BUILD_TOOL="nerdctl --namespace=k8s.io"
OPEN_LIBERTY_IMAGE_TAG="23.0.0.12-full-java11-openj9-ubi"
PLATFORM="linux/amd64"
BUILD_WITH_MOCK=true

# Create a generic Containerfile to use for all Liberty services
cd ../src/rover/services/liberty
sed -e "s/OPEN_LIBERTY_IMAGE_TAG/$OPEN_LIBERTY_IMAGE_TAG/g" Containerfile.template > Containerfile

# Build liberty
mvn -pl models install
mvn -DskipTests package
$CONTAINER_BUILD_TOOL build --platform=$PLATFORM -t space-rover/leaderboard ./leaderboard -f ./Containerfile
$CONTAINER_BUILD_TOOL build --platform=$PLATFORM -t space-rover/messenger ./messenger -f ./Containerfile
$CONTAINER_BUILD_TOOL build --platform=$PLATFORM -t space-rover/game ./game -f ./Containerfile
if [[ "$BUILD_WITH_MOCK" == "true" ]]; then
    $CONTAINER_BUILD_TOOL build --platform=$PLATFORM -t space-rover/mockboard ./mockboard -f ./Containerfile
    $CONTAINER_BUILD_TOOL build --platform=$PLATFORM -t space-rover/mockrover ./mockrover -f ./Containerfile
fi


# Build client
cd ../
$CONTAINER_BUILD_TOOL build --platform=$PLATFORM -t space-rover/client \
    --build-arg GAME_URL=http://localhost/game-backend/game \
    --build-arg GAME_SOCKET_URL=ws://localhost/rover-backend/ \
    --build-arg GAME_DURATION_SECONDS=120 \
    --build-arg LEADERBOARD_URL=http://localhost/mongo/leaderboard ./client

# Build monitoring
cd ./monitoring
$CONTAINER_BUILD_TOOL build --platform=$PLATFORM -t space-rover/grafana ./grafana
$CONTAINER_BUILD_TOOL build --platform=$PLATFORM -t space-rover/prometheus ./prometheus


