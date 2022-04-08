# Open Liberty Space Rover Mission

Open Liberty Space Rover Mission is an interactive IoT game designed to showcase microservices with modern Java technologies in a fun way. The mission is to guide the Space Rover using hand gestures to its home while visiting planets along the way to gather supplies. Make sure to avoid any obstacles (asteroids, black-hole, etc.) in your path.

![GameBoard](/images/gameboard.jpg)

## Prerequisites
1. [Space Rover hardware component](https://github.com/OpenLiberty/space-rover-mission/tree/main/devices/space-rover) with the project code compiled and running.
2. [Game Board hardware component](https://github.com/OpenLiberty/space-rover-mission/tree/main/devices/game-board) with the project code compiled and running.
3. [Gesture Control System client](https://github.com/OpenLiberty/space-rover-mission/tree/main/gestures) with webcam facing a defined area for users to control the Space Rover. (TODO code found where?)
4. Docker CE or [Docker Desktop](https://www.docker.com/products/docker-desktop/) either running on the Gesture Control System's hardware or on its own hardware. 

## Setup, Cleanup, and Troubleshooting
For setup, cleanup, and troubleshooting instructions, see [documentation](https://github.com/OpenLiberty/space-rover-mission/tree/main/documentation).

## Architecture Diagram

![ArchitectureDiagram](/images/architecture.png)

## Jakarta EE and MicroProfile features used

### Docker Container 2 – Leaderboard service

#### Jakarta EE 9.1
- [RESTful Web Services (JAX-RS) 3.0](https://jakarta.ee/specifications/restful-ws/)
    -	Used REST endpoints for interfacing with the leaderboard.
    -	HTTP GET endpoint used to retrieve stats from MongoDB container.
    -	HTTP POST to update MongoDB with new statistics.
- [Context Dependency Injection (CDI) 3.0](https://jakarta.ee/specifications/cdi/3.0/)
    - Used to inject a MongoDatabase object and MongoClient used to interact with the MongoDB leaderboard database.

#### MicroProfile 5.0
- [MP Health 4.0](https://download.eclipse.org/microprofile/microprofile-health-4.0/microprofile-health-spec-4.0.html)
    -	Used to add a /health endpoint that determines if MongoDB is up and running.
- [MP Config 3.0](https://download.eclipse.org/microprofile/microprofile-config-3.0/microprofile-config-spec-3.0.html)
    - Used to store connection information to MongoDB such as the username, port, and encrypted password.

### Docker Container 3 – Game service

#### Jakarta EE 9.1
- [WebSocket 2.0](https://jakarta.ee/specifications/websocket/)
    - Used extensively to handle socket connections from the GUI and Gesture Control Service.

#### MicroProfile 5.0
- [MP Health 4.0](https://download.eclipse.org/microprofile/microprofile-health-4.0/microprofile-health-spec-4.0.html)
    - Used to add a /health endpoint which returns the server status based on its ability to handle messages within a timeout.
- [MP Config 3.0](https://download.eclipse.org/microprofile/microprofile-config-3.0/microprofile-config-spec-3.0.html)
    - Used to configure known IP addresses and ports for external connections to the Space Rover, Game Board and Leaderboard.
- [MP Metrics 3.0](https://download.eclipse.org/microprofile/microprofile-metrics-3.0/microprofile-metrics-spec-3.0.html)
    - Used to record JVM metrics on the game service as well as a SimpleTimer that tracks the amount of time it takes for the GameService to respond to the health check test. This tells us a local round trip latency value determining how long it takes a message to be sent to the server and have the server respond.
- [MP Rest Client 3.0](https://download.eclipse.org/microprofile/microprofile-rest-client-3.0/microprofile-rest-client-spec-3.0.html)
    - Generates an HTTP client to send game end statistics to the leaderboard's REST server.
	
