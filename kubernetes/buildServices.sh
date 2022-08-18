# Build all the images
# client
cd ../services/client
docker build -t space-rover/client ^
--build-arg GAME_URL=http://localhost:9070/game ^
--build-arg GAME_SOCKET_URL=ws://localhost:9070/roversocket ^
--build-arg GAME_DURATION_SECONDS=120 ^
--build-arg LEADERBOARD_URL=http://localhost:9080/mongo/leaderboard .

# gameservice
cd ../game
docker build -t space-rover/gameservice .

# leaderboard
cd ../leaderboard
docker build -t space-rover/leaderboard .

# mockboard (if applicable)
cd ../mock/board
docker build -t space-rover/mockboard .

# mockrover (if applicable)
cd ../rover
docker build -t space-rover/rover .

# mongo
cd ../../services/leaderboard/assets
docker build -t space-rover/mongo .

# prometheus 
cd ../../prometheus
docker build -t space-rover/prometheus .

# grafana
cd ../grafana
docker build -t space-rover/grafana .

cd ..
echo Your Images are Complete!
echo You can now apply your kubernetes files.