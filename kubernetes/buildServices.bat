@echo off
:: Build all the images

:: client
START /B /WAIT docker build -t space-rover/client ^
--build-arg GAME_URL=http://localhost:9080/game ^
--build-arg GAME_SOCKET_URL=ws://localhost:9080/roversocket ^
--build-arg GAME_DURATION_SECONDS=120 ^
--build-arg LEADERBOARD_URL=http://localhost:9070/mongo/leaderboard ../services/client

:: gameservice
START /B /WAIT docker build -t space-rover/gameservice ../services/game

:: leaderboard
START /B /WAIT docker build -t space-rover/leaderboard ../services/leaderboard

:: mockboard (if applicable)
START /B /WAIT docker build -t space-rover/mockboard ../services/mock/board

:: mockrover (if applicable)
START /B /WAIT docker build -t space-rover/mockrover ../services/mock/rover

:: mongo
START /B /WAIT  docker build -t space-rover/mongo ../services/leaderboard/assets

:: prometheus 
START /B /WAIT docker build -t space-rover/prometheus ../services/prometheus

:: grafana
START /B /WAIT docker build -t space-rover/grafana ../services/grafana

echo Your Images are Complete!
echo You can now apply your kubernetes files.
