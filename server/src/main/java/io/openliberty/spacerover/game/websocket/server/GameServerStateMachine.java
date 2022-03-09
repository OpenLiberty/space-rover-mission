package io.openliberty.spacerover.game.websocket.server;

import io.openliberty.spacerover.game.Constants;

public class GameServerStateMachine {

	GameServerState currentState;

	public GameServerStateMachine() {
		this.currentState = GameServerState.SERVER_STARTED;
	}

	protected void incrementState(String msgID) {
		if (msgID.equals(Constants.CONNECT_GUI)) {
			if (this.currentState == GameServerState.SERVER_STARTED) {
				this.currentState = GameServerState.GUI_CONNECTED;
			} else {
				this.currentState = GameServerState.GUI_AND_GESTURE_CONNECTED;
			}
		} else if (msgID.equals(Constants.CONNECT_GESTURE)) {
			if (this.currentState == GameServerState.SERVER_STARTED) {
				this.currentState = GameServerState.GESTURE_CONNECTED;
			} else {
				this.currentState = GameServerState.GUI_AND_GESTURE_CONNECTED;
			}
		} else if (msgID.equals(Constants.START_GAME)) {
			this.currentState = GameServerState.GAME_STARTED;
		} else if (msgID.equals(Constants.END_GAME)) {
			this.currentState = GameServerState.GAME_ENDED;
		}

	}

	protected boolean isValidState(String msgID) {
		boolean isValid = true; 
		if (msgID.equals(Constants.CONNECT_GUI)) {
			if (this.currentState != GameServerState.SERVER_STARTED
					|| this.currentState != GameServerState.GESTURE_CONNECTED) {
				isValid = false;
			}
		} else if (msgID.equals(Constants.CONNECT_GESTURE)) {
			if (this.currentState != GameServerState.SERVER_STARTED
					|| this.currentState != GameServerState.GUI_CONNECTED) {
				isValid = false;
			}
		} else if (msgID.equals(Constants.START_GAME)) {
			if (this.currentState != GameServerState.ALL_CONNECTED || this.currentState != GameServerState.GAME_ENDED ) {
				isValid = false;
			}
		} else if (msgID.equals(Constants.END_GAME)) {
			if (this.currentState != GameServerState.GAME_STARTED) {
				isValid = false;
			}
		} else if (GameServer.isDirection(msgID)) {
			if (this.currentState != GameServerState.GAME_STARTED) {
				isValid = false; 
			}
		}

		if (!isValid)
		{
			System.out.println(msgID + " recieved during invalid server state " + this.currentState);
		}
		return isValid;
	}

	protected void attachRover() {
		if (this.currentState != GameServerState.GUI_AND_GESTURE_CONNECTED) {
			throw new RuntimeException("AttachRover recieved during invalid server state " + this.currentState);
		}
		this.currentState = GameServerState.ROVER_CONNECTED;
	}
	
	protected void attachGameBoard()
	{
		if (this.currentState != GameServerState.ROVER_CONNECTED) {
			throw new RuntimeException("attachGameBoard recieved during invalid server state " + this.currentState);
		}
		this.currentState = GameServerState.GAMEBOARD_CONNECTED;
	}
	
	protected void attachLeaderboard()
	{
		if (this.currentState != GameServerState.GAMEBOARD_CONNECTED) {
			throw new RuntimeException("attachLeaderboard recieved during invalid server state " + this.currentState);
		}
		this.currentState = GameServerState.ALL_CONNECTED;
	}
	
	public GameServerState getCurrentState()
	{
		return this.currentState;
	}
	
	public boolean isReadyToConnectGamePieces()
	{
		return this.getCurrentState() == GameServerState.GUI_AND_GESTURE_CONNECTED;
	}
}
