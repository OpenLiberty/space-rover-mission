FROM mongodb/mongodb-community-server

COPY --chown=1001:0 ./entrypoint.js /docker-entrypoint-initdb.d/

RUN mongod &
