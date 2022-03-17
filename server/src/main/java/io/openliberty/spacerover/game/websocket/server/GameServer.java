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
package io.openliberty.spacerover.game.websocket.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

import io.openliberty.spacerover.game.Messages;
import io.openliberty.spacerover.game.Game;
import io.openliberty.spacerover.game.GameEvent;
import io.openliberty.spacerover.game.GameEventListener;
import io.openliberty.spacerover.game.GameSession;
import io.openliberty.spacerover.game.websocket.client.WebsocketClientEndpoint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.logging.Level;
import java.util.logging.Logger;

@ApplicationScoped
@ServerEndpoint(value = "/roversocket")
public class GameServer implements GameEventListener, io.openliberty.spacerover.game.websocket.client.MessageHandler {
	private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
	Game currentGame = GameHolder.INSTANCE;
	GameServerStateMachine stateMachine = new GameServerStateMachine();
	Session guiSession = null;
	Session gestureSession = null;
	WebsocketClientEndpoint roverClient = null;
	WebsocketClientEndpoint boardClient = null;

	@OnOpen
	public void onOpen(Session session, @PathParam("path") String path) {
	
		// (lifecycle) Called when the connection is opened
		LOGGER.log(Level.INFO, "Websocket open! client {0} connected on path {1} timeout: {2}, params{3}",
				new Object[] { session.getId(), path, session.getMaxIdleTimeout(), session.getRequestParameterMap()});
		session.getAsyncRemote().sendText("Welcome space explorer!");
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) {
		// (lifecycle) Called when the connection is closed
		LOGGER.log(Level.INFO, "Websocket closed! client {0} connected on path {1} timeout: {2}",
				new Object[] { session.getId(), reason, reason.getReasonPhrase() });

		GameSession sessionName = getGameSession(session);
		if (this.currentGame.isInProgress() && (sessionName == GameSession.GUI || sessionName == GameSession.GESTURE)) {
			waitForSessionsToReconnect();
			if (!this.guiSession.isOpen() || !this.gestureSession.isOpen()) {
				LOGGER.info("Ending game after waiting for reconnect");
				this.currentGame.endGameSession();
			}
		}

		LOGGER.log(Level.INFO, "Lost connection with {0}", sessionName);
	}

	private void waitForSessionsToReconnect() {
		for (int i = 0; i < 5; i++) {
			if (!this.guiSession.isOpen() || !this.gestureSession.isOpen()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					LOGGER.log(Level.SEVERE, "Failed to wait for reconnection", e);
				}
			}
		}
	}

	private GameSession getGameSession(Session session) {
		String sessionID = session.getId();
		GameSession detectedSession = GameSession.UNKNOWN;
		if (this.guiSession != null && sessionID.equals(guiSession.getId())) {
			detectedSession = GameSession.GUI;

		} else if (this.gestureSession != null && sessionID.equals(gestureSession.getId())) {
			detectedSession = GameSession.GESTURE;

		}
		return detectedSession;
	}

	@OnMessage
	public void receiveMessage(String message, Session session) {
		// Called when a message is received.
		this.handleMessage(message, session);
	}

	private WebsocketClientEndpoint connectRoverClient() {
		String roverIP = System.getProperty("io.openliberty.spacerover.ip", "192.168.0.111");
		String roverPort = System.getProperty("io.openliberty.spacerover.port", "5045");
		WebsocketClientEndpoint client = null;
		try {
			URI uri = new URI("ws://" + roverIP + ":" + roverPort);
			client = new WebsocketClientEndpoint(uri);
			client.addMessageHandler(this);
			client.sendMessage(Messages.ROVER_TEST);
			this.stateMachine.attachRover();
		} catch (URISyntaxException | IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to connect to rover", e);
			this.guiSession.getAsyncRemote().sendText(getErrorMessage("Failed to connect to rover."));
			this.stateMachine.setErrorState();
		}
		return client;
	}

	private String getErrorMessage(String errorText) {
		return Messages.ERROR_MESSAGE + Messages.SOCKET_MESSAGE_DATA_DELIMITER + errorText;
	}

	private void startGame(String[] parsedMsg) {
		this.currentGame.startGameSession(parsedMsg[1]);
		this.currentGame.getEventManager().subscribe(GameEvent.HP, this);
		this.currentGame.getEventManager().subscribe(GameEvent.SCORE, this);
	}

	@OnError
	public void onError(Throwable t) {
		// (lifecycle) Called if/when an error occurs and the connection is disrupted
		LOGGER.log(Level.SEVERE, "Error received {0}", t);
	}

	@Override
	public void update(GameEvent eventType, long value) {
		if (this.guiSession.isOpen()) {
			try {
				this.guiSession.getBasicRemote()
						.sendText(eventType.toString() + Messages.SOCKET_MESSAGE_DATA_DELIMITER + value);
			} catch (IOException ioe) {
				LOGGER.severe(ioe.toString());
			}

		} else {
			LOGGER.info("update game event failed because session is closed");
		}
	}

	@Override
	public void handleMessage(String message) {
		handleMessage(message, null);
	}

	public void handleMessage(String message, Session session) {
		LOGGER.log(Level.INFO, "Message received: <{0}>", message);
		String[] parsedMsg = message.split("\\" + Messages.SOCKET_MESSAGE_DATA_DELIMITER);
		String msgID = parsedMsg[0];
		try {
			if (this.stateMachine.isValidState(msgID)) {
				switch (msgID) {
				case Messages.CONNECT_GUI:
					this.guiSession = session;
					break;
				case Messages.CONNECT_GESTURE:
					this.gestureSession = session;
					break;
				case Messages.ROVER_TEST:
					this.roverClient.sendMessage(Messages.ROVER_ACK);
					break;
				case Messages.GAMEBOARD_TEST:
					this.boardClient.sendMessage((Messages.GAMEBOARD_ACK));
					break;
				case Messages.START_GAME:
					LOGGER.log(Level.INFO, "Start Game received for player ID: {}", parsedMsg[1]);
					startGame(parsedMsg);
					this.roverClient.sendMessage(Messages.START_GAME);
					this.guiSession.getAsyncRemote().sendText("Game Started!");
					break;
				case Messages.END_GAME:
					LOGGER.info("Stop Game received");
					this.currentGame.endGameSession();
					this.roverClient.sendMessage(Messages.END_GAME);
					this.roverClient.disconnect();
					this.boardClient.disconnect();
					break;
				case Messages.BACKWARD:
				case Messages.FORWARD:
				case Messages.LEFT:
				case Messages.RIGHT:
				case Messages.STOP:
					this.sendRoverDirection(msgID);
					break;
				default:
					LOGGER.log(Level.INFO, "Unknown Message received <{}>", msgID);
					break;
				}
			}
		} catch (IOException ioe) {
			LOGGER.log(Level.SEVERE, "Failed in message handler", ioe);
		}

		this.stateMachine.incrementState(msgID);

		if (this.stateMachine.isReadyToConnectGamePieces()) {
			this.roverClient = connectRoverClient();
			this.boardClient = connectBoardClient();
			connectLeaderboard();
			if(!this.stateMachine.hasErrorOccurred())
			{
				this.guiSession.getAsyncRemote().sendText(Messages.SERVER_READY);
			}
		}
	}

	private void connectLeaderboard() {

		// connect to leaderboard here
		this.stateMachine.attachLeaderboard();
	}

	private WebsocketClientEndpoint connectBoardClient() {
		// TODO add logic for connecting the board
		this.stateMachine.attachGameBoard();
		return null;
	}

	public static boolean isDirection(String msgID) {
		return Arrays.asList(Messages.DIRECTIONS).contains(msgID);
	}

	private void sendRoverDirection(String direction) throws IOException {
		this.roverClient.sendMessage(direction);
	}

	private static class GameHolder {
		static final Game INSTANCE = new Game();
	}

}
