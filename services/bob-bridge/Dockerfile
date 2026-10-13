# bob-bridge — Bob Shell + script relay
#
# Runs Bob Shell directly inside this container.
# Serves GET /scripts (list all scripts) and POST /generate (run Bob Shell).
# No host-side agent or Unix socket needed.

FROM node:22-slim

WORKDIR /app

# Install ca-certificates first so curl can verify SSL, then install Bob Shell.
# --pm npm skips the interactive package manager selection (no TTY in docker build).
RUN apt-get update && \
    apt-get install -y --no-install-recommends ca-certificates curl bash && \
    rm -rf /var/lib/apt/lists/* && \
    curl -fsSL https://bob.ibm.com/download/bobshell.sh > /tmp/bobshell-install.sh && \
    bash /tmp/bobshell-install.sh --pm npm && \
    rm /tmp/bobshell-install.sh

COPY package.json ./
RUN npm install --omit=dev

COPY server.js ./

# Run as root so Bob Shell can write generated scripts to the host-mounted
# specs/scripts/ volume (owned by root on the host).
# Note: this is an internal sidecar not exposed to the internet.

EXPOSE 4000

ENV BOB_BRIDGE_PORT=4000
ENV SCRIPTS_DIR=/workspace/specs/scripts

CMD ["node", "server.js"]
