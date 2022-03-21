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

import io.openliberty.spacerover.game.models.GameEvent;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Game {

	private String playerId;
	private long startTime;
	private long endTime;
	private boolean inProgress = false;
	private long score;
	private long health;
	private GameEventManager eventManager = null;

	public void startGameSession(String playerId) {
		this.startGameSession(playerId, 100);
	}

	public void startGameSession(String playerId, long maxHP) {
		this.playerId = playerId;
		startTime = System.currentTimeMillis();
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

	public boolean isInProgress() {
		return inProgress;
	}

}
