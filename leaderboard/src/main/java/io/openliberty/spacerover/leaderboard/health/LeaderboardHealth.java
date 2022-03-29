package io.openliberty.spacerover.leaderboard.health;
import java.util.logging.Logger;

import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import io.openliberty.spacerover.leaderboard.rest.Leaderboard;
import jakarta.enterprise.context.ApplicationScoped;

@Liveness
@ApplicationScoped
public class LeaderboardHealth implements HealthCheck{
    private static final Logger LOGGER = Logger.getLogger(LeaderboardHealth.class.getName());

	@Override
    public HealthCheckResponse call() {
        HealthCheckResponse resp;
        if (!isHealthy()) {
            resp = HealthCheckResponse.named(LeaderboardHealth.class.getSimpleName())
                    .withData("GameServerHealth", "not available").down()
                    .build();
            LOGGER.warning("GameHealth check failed.");
        } else {
            resp = HealthCheckResponse.named(LeaderboardHealth.class.getSimpleName())
                    .withData("GameServerHealth", "available").up().build();
            LOGGER.info("GameHealth check passed.");
        }
        return resp;
    }
    
    private boolean isHealthy() {
        boolean isHealthy = true;
        
        Leaderboard board = new Leaderboard();
        if(board.retrieve().getStatus() != 200)
        {
        	isHealthy = false;
        }
        return isHealthy;
    }


}
