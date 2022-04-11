# Services

This folder contains the services required to play the game including the Client, Game Service, Leaderboard Service, MongoDB, Prometheus server, and Grafana. 

These services are run in Docker containers and can be started by running `docker-compose up` within this folder. Include the `--build` flag to ensure that the images are rebuilt before running them in containers.

To tear down the services, run `docker-compose down` within this folder.