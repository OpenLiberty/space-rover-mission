# Game Service

The Game Service is built using Open Liberty. It holds the current game state and is the piece that connects all the different components together. It gets information from the hardware devices using WebSockets and sends events to the Client to update the health and score. It also updates the Leaderboard Service about the game information at the end of a game.

## WebSocket Endpoint

`/roversocket`: The endpoint to connect to the game websocket using the `ws` protocol

## Jakarta EE and MicroProfile features

### Jakarta EE 9.1
- [WebSocket 2.0](https://jakarta.ee/specifications/websocket/)
    - Used extensively to handle socket connections from the GUI, Gesture Control Service, Game Board, and Space Rover.

### MicroProfile 5.0
- [MP Health 4.0](https://download.eclipse.org/microprofile/microprofile-health-4.0/microprofile-health-spec-4.0.html)
    - Used to add a /health endpoint which returns the server status based on its ability to handle messages within a timeout.
- [MP Config 3.0](https://download.eclipse.org/microprofile/microprofile-config-3.0/microprofile-config-spec-3.0.html)
    - Used to configure known IP addresses and ports for external connections to the Space Rover, Game Board and Leaderboard.
- [MP Metrics 3.0](https://download.eclipse.org/microprofile/microprofile-metrics-3.0/microprofile-metrics-spec-3.0.html)
    - Used to record JVM metrics on the game service as well as a SimpleTimer that tracks the amount of time it takes for the GameService to respond to the health check test. This tells us a local round trip latency value determining how long it takes a message to be sent to the server and have the server respond.
- [MP Rest Client 3.0](https://download.eclipse.org/microprofile/microprofile-rest-client-3.0/microprofile-rest-client-spec-3.0.html)
    - Generates an HTTP client to send game end statistics to the leaderboard's REST server.
- [MP OpenAPI 3.0](https://download.eclipse.org/microprofile/microprofile-open-api-3.0/microprofile-openapi-spec-3.0.html)
	- Used for providing REST API documentation and UI for demonstration.