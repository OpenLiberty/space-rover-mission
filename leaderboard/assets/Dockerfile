FROM mongo:latest

# Create the directories.
RUN mkdir /home/mongodb
RUN mkdir /home/mongodb/data
RUN mkdir /home/mongodb/certs
RUN mkdir /home/mongodb/logs
RUN chown -R mongodb:mongodb /home/mongodb

# Copy the configuration and setup files from
# the host machine to the image.
COPY assets/mongodb.conf /home/mongodb
COPY assets/index.js /home/mongodb

# Run MongodDB daemon and execute the setup script.
RUN mongod \
        --fork \
        --config /home/mongodb/mongodb.conf \
    && mongo \
        spaceDB \
        /home/mongodb/index.js \
    && mongod --dbpath /home/mongodb/data --shutdown \
    && chown -R mongodb /home/mongodb

# Start MongoDB daemon when the image is run in a container.
CMD ["mongod", "--config", "/home/mongodb/mongodb.conf"]
