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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.openliberty.spacerover.game.Game;
import io.openliberty.spacerover.game.GameEventListener;
import io.openliberty.spacerover.game.GameLeaderboard;
import io.openliberty.spacerover.game.GameServerState;
import io.openliberty.spacerover.game.GameServerStateMachine;
import io.openliberty.spacerover.game.GameSession;
import io.openliberty.spacerover.game.models.GameEvent;
import io.openliberty.spacerover.game.models.SocketMessages;
import io.openliberty.spacerover.game.websocket.client.WebsocketClientEndpoint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint(value = "/" + GameServerConstants.WEBSOCKET_ENDPOINT)
public class GameServer implements GameEventListener, io.openliberty.spacerover.game.websocket.client.MessageHandler {
	private static final String COLON = ":";
	private static final String WEBSOCKET_PROTOCOL = "ws://";
	private static final Logger LOGGER = Logger.getLogger(GameServer.class.getName());
	Game currentGame = GameHolder.INSTANCE;

	@Singleton
	GameServerStateMachine stateMachine = new GameServerStateMachine();

	Session guiSession = null;
	Session gestureSession = null;
	WebsocketClientEndpoint roverClient = null;
	WebsocketClientEndpoint boardClient = null;

	@Inject
	@ConfigProperty(name = "io.openliberty.leaderboard.hostname", defaultValue = "leaderboard")
	String leaderboardHost;

	@Inject
	@ConfigProperty(name = "io.openliberty.leaderboard.port", defaultValue = "9080")
	String leaderboardPort;

	@Inject
	@ConfigProperty(name = "io.openliberty.spacerover.ip", defaultValue = "192.168.0.110")
	String roverIP;

	@Inject
	@ConfigProperty(name = "io.openliberty.spacerover.port", defaultValue = "5045")
	String roverPort;

	@Inject
	@ConfigProperty(name = "io.openliberty.gameboard.ip", defaultValue = "192.168.0.111")
	String gameboardIP;

	@Inject
	@ConfigProperty(name = "io.openliberty.gameboard.port", defaultValue = "5045")
	String gameboardPort;

	@OnOpen
	public void onOpen(final Session session, @PathParam("path") final String path) {
		LOGGER.log(Level.INFO, "Websocket open! client {0} connected on path {1} timeout: {2}, params{3}",
				new Object[] { session.getId(), path, session.getMaxIdleTimeout(), session.getRequestParameterMap() });
		String welcomeText = "Welcome space explorer!";
		session.getAsyncRemote().sendText(welcomeText);
		LOGGER.log(Level.WARNING,
				"roverIP = {0}, roverPort = {1}, gameboardIP = {2}, gameboardPort = {3}, leaderboardHost = {4}, leaderboardPort = {5}",
				new Object[] { roverIP, roverPort, gameboardIP, gameboardPort, leaderboardHost, leaderboardPort });
	}

	@OnClose
	public void onClose(final Session session, final CloseReason reason) {
		LOGGER.log(Level.INFO, "Websocket closed! client ID: {0} close reason: {1} close reason phrase: {2}",
				new Object[] { session.getId(), reason, reason.getReasonPhrase() });

		final GameSession sessionName = getGameSession(session);
		if (sessionName == GameSession.GUI) {
			LOGGER.log(Level.INFO, "Lost connection with {0}", sessionName);
			reInit();
		}

	}

	private GameSession getGameSession(final Session session) {
		final String sessionID = session.getId();
		GameSession detectedSession = GameSession.UNKNOWN;
		if (this.guiSession != null && sessionID.equals(guiSession.getId())) {
			detectedSession = GameSession.GUI;

		} else if (this.gestureSession != null && sessionID.equals(gestureSession.getId())) {
			detectedSession = GameSession.GESTURE;

		}
		return detectedSession;
	}

	@OnMessage
	public void receiveMessage(final String message, final Session session) {
		this.handleMessage(message, session);
	}

	private String getErrorMessage(final String errorText) {
		return SocketMessages.ERROR_MESSAGE + SocketMessages.SOCKET_MESSAGE_DATA_DELIMITER + errorText;
	}

	private void startGame(final String[] parsedMsg) {
		String playerId = parsedMsg[1];
		LOGGER.log(Level.INFO, "Start Game received for player ID: {0}", playerId);
		this.currentGame.startGameSession(playerId);
		registerGameEventManager();
	}

	private void registerGameEventManager() {
		this.currentGame.getEventManager().subscribe(GameEvent.HP, this);
		this.currentGame.getEventManager().subscribe(GameEvent.SCORE, this);
		this.currentGame.getEventManager().subscribe(GameEvent.GAME_OVER, this);
	}

	@OnError
	public void onError(final Throwable t) {
		LOGGER.log(Level.SEVERE, "Error received", t);
	}

	@Override
	public void update(final GameEvent eventType, final long value) {
		if (eventType == GameEvent.SOCKET_DISCONNECT) {
			if (!this.stateMachine.hasErrorOccurred()) {
				this.setErrorStateAndSendError("Socket disconnected");
			}
		} else if (eventType == GameEvent.GAME_OVER) {
			LOGGER.log(Level.WARNING, "Ending game from event type {0}", eventType);
			endGameFromServer(false);
		} else {
			this.sendTextToGuiSocket(
					eventType.toString().toLowerCase() + SocketMessages.SOCKET_MESSAGE_DATA_DELIMITER + value);
		}
	}

	private void endGameFromServer(boolean isEndOnError) {
		if (isEndOnError) {
			LOGGER.log(Level.WARNING, "Game ended from server side due to error. {0}", this.currentGame);
			this.setErrorStateAndSendError("Game ended unexpectedly");
		} else {
			LOGGER.log(Level.INFO, "Ending game from server side. {0}", this.currentGame);
			this.getLeaderboard().updateLeaderboard(this.currentGame.getGameLeaderboardStat());
			this.sendTextToGuiSocket(SocketMessages.END_GAME);
		}
	}

	@Override
	public void handleMessage(final String message) {
		handleMessage(message, null);
	}

	public synchronized void handleMessage(final String message, final Session session) {
		LOGGER.log(Level.INFO, "Message received: <{0}>", message);
		final String[] parsedMsg = message.split("\\" + SocketMessages.SOCKET_MESSAGE_DATA_DELIMITER);
		String msgID = parsedMsg[0];

		if (this.stateMachine.isValidState(msgID)) {
			switch (msgID) {
			case SocketMessages.CONNECT_GUI:
				this.guiSession = session;
				break;
			case SocketMessages.CONNECT_GESTURE:
				this.gestureSession = session;
				break;
			case SocketMessages.ROVER_ACK:
				this.roverClient.getEventManager().subscribe(GameEvent.SOCKET_DISCONNECT, this);
				break;
			case SocketMessages.GAMEBOARD_ACK:
				this.boardClient.getEventManager().subscribe(GameEvent.SOCKET_DISCONNECT, this);
				break;
			case SocketMessages.START_GAME:
				assert (parsedMsg.length == 2);
				startGame(parsedMsg);
				this.roverClient.sendMessage(SocketMessages.INIT_GAME);
				this.boardClient.sendMessage(SocketMessages.INIT_GAME);
				break;
			case SocketMessages.END_GAME:
				if (parsedMsg.length == 2) {
					// timeout
					this.currentGame.endGameSession(parsedMsg[1]);
				} else {
					this.currentGame.endGameSession();
				}
				break;
			case SocketMessages.BACKWARD:
			case SocketMessages.FORWARD:
			case SocketMessages.LEFT:
			case SocketMessages.RIGHT:
			case SocketMessages.STOP:
				this.sendRoverDirection(msgID);
				break;
			case SocketMessages.COLOUR_RED:
				this.sendBoardColour(msgID);
				this.currentGame.processColour(msgID);
				break;
			case SocketMessages.COLOUR_BLUE:
			case SocketMessages.COLOUR_GREEN:
			case SocketMessages.COLOUR_PURPLE:
			case SocketMessages.COLOUR_YELLOW:
				msgID = this.currentGame.getColour(msgID);
				this.sendBoardColour(msgID);
				this.currentGame.processColour(msgID);
				break;
			case SocketMessages.GAME_HEALTH_TEST:
				session.getAsyncRemote().sendText(SocketMessages.GAME_HEALTH_ACK);
				break;
			default:
				LOGGER.log(Level.INFO, "Unknown Message received <{0}>", msgID);
			}
			this.stateMachine.incrementState(msgID);
		}

		if (!msgID.equals(SocketMessages.END_GAME)) {
			connectGamePieces();
		}
	}

	private synchronized void connectGamePieces() {
		if (this.stateMachine.isReadyToConnectGamePieces()) {
			testLeaderboard();
		}
		if (this.stateMachine.isReadyToConnectRover() && !this.stateMachine.hasErrorOccurred()) {
			connectRover();
		} else if (this.stateMachine.isReadyToConnectBoard() && !this.stateMachine.hasErrorOccurred()) {
			connectBoard();
		} else if (this.stateMachine.isAllConnected()) {
			this.sendTextToGuiSocket(SocketMessages.SERVER_READY);
		} else if (this.stateMachine.hasErrorOccurred()) {
			this.endGameFromServer(true);
		}
	}

	private void connectBoard() {
		disconnectBoard();
		String boardConnectionString = WEBSOCKET_PROTOCOL + gameboardIP + COLON + gameboardPort;
		this.stateMachine.attachGameBoard();
		this.boardClient = new WebsocketClientEndpoint(this, boardConnectionString);
		try {
			this.boardClient.connect();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to connect to board", e);
			this.setErrorStateAndSendError("Failed to connect to board.");
		}
	}

	private void connectRover() {
		disconnectRover();
		String roverConnectionString = WEBSOCKET_PROTOCOL + roverIP + COLON + roverPort;
		this.stateMachine.attachRover();
		this.roverClient = new WebsocketClientEndpoint(this, roverConnectionString);
		try {
			this.roverClient.connect();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to connect to rover", e);
			this.setErrorStateAndSendError("Failed to connect to rover.");
		}
	}

	private void sendBoardColour(String colour) {
		this.boardClient.sendMessage(colour);
	}

	private synchronized void disconnectRover() {
		if (this.roverClient != null) {
			try {
				this.roverClient.disconnect();
				LOGGER.log(Level.WARNING, "Disconnected rover");
			} catch (IOException ioe) {
				LOGGER.log(Level.SEVERE, "failed to disconnect rover", ioe);
			}
			this.roverClient = null;
		}
	}

	private synchronized void disconnectBoard() {
		if (this.boardClient != null) {
			try {
				this.boardClient.disconnect();
				LOGGER.log(Level.WARNING, "Disconnected board");
			} catch (IOException ioe) {
				LOGGER.log(Level.SEVERE, "failed to disconnect rover", ioe);
			}
			this.boardClient = null;
		}
	}

	private void sendTextToGuiSocket(String text) {
		if (this.guiSession != null && this.guiSession.isOpen()) {
			try {
				this.guiSession.getBasicRemote().sendText(text);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "Failed to send message to GUI <{0}>", text);
			}
			LOGGER.log(Level.INFO, "Sent message to GUI <{0}>", text);
		} else {
			LOGGER.log(Level.WARNING, "Failed to send message to GUI <{0}>", text);
		}
	}

	private GameLeaderboard getLeaderboard() {
		return new GameLeaderboard(this.leaderboardHost, Integer.parseInt(leaderboardPort));
	}

	private void testLeaderboard() {
		GameLeaderboard board = getLeaderboard();
		if (board.testLeaderboard()) {
			this.stateMachine.attachLeaderboard();
		} else {
			setErrorStateAndSendError("Failed to connect to leaderboard");
		}
	}

	private void setErrorStateAndSendError(String errMsg) {
		LOGGER.log(Level.SEVERE, "setErrorStateAndSendError called: {0}", errMsg);
		this.stateMachine.setErrorState();
		this.sendTextToGuiSocket(this.getErrorMessage(errMsg));
		this.reInit();
	}

	public static boolean isDirection(final String msgID) {
		return Arrays.asList(SocketMessages.DIRECTIONS).contains(msgID);
	}

	private void sendRoverDirection(final String direction) {
		this.roverClient.sendMessage(direction, false);
	}

	private static class GameHolder {
		static final Game INSTANCE = new Game();
	}

	private synchronized void reInit() {
		GameServerState state = GameServerState.SERVER_STARTED;
		LOGGER.log(Level.WARNING, "ReInit called, resetting state to: {0}", new Object[] { state });

		if (!this.stateMachine.hasErrorOccurred()) {
			if (this.roverClient != null) {
				this.roverClient.getEventManager().unsubscribe(GameEvent.SOCKET_DISCONNECT, this);
			}
			if (this.boardClient != null) {
				this.boardClient.getEventManager().unsubscribe(GameEvent.SOCKET_DISCONNECT, this);
			}
		}

		if (this.guiSession != null) {
			try {
				this.guiSession.close();
				LOGGER.log(Level.WARNING, "Disconnected gui session from server side");
			} catch (IOException ioe) {
				LOGGER.log(Level.WARNING, "failure during reInit", ioe);
			}
		}
		if (this.gestureSession != null) {
			try {
				this.gestureSession.close();
				LOGGER.log(Level.WARNING, "Disconnected gesture  session from server side");

			} catch (IOException ioe) {
				LOGGER.log(Level.WARNING, "failure during reInit", ioe);
			}
		}
		disconnectRover();
		disconnectBoard();
		this.stateMachine = new GameServerStateMachine(state);
		this.currentGame = new Game();
	}

}
