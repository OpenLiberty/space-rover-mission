/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.rover.services.game.model;

import jakarta.enterprise.context.ApplicationScoped;

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
