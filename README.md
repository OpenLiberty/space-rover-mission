# Open Liberty Space Rover Rescue Mission

## Prerequisites
1. Space Rover hardware component with the project code compiled and running (TODO code found where?)
2. Game Board hardware component with the project code compiled and running (TODO code found where?)
3. Gesture control system client with webcam facing a defined area for users to control the rover. (TODO code found where?)
4. Docker CE or Docker Desktop either running on the Gesture control system hardware or on it's own hardware. 

## Setup
1. Plugin router with preconfigured IP addresses assigned for Space Rover and Game Board
  1. IP's are defined in https://github.com/OpenLiberty/space-rover-mission/blob/5fbd547088748b798bcb049e2e151f1f2b180daf/server/src/main/webapp/META-INF/microprofile-config.properties#L5-L8
2. Set up and connect gameboard (TODO using guide from Ellen?)
3. Place Space Rover hardware in starting location (Behind Earth) on the gameboard and turn it on
4. Go to the git project root and execute `docker-compose up --build` to build all the dockers with the cluster
