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

import java.time.Instant;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import io.openliberty.spacerover.game.models.GameEvent;
import io.openliberty.spacerover.game.models.Constants;

public class SpaceHop extends Game {
	private static final int SCORE_INCREMENT = 10;
	private static final long MAX_TIMEOUT_SECONDS = 15 * 1000L;
	private String currentColour;
	private Random r;
	private Timer gameTimer;
	private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

	public SpaceHop() {
		super();
		this.getEventManager().addOperations(GameEvent.FIVE_SECONDS_LEFT, GameEvent.PLANET_CHANGED);
	}

	@Override
	public void startGameSession(String playerId) {
		this.startGameSession(playerId, 100);
	}

	@Override
	public void startGameSession(String playerId, int maxHP) {
		super.startGameSession(playerId, maxHP);
		this.gameTimer = new Timer();
		this.r = new Random();
		this.chooseNextPlanet();
	}

	private void chooseNextPlanet() {
		String prevColour = this.getCurrentPlanetColour();
		String newColour = prevColour;
		if (newColour == null) {
			newColour = Constants.COLOURS_EXCLUDING_RED[r.nextInt(Constants.COLOURS_EXCLUDING_RED.length)];
		} else {

			while (newColour.equals(prevColour)) {
				newColour = Constants.COLOURS_EXCLUDING_RED[r.nextInt(Constants.COLOURS_EXCLUDING_RED.length)];
			}
		}
		this.setCurrentColour(newColour);
		this.getEventManager().notify(GameEvent.PLANET_CHANGED, 0);
		this.gameTimer.cancel();
		// maybe we should keep a list of tasks being scheduled and cancel those instead
		// of recreating the timer object.
		// it seems odd that the Timer object doesn't have a way to get the currently
		// scheduled tasks.
		this.gameTimer = new Timer();
		this.gameTimer.schedule(getFiveSecondWarningTask(), MAX_TIMEOUT_SECONDS - 5000);
		this.gameTimer.schedule(getTimeoutTask(), MAX_TIMEOUT_SECONDS);
	}

	private TimerTask getTimeoutTask() {
		return new TimerTask() {

			@Override
			public void run() {
				chooseNextPlanet();
			}
		};
	}

	private TimerTask getFiveSecondWarningTask() {
		return new TimerTask() {

			@Override
			public void run() {
				sendFiveSecondWarning();
			}
		};
	}

	public void sendFiveSecondWarning() {
		this.getEventManager().notify(GameEvent.FIVE_SECONDS_LEFT, 0);
	}

	@Override
	public String getCurrentPlanetColour() {
		return currentColour;
	}

	public void setCurrentColour(String currentColour) {
		this.currentColour = currentColour;
	}

	@Override
	public boolean isInProgressGameOver() {
		boolean isOver = false;
		if (this.isInProgress() && (this.getHealth() <= 0)) {
			isOver = true;
		}
		return isOver;
	}

	@Override
	public void processColour(String msgID) {
		LOGGER.log(Level.INFO, "Colour visited: {0}", msgID);

		if (msgID.equals(Constants.COLOUR_RED)) {
			this.decrementScore(OBSTACLE_SCORE_DECREMENT);
			this.decrementHP(OBSTACLE_HP_DECREMENT);
		} else if (msgID.equals(Constants.COLOUR_RED_SUN)) {
			this.decrementScore(OBSTACLE_SUN_SCORE_DECREMENT);
			this.decrementHPSun(OBSTACLE_SUN_HP_DECREMENT);
		} else if (msgID.equals(getCurrentPlanetColour())) {
			this.incrementScore(SCORE_INCREMENT);
			this.chooseNextPlanet();
		}
		if (this.isInProgressGameOver()) {
			this.endGameSession();
		}
	}

	@Override
	public String getGameMode() {
		return Constants.INIT_GAME_HOP;
	}

	@Override
	public void endGameSession(Instant inputEndTime) {
		super.endGameSession(inputEndTime);
		this.gameTimer.cancel();
	}
}
