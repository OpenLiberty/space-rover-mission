#!/bin/bash

./mvnw install
#docker-compose up --build
x#docker-compose up > leaderboard.log 2>&1 &
exit
