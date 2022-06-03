# Leaderboard Service

The Leaderboard Service is built using Open Liberty. It is used to provide a REST API for the Client to query for past game information and for the Game Service to add the information after a game completes. To persist this data, the Leaderboard Service interacts with MongoDB, a NoSQL database.

## Models

LeaderboardEntry
```
{
  _id: ObjectId
  player: String
  score: int
  time: int
  health: int
  timestamp: String
}
```

## Endpoints

`POST /mongo/leaderboard`: Add a new leaderboard entry

`GET /mongo/leaderboard`: Get all leaderboard entries

## Jakarta EE and MicroProfile features

### Jakarta EE 9.1
- [RESTful Web Services (JAX-RS) 3.0](https://jakarta.ee/specifications/restful-ws/)
    -	Used REST endpoints for interfacing with the leaderboard.
    -	HTTP GET endpoint used to retrieve stats from MongoDB container.
    -	HTTP POST to update MongoDB with new statistics.
- [Context Dependency Injection (CDI) 3.0](https://jakarta.ee/specifications/cdi/3.0/)
    - Used to inject a MongoDatabase object and MongoClient used to interact with the MongoDB leaderboard database.

### MicroProfile 5.0
- [MP Health 4.0](https://download.eclipse.org/microprofile/microprofile-health-4.0/microprofile-health-spec-4.0.html)
    -	Used to add a /health endpoint that determines if MongoDB is up and running.
- [MP Config 3.0](https://download.eclipse.org/microprofile/microprofile-config-3.0/microprofile-config-spec-3.0.html)
    - Used to store connection information to MongoDB such as the username, port, and encrypted password.
- [MP FaultTolerance 4.0](https://download.eclipse.org/microprofile/microprofile-fault-tolerance-4.0/microprofile-fault-tolerance-spec-4.0.html)
	- Used to retry connection attempts to MongoDB if they are not successful. 
- [MP OpenAPI 3.0](https://download.eclipse.org/microprofile/microprofile-open-api-3.0/microprofile-openapi-spec-3.0.html)
	- Used for providing REST API documentation and UI for demonstration.