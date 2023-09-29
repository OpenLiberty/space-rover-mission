/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.spacerover.game.websocket.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;
import org.eclipse.microprofile.metrics.annotation.SimplyTimed;

import io.openliberty.spacerover.game.models.Constants;
import io.openliberty.spacerover.game.websocket.client.WebsocketClientEndpoint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@Liveness
@ApplicationScoped
public class GameServerHealth implements HealthCheck {
    private static final Logger LOGGER = Logger.getLogger(GameServerHealth.class.getName());

    public static final long HEALTH_CHECK_TIMEOUT_SECONDS = 20;

    @Inject
    @ConfigProperty(name = "io.openliberty.server.port", defaultValue = "9080")
    String serverPort;

    @Inject
    @ConfigProperty(name = "io.openliberty.server.hostname", defaultValue = "gameservice")
    String serverHost;

    @Override
    @SimplyTimed(name="heartbeat", displayName = "Heartbeat", description = "provides a heart beat latency timer")
    public HealthCheckResponse call() {
        HealthCheckResponse resp;
        if (!isHealthy()) {
            resp = HealthCheckResponse.named(GameServerHealth.class.getSimpleName())
                    .withData("GameServerHealth", "not available").down()
                    .build();
            LOGGER.warning("GameHealth check failed.");
        } else {
            resp = HealthCheckResponse.named(GameServerHealth.class.getSimpleName())
                    .withData("GameServerHealth", "available").up().build();
            LOGGER.info("GameHealth check passed.");
        }
        return resp;
    }
    
    private boolean isHealthy() {
        boolean isHealthy = false;
        try {
            CountDownLatch latch = new CountDownLatch(1);
            final URI uri = new URI("ws", null, serverHost, Integer.parseInt(serverPort),
                    "/" + GameServerConstants.WEBSOCKET_ENDPOINT, null, null);        
            WebsocketClientEndpoint client = new WebsocketClientEndpoint(uri);

            client.addMessageHandler(e -> {
                if (e.equals(Constants.GAME_HEALTH_ACK)) {
                    latch.countDown();
                }
            });
            client.connect();
            client.sendMessage(Constants.GAME_HEALTH_TEST);
            isHealthy = latch.await(HEALTH_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch ( IOException | InterruptedException | NumberFormatException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "isHealthy() failed.", e);
            Thread.currentThread().interrupt();
        }
        return isHealthy;
    }

}
