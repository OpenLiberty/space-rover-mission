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
package io.openliberty.spacerover.game;

import java.time.Duration;
import java.time.Instant;

import io.openliberty.spacerover.game.models.GameEvent;
import io.openliberty.spacerover.game.models.GameScore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Game {

	private String playerId;
	private Instant startTime;
	private Instant endTime;
	private boolean inProgress = false;
	private long score;
	private long health;
	private GameEventManager eventManager = null;

	public void startGameSession(String playerId) {
		this.startGameSession(playerId, 100);
	}

	public void startGameSession(String playerId, long maxHP) {
		this.playerId = playerId;
		startTime = Instant.now();
		inProgress = true;
		this.score = 0;
		this.health = maxHP;
		this.eventManager = new GameEventManager(GameEvent.HP, GameEvent.SCORE);

	}

	public GameEventManager getEventManager() {
		return eventManager;
	}

	public void decrementHP(long amount) {
		this.health = Math.max(this.health - amount, 0);
		getEventManager().notify(GameEvent.HP, this.health);
	}

	public void decrementScore(long amount) {
		this.score = Math.max(this.score - amount, 0);
		getEventManager().notify(GameEvent.SCORE, this.score);
	}

	public void incrementScore(long amount) {
		this.score += amount;
		getEventManager().notify(GameEvent.SCORE, this.score);
	}

	public void endGameSession() throws IllegalStateException {
		if (inProgress) {
			endTime = Instant.now();
			inProgress = false;
		} else {
			throw new IllegalStateException("Game was not started");
		}
	}

	public long getGameDuration() {
		return Duration.between(startTime, endTime).toSeconds();
	}

	public String getPlayerId() {
		return this.playerId;
	}

	public boolean isInProgress() {
		return inProgress;
	}

    public GameScore getGameLeaderboardStat() {
        GameScore currScore = new GameScore();
		currScore.setPlayer(this.playerId);
		currScore.setScore(this.score);
		currScore.setTime(getGameDuration());
		return currScore;
    }

}
