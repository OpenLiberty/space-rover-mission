FROM maven AS builder

WORKDIR /usr/src/app

COPY pom.xml .

RUN mvn org.apache.maven.plugins:maven-dependency-plugin:3.3.0:go-offline

COPY src src

RUN mvn -o package

FROM icr.io/appcafe/open-liberty:23.0.0.3-full-java11-openj9-ubi

COPY --chown=1001:0 --from=builder /usr/src/app/target/*.war /config/apps

COPY --chown=1001:0 --from=builder /usr/src/app/src/main/liberty/config/server.xml /config/

COPY --chown=1001:0 --from=builder /usr/src/app/src/main/liberty/config/bootstrap.properties /config/

RUN configure.sh
