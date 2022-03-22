package io.openliberty.spacerover.game;

import io.openliberty.spacerover.game.models.SocketMessages;
import io.openliberty.spacerover.game.websocket.server.GameServer;

import java.util.logging.Logger;
public class GameServerStateMachine {
    private static final Logger LOGGER = Logger.getLogger(GameServerStateMachine.class.getName());
	GameServerState currentState;

	public GameServerStateMachine() {
		this.currentState = GameServerState.SERVER_STARTED;
	}

	public void incrementState(String msgID) {
		GameServerState beforeState = this.currentState;
		switch (msgID) {
		case SocketMessages.CONNECT_GUI:
			if (this.currentState == GameServerState.SERVER_STARTED) {
				this.currentState = GameServerState.GUI_CONNECTED;
			} else {
				this.currentState = GameServerState.GUI_AND_GESTURE_CONNECTED;
			}
			break;
		case SocketMessages.CONNECT_GESTURE:
			if (this.currentState == GameServerState.SERVER_STARTED) {
				this.currentState = GameServerState.GESTURE_CONNECTED;
			} else {
				this.currentState = GameServerState.GUI_AND_GESTURE_CONNECTED;
			}
			break;

		case SocketMessages.ROVER_ACK:
			this.currentState = GameServerState.ROVER_CONNECTED;
			break;
		case SocketMessages.GAMEBOARD_ACK:
			this.currentState = GameServerState.ALL_CONNECTED;
			break;
		case SocketMessages.START_GAME:
			this.currentState = GameServerState.GAME_STARTED;
			break;
		case SocketMessages.END_GAME:
			this.currentState = GameServerState.GUI_AND_GESTURE_CONNECTED;
			break;
		default:
			LOGGER.severe("Unexpected msgID " + msgID + " during state " + beforeState);
		}
		LOGGER.info("Change state from " + beforeState + " to " + this.currentState);
	}

	public boolean isValidState(String msgID) {
		boolean isValid = true;
		if (msgID.equals(SocketMessages.CONNECT_GUI)) {
			if (this.currentState != GameServerState.SERVER_STARTED
					&& this.currentState != GameServerState.GESTURE_CONNECTED) {
				isValid = false;
			}
		} else if (msgID.equals(SocketMessages.CONNECT_GESTURE)) {
			if (this.currentState != GameServerState.SERVER_STARTED
					&& this.currentState != GameServerState.GUI_CONNECTED) {
				isValid = false;
			}
		} else if (msgID.equals(SocketMessages.ROVER_ACK)) {
			if (this.currentState != GameServerState.ROVER_CONNECT_TEST) {
				isValid = false;
			}
			
		} else if (msgID.equals(SocketMessages.GAMEBOARD_ACK)) {
			if (this.currentState != GameServerState.GAMEBOARD_CONNECT_TEST) {
				isValid = false;
			}
			
		} else if (msgID.equals(SocketMessages.START_GAME)) {
			if (this.currentState != GameServerState.ALL_CONNECTED && this.currentState != GameServerState.GAME_ENDED) {
				isValid = false;
			}
		} else if (msgID.equals(SocketMessages.END_GAME)) {
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

	public void attachRover() {
		if (this.currentState != GameServerState.LEADERBOARD_CONNECTED) {
			throw new RuntimeException("AttachRover recieved during invalid server state " + this.currentState);
		}
		this.currentState = GameServerState.ROVER_CONNECT_TEST;
	}

	public void attachGameBoard() {
		if (this.currentState != GameServerState.ROVER_CONNECTED) {
			throw new RuntimeException("attachGameBoard recieved during invalid server state " + this.currentState);
		}
		this.currentState = GameServerState.GAMEBOARD_CONNECT_TEST;
	}

	protected void attachLeaderboard() {
		if (this.currentState != GameServerState.GUI_AND_GESTURE_CONNECTED) {
			throw new RuntimeException("attachLeaderboard recieved during invalid server state " + this.currentState);
		}
		this.currentState = GameServerState.LEADERBOARD_CONNECTED;
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
