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
import io.openliberty.spacerover.game.models.SocketMessages;
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
    @ConfigProperty(name = "io.openliberty.server.hostname", defaultValue = "server")
    String serverHost;

    @Override
    public HealthCheckResponse call() {
        HealthCheckResponse resp;
        if (!isHealthy()) {
            resp = HealthCheckResponse.named(GameServerHealth.class.getSimpleName())
                    .withData("GameHealth", "not available").down()
                    .build();
            LOGGER.warning("GameHealth check failed.");
        } else {
            resp = HealthCheckResponse.named(GameServerHealth.class.getSimpleName())
                    .withData("GameHealth", "available").up().build();
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
                if (e.equals(SocketMessages.GAME_HEALTH_ACK)) {
                    latch.countDown();
                }
            });

            client.sendMessage(SocketMessages.GAME_HEALTH_TEST);
            isHealthy = latch.await(HEALTH_CHECK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException | IOException | URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "isHealthy() failed.", e);
        }
        return isHealthy;
    }

}
