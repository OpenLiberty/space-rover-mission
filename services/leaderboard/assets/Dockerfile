FROM mongo:4.4.6

COPY --chown=1001:0 ./entrypoint.js /docker-entrypoint-initdb.d/

RUN mongod &
