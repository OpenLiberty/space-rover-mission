FROM icr.io/appcafe/open-liberty:23.0.0.3-full-java11-openj9-ubi

COPY --chown=1001:0 src/main/liberty/config /config/
COPY --chown=1001:0 target/messenger.war /config/apps

RUN configure.sh

EXPOSE 9085 9448