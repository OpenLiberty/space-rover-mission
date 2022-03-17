package io.openliberty.spacerover.game.websocket.server;

import io.openliberty.spacerover.game.Messages;
import java.util.logging.Logger;
public class GameServerStateMachine {
    private static final Logger LOGGER = Logger.getLogger(GameServerStateMachine.class.getName());
	GameServerState currentState;

	public GameServerStateMachine() {
		this.currentState = GameServerState.SERVER_STARTED;
	}

	protected void incrementState(String msgID) {
		GameServerState beforeState = this.currentState;
		switch (msgID) {
		case Messages.CONNECT_GUI:
			if (this.currentState == GameServerState.SERVER_STARTED) {
				this.currentState = GameServerState.GUI_CONNECTED;
			} else {
				this.currentState = GameServerState.GUI_AND_GESTURE_CONNECTED;
			}
			break;
		case Messages.CONNECT_GESTURE:
			if (this.currentState == GameServerState.SERVER_STARTED) {
				this.currentState = GameServerState.GESTURE_CONNECTED;
			} else {
				this.currentState = GameServerState.GUI_AND_GESTURE_CONNECTED;
			}
			break;

		case Messages.ROVER_TEST:
			this.currentState = GameServerState.ROVER_CONNECTED;
			break;
		case Messages.GAMEBOARD_TEST:
			this.currentState = GameServerState.GAMEBOARD_CONNECTED;
			break;
		case Messages.START_GAME:
			this.currentState = GameServerState.GAME_STARTED;
			break;
		case Messages.END_GAME:
			this.currentState = GameServerState.SERVER_STARTED;
			break;
		default:
			LOGGER.severe("Unexpected msgID " + msgID + " during state " + beforeState);
		}

		if (msgID.equals(Messages.CONNECT_GUI)) {

		} else if (msgID.equals(Messages.CONNECT_GESTURE)) {

		} else if (msgID.equals(Messages.ROVER_TEST)) {
			this.currentState = GameServerState.ROVER_CONNECTED;
		} else if (msgID.equals(Messages.GAMEBOARD_TEST)) {
			this.currentState = GameServerState.GAMEBOARD_CONNECTED;
		} else if (msgID.equals(Messages.START_GAME)) {
			this.currentState = GameServerState.GAME_STARTED;
		} else if (msgID.equals(Messages.END_GAME)) {
			this.currentState = GameServerState.SERVER_STARTED;
		}
		LOGGER.info("Change state from " + beforeState + " to " + this.currentState);
	}

	protected boolean isValidState(String msgID) {
		boolean isValid = true;
		if (msgID.equals(Messages.CONNECT_GUI)) {
			if (this.currentState != GameServerState.SERVER_STARTED
					&& this.currentState != GameServerState.GESTURE_CONNECTED) {
				isValid = false;
			}
		} else if (msgID.equals(Messages.CONNECT_GESTURE)) {
			if (this.currentState != GameServerState.SERVER_STARTED
					&& this.currentState != GameServerState.GUI_CONNECTED) {
				isValid = false;
			}
		} else if (msgID.equals(Messages.ROVER_TEST)) {
			if (this.currentState != GameServerState.ROVER_CONNECT_TEST) {
				isValid = false;
			}
		} else if (msgID.equals(Messages.START_GAME)) {
			if (this.currentState != GameServerState.ALL_CONNECTED || this.currentState != GameServerState.GAME_ENDED) {
				isValid = false;
			}
		} else if (msgID.equals(Messages.END_GAME)) {
			if (this.currentState != GameServerState.GAME_STARTED) {
				isValid = false;
			}
		} else if (GameServer.isDirection(msgID)) {
			if (this.currentState != GameServerState.GAME_STARTED) {
				isValid = false;
			}
		}

		if (!isValid) {
			LOGGER.severe(msgID + " recieved during invalid server state " + this.currentState);
		}
		return isValid;
	}

	protected void attachRover() {
		if (this.currentState != GameServerState.GUI_AND_GESTURE_CONNECTED) {
			throw new RuntimeException("AttachRover recieved during invalid server state " + this.currentState);
		}
		this.currentState = GameServerState.ROVER_CONNECT_TEST;
	}

	protected void attachGameBoard() {
		if (this.currentState != GameServerState.ROVER_CONNECTED) {
			throw new RuntimeException("attachGameBoard recieved during invalid server state " + this.currentState);
		}
		this.currentState = GameServerState.GAMEBOARD_CONNECTED;
	}

	protected void attachLeaderboard() {
		if (this.currentState != GameServerState.GAMEBOARD_CONNECTED) {
			throw new RuntimeException("attachLeaderboard recieved during invalid server state " + this.currentState);
		}
		this.currentState = GameServerState.ALL_CONNECTED;
	}

	public GameServerState getCurrentState() {
		return this.currentState;
	}

	public boolean isReadyToConnectGamePieces() {
		return this.getCurrentState() == GameServerState.GUI_AND_GESTURE_CONNECTED;
	}

	public void setErrorState() {
		this.currentState= GameServerState.ERROR_OCCURRED;
	}
	public boolean hasErrorOccurred()
	{
		return this.currentState == GameServerState.ERROR_OCCURRED;
	}
}
