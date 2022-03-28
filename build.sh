#!/bin/bash
cd server
./mvnw install -DskipTests=true
cd ../leaderboard
./mvnw install 
cd ..
docker-compose down
docker-compose up --build

