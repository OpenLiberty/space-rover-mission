package io.openliberty.rover.services.game.model;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Game {
    
    private String playerId;
    private long startTime;
    private long endTime;
    private boolean inProgress = false;

    public void startGameSession(String playerId) {
        this.playerId = playerId;
        startTime = System.currentTimeMillis();
        inProgress = true;
    }

    public void endGameSession() throws IllegalStateException {
        if (inProgress) {
            endTime = System.currentTimeMillis();
            inProgress = false;
        } else {
            throw new IllegalStateException("Game was not started");
        }
    }

    public long getGameDuration() {
        if (startTime > 0) {
            if (endTime > 0) {
                return endTime - startTime;
            } else {
                return System.currentTimeMillis() - startTime;
            }
        } else {
            return 0;
        }
    }
    
    public String getPlayerId() {
        return this.playerId;
    }

}
