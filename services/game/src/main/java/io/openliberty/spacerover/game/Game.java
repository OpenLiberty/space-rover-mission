/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import io.openliberty.spacerover.game.models.Constants;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Game {
	private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

	private static final int MAX_GAME_TIME_MINUTES = 5;
	protected static final int OBSTACLE_HP_DECREMENT = 10;
	protected static final int OBSTACLE_SCORE_DECREMENT = OBSTACLE_HP_DECREMENT;
	protected static final int OBSTACLE_SUN_HP_DECREMENT = 2 * OBSTACLE_HP_DECREMENT;
	protected static final int OBSTACLE_SUN_SCORE_DECREMENT = OBSTACLE_SCORE_DECREMENT;
	private static final int MAX_GAME_TIME_SECONDS = 60 * MAX_GAME_TIME_MINUTES;
	private String playerId;
	private Instant startTime;
	private Instant endTime;
	private boolean inProgress = false;
	private int score;
	private int health;
	private int startingHealth;
	private GameEventManager eventManager = null;
	private Set<String> coloursVisited;
	private String lastColourVisited;

	public String getLastColourVisited() {
		return lastColourVisited;
	}

	public void setLastColourVisited(String lastColourVisited) {
		this.lastColourVisited = lastColourVisited;
	}

	public Game() {
		this.eventManager = new GameEventManager(GameEvent.HP, GameEvent.SCORE, GameEvent.GAME_OVER, GameEvent.HP_SUN);
	}

	public void startGameSession(String playerId) {
		this.startGameSession(playerId, 100);
	}

	public void startGameSession(String playerId, int maxHP) {
		this.playerId = playerId;
		startTime = Instant.now();
		inProgress = true;
		this.score = 0;
		this.health = maxHP;
		this.startingHealth = this.health;
		this.coloursVisited = new HashSet<>();
	}

	public GameEventManager getEventManager() {
		return eventManager;
	}

	public void decrementHP(int amount) {
		this.health = Math.max(this.health - amount, 0);
		getEventManager().notify(GameEvent.HP, this.health);
	}
	public void decrementHPSun(int amount) {
		this.health = Math.max(this.health - amount, 0);
		getEventManager().notify(GameEvent.HP_SUN, this.health);
	}
	public void decrementScore(int amount) {
		this.score = this.score - amount;
		getEventManager().notify(GameEvent.SCORE, this.score);
	}

	public void incrementScore(int amount) {
		this.score += amount;
		getEventManager().notify(GameEvent.SCORE, this.score);
	}

	public void endGameSession() throws IllegalStateException {
		this.endGameSession(Instant.now());
	}

	public void endGameSession(String gameLengthInMillis) {
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
		currScore.setGameMode(this.getGameMode());
		return currScore;
	}

	public String getGameMode() {
		return Constants.INIT_GAME_CLASSIC;
	}

	public Set<String> getColoursVisited() {
		return coloursVisited;
	}

	public void setColoursVisited(Set<String> coloursVisited) {
		this.coloursVisited = coloursVisited;
	}

	public void processColour(String msgID) {
		this.lastColourVisited = msgID;
		if (msgID.equals(Constants.COLOUR_RED)) {
			this.decrementScore(OBSTACLE_SCORE_DECREMENT);
			this.decrementHP(OBSTACLE_HP_DECREMENT);
		} else if (msgID.equals(Constants.COLOUR_RED_SUN)) {
			this.decrementScore(OBSTACLE_SUN_SCORE_DECREMENT);
			this.decrementHPSun(OBSTACLE_SUN_HP_DECREMENT);
		} else if (!this.coloursVisited.contains(msgID)) {
			LOGGER.log(Level.INFO, "Colour visited: {0}", msgID);
			this.coloursVisited.add(msgID);
			this.incrementScore(getScore());
		}
		if (this.isInProgressGameOver()) {
			this.endGameSession();
		}
	}

	protected int getScore() {
		return this.coloursVisited.size() * 10;
	}

	@Override
	public String toString() {
		return "Game [playerId=" + playerId + ", score=" + score + ", health=" + health + ", eventManager="
				+ eventManager + ", duration=" + this.getGameDuration() + "]";
	}

	public boolean isInProgressGameOver() {
		boolean isOver = false;
		if (this.isInProgress() && (this.health <= 0 || this.coloursVisited.size() == 4)) {
			isOver = true;
		}
		return isOver;
	}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
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

	public int getDamageTaken() {
		return this.startingHealth - this.health;
	}

	public String getCurrentPlanetColour() {
		return this.lastColourVisited;
	}
}
