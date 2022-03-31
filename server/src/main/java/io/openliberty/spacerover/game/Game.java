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
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.openliberty.spacerover.game.models.GameEvent;
import io.openliberty.spacerover.game.models.GameScore;
import io.openliberty.spacerover.game.models.SocketMessages;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Game {
	private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

	private static final int MAX_GAME_TIME_MINUTES = 5;
	private static final int SCORE_INCREMENT = 10;
	private static final int OBSTACLE_DMG = 10;
	private static final int MAX_GAME_TIME_SECONDS = 60 * MAX_GAME_TIME_MINUTES;
	private String playerId;
	private Instant startTime;
	private Instant endTime;
	private boolean inProgress = false;
	private int score;
	private int health;
	private GameEventManager eventManager = null;
	private Set<String> coloursVisited;

	public void startGameSession(String playerId) {
		this.startGameSession(playerId, 100);
	}

	public void startGameSession(String playerId, int maxHP) {
		this.playerId = playerId;
		startTime = Instant.now();
		inProgress = true;
		this.score = 0;
		this.health = maxHP;
		this.eventManager = new GameEventManager(GameEvent.HP, GameEvent.SCORE, GameEvent.GAME_OVER);
		this.coloursVisited = new HashSet<>();
	}

	public GameEventManager getEventManager() {
		return eventManager;
	}

	public void decrementHP(int amount) {
		this.health = Math.max(this.health - amount, 0);
		getEventManager().notify(GameEvent.HP, this.health);
	}

	public void decrementScore(int amount) {
		this.score = Math.max(this.score - amount, 0);
		getEventManager().notify(GameEvent.SCORE, this.score);
	}

	public void incrementScore(int amount) {
		this.score += amount;
		getEventManager().notify(GameEvent.SCORE, this.score);
	}

	public void endGameSession() throws IllegalStateException {
		this.endGameSession(Instant.now());
	}
	public void endGameSession(String gameLengthInMillis)
	{
		long seconds = Long.parseLong(gameLengthInMillis);
		this.endGameSession(this.startTime.plus(seconds, ChronoUnit.SECONDS));
	}

	public long getGameDuration() {
		long durationInSeconds;
		if (startTime != null && endTime != null) {
			durationInSeconds = Duration.between(startTime, endTime).toSeconds();
		} else {
			if (this.isInProgress()) {
				durationInSeconds = Duration.between(startTime, Instant.now()).toSeconds();
			} else {
				durationInSeconds = 0;
			}
		}
		return Math.min(durationInSeconds, MAX_GAME_TIME_SECONDS);
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
		currScore.setHealth(this.health);
		return currScore;
	}

	public void processColour(String msgID) {
		if (msgID.equals(SocketMessages.COLOUR_RED)) {
			this.decrementHP(OBSTACLE_DMG);
		} else {
			if (!this.coloursVisited.contains(msgID)) {
				LOGGER.log(Level.INFO, "New colour visited: {0}", msgID);
				this.coloursVisited.add(msgID);
				this.incrementScore(SCORE_INCREMENT);
			}
		}
		if (this.isInProgressGameOver()) {
			this.endGameSession();
		}
	}

	@Override
	public String toString() {
		return "Game [playerId=" + playerId + ", score=" + score + ", health=" + health + ", eventManager="
				+ eventManager + ", duration=" + this.getGameDuration() + "]";
	}

	public boolean isInProgressGameOver() {
		boolean isOver = false;
		if (this.health <= 0 || this.coloursVisited.size() == 4) {
			isOver = true;
		}
		return isOver;
	}

	public void endGameSession(Instant inputEndTime) {
		if (inProgress) {
			this.endTime = inputEndTime;
			inProgress = false;
			getEventManager().notify(GameEvent.GAME_OVER, 0);
		} else {
			throw new IllegalStateException("Game was not started");
		}
	}

}
