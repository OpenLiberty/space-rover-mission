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

import io.openliberty.spacerover.game.models.Constants;
import io.openliberty.spacerover.game.websocket.server.GameServer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class GameServerStateMachine {
	private static final Logger LOGGER = Logger.getLogger(GameServerStateMachine.class.getName());
	GameServerState currentState;

	public GameServerStateMachine() {
		this(GameServerState.SERVER_STARTED);
	}
	
	public GameServerStateMachine(GameServerState startingPoint) {
		this.currentState = startingPoint;
	}

	public void incrementState(String msgID) {
		GameServerState beforeState = this.currentState;
		switch (msgID) {
			case Constants.CONNECT_GUI:
				if (this.currentState == GameServerState.SERVER_STARTED
						|| this.currentState == GameServerState.ERROR_OCCURRED) {
					this.currentState = GameServerState.GUI_CONNECTED;
				} else {
					this.currentState = GameServerState.GUI_AND_GESTURE_CONNECTED;
				}
				break;
			case Constants.CONNECT_GESTURE:
				if (this.currentState == GameServerState.SERVER_STARTED
						|| this.currentState == GameServerState.ERROR_OCCURRED) {
					this.currentState = GameServerState.GESTURE_CONNECTED;
				} else {
					this.currentState = GameServerState.GUI_AND_GESTURE_CONNECTED;
				}
				break;

			case Constants.ROVER_ACK:
				this.currentState = GameServerState.ROVER_CONNECTED;
				break;
			case Constants.GAMEBOARD_ACK:
				this.currentState = GameServerState.ALL_CONNECTED;
				break;
			case Constants.START_GAME:
				this.currentState = GameServerState.GAME_STARTED;
				break;
			case Constants.END_GAME:
				this.currentState = GameServerState.GUI_AND_GESTURE_CONNECTED;
				break;
			default:
				break;
		}
		logStateChange(beforeState, this.currentState);
	}

	public boolean isValidState(String msgID) {
		boolean isValid = true;
		if (msgID.equals(Constants.CONNECT_GUI)) {
			if (this.currentState != GameServerState.SERVER_STARTED
					&& this.currentState != GameServerState.GESTURE_CONNECTED
					&& this.currentState != GameServerState.ERROR_OCCURRED) {
				isValid = false;
			}
		} else if (msgID.equals(Constants.CONNECT_GESTURE)) {
			if (this.currentState != GameServerState.SERVER_STARTED
					&& this.currentState != GameServerState.GUI_CONNECTED
					&& this.currentState != GameServerState.ERROR_OCCURRED) {
				isValid = false;
			}
		} else if (msgID.equals(Constants.ROVER_ACK)) {
			if (this.currentState != GameServerState.ROVER_CONNECT_TEST) {
				isValid = false;
			}

		} else if (msgID.equals(Constants.GAMEBOARD_ACK)) {
			if (this.currentState != GameServerState.GAMEBOARD_CONNECT_TEST) {
				isValid = false;
			}

		} else if (msgID.equals(Constants.START_GAME)) {
			if (this.currentState != GameServerState.ALL_CONNECTED && this.currentState != GameServerState.GAME_ENDED) {
				isValid = false;
			}
		} else if (msgID.equals(Constants.END_GAME)) {
			if (this.currentState != GameServerState.GAME_STARTED) {
				isValid = false;
			}
		} else if (GameServer.isDirection(msgID) && this.currentState != GameServerState.GAME_STARTED) {
			isValid = false;
		}

		if (!isValid) {
			LOGGER.log(Level.SEVERE, "{0} recieved during invalid server state {1}",
					new Object[] { msgID, this.currentState });
		}
		return isValid;
	}

	public void attachRover() {
		if (this.currentState != GameServerState.LEADERBOARD_CONNECTED) {
			throw new RuntimeException("AttachRover recieved during invalid server state " + this.currentState);
		}
		this.currentState = GameServerState.ROVER_CONNECT_TEST;
		logStateChange(GameServerState.LEADERBOARD_CONNECTED, this.currentState);
	}

	public void attachGameBoard() {
		if (this.currentState != GameServerState.ROVER_CONNECTED) {
			throw new RuntimeException("attachGameBoard recieved during invalid server state " + this.currentState);
		}
		this.currentState = GameServerState.GAMEBOARD_CONNECT_TEST;
		logStateChange(GameServerState.ROVER_CONNECTED, this.currentState);
	}

	public void attachLeaderboard() {
		if (this.currentState != GameServerState.GUI_AND_GESTURE_CONNECTED) {
			throw new RuntimeException("attachLeaderboard recieved during invalid server state " + this.currentState);
		}
		this.currentState = GameServerState.LEADERBOARD_CONNECTED;
		logStateChange(GameServerState.GUI_AND_GESTURE_CONNECTED, this.currentState);
	}

	public GameServerState getCurrentState() {
		return this.currentState;
	}

	public boolean isReadyToConnectGamePieces() {
		return this.getCurrentState() == GameServerState.GUI_AND_GESTURE_CONNECTED;
	}

	public void setErrorState() {
		GameServerState oldState = this.currentState;
		this.currentState = GameServerState.ERROR_OCCURRED;
		logStateChange(oldState, this.currentState);

	}

	private void logStateChange(GameServerState oldState, GameServerState newState) {
		if (oldState != newState) {
			LOGGER.log(Level.INFO, "STATECHANGE: From {0} to {1}", new Object[] { oldState, newState });
		}
	}

	public boolean hasErrorOccurred() {
		return this.currentState == GameServerState.ERROR_OCCURRED;
	}

	public boolean isAllConnected() {
		return this.currentState == GameServerState.ALL_CONNECTED;
	}

	public boolean isReadyToConnectBoard() {

		return this.currentState == GameServerState.ROVER_CONNECTED;
	}

	public boolean isReadyToConnectRover() {
		return this.currentState == GameServerState.LEADERBOARD_CONNECTED;
	}
}
