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
	int leaderboardPort;

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
		LOGGER.log(Level.INFO, "Websocket closed! client {0} connected on path {1} timeout: {2}",
				new Object[] { session.getId(), reason, reason.getReasonPhrase() });

		final GameSession sessionName = getGameSession(session);
		if (sessionName == GameSession.GUI) {
			reInit(true);
		}

		LOGGER.log(Level.INFO, "Lost connection with {0}", sessionName);
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

	private synchronized WebsocketClientEndpoint connectRoverClient() {
		WebsocketClientEndpoint client = null;
		try {
			final URI uri = new URI("ws://" + roverIP + ":" + roverPort);
			client = new WebsocketClientEndpoint(uri, this);
			this.stateMachine.attachRover();
		} catch (URISyntaxException | IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to connect to rover", e);
			this.setErrorStateAndSendError("Failed to connect to rover.");
		}
		return client;
	}

	private String getErrorMessage(final String errorText) {
		return SocketMessages.ERROR_MESSAGE + SocketMessages.SOCKET_MESSAGE_DATA_DELIMITER + errorText;
	}

	private void startGame(final String[] parsedMsg) {
		this.currentGame.startGameSession(parsedMsg[1]);
		this.currentGame.getEventManager().subscribe(GameEvent.HP, this);
		this.currentGame.getEventManager().subscribe(GameEvent.SCORE, this);
		this.currentGame.getEventManager().subscribe(GameEvent.GAME_OVER, this);
		this.roverClient.getEventManager().subscribe(GameEvent.SOCKET_DISCONNECT, this);
		this.boardClient.getEventManager().subscribe(GameEvent.SOCKET_DISCONNECT, this);
	}

	@OnError
	public void onError(final Throwable t) {
		LOGGER.log(Level.SEVERE, "Error received", t);
	}

	@Override
	public void update(final GameEvent eventType, final long value) {
		if (this.currentGame.isInProgress()
				&& (eventType == GameEvent.GAME_OVER || eventType == GameEvent.SOCKET_DISCONNECT)) {
			LOGGER.log(Level.WARNING, "Ending game from event type {0}", eventType);
			endGameFromServer();
		} else {
			this.sendTextToGuiSocket(
					eventType.toString().toLowerCase() + SocketMessages.SOCKET_MESSAGE_DATA_DELIMITER + value);
		}
	}

	private void endGameFromServer() {
		LOGGER.log(Level.INFO, "Game ended from server side. {0}", this.currentGame);
		this.currentGame.endGameSession();
		this.getLeaderboard().updateLeaderboard(this.currentGame.getGameLeaderboardStat());
		this.sendTextToGuiSocket(SocketMessages.END_GAME);
	}

	@Override
	public void handleMessage(final String message) {
		handleMessage(message, null);
	}

	public synchronized void handleMessage(final String message, final Session session) {
		LOGGER.log(Level.INFO, "Message received: <{0}>", message);
		final String[] parsedMsg = message.split("\\" + SocketMessages.SOCKET_MESSAGE_DATA_DELIMITER);
		final String msgID = parsedMsg[0];
		try {
			if (this.stateMachine.isValidState(msgID)) {
				switch (msgID) {
				case SocketMessages.CONNECT_GUI:
					this.guiSession = session;
					break;
				case SocketMessages.CONNECT_GESTURE:
					this.gestureSession = session;
					break;
				case SocketMessages.ROVER_ACK:
					break;
				case SocketMessages.GAMEBOARD_ACK:
					break;
				case SocketMessages.START_GAME:
					LOGGER.log(Level.INFO, "Start Game received for player ID: {0}", parsedMsg[1]);
					startGame(parsedMsg);
					this.roverClient.sendMessage("1");
					this.boardClient.sendMessage("1");
					String gameStarted = "Game Started!";
					sendTextToGuiSocket(gameStarted);
					break;
				case SocketMessages.END_GAME:
					endGameFromServer();
					break;
				case SocketMessages.BACKWARD:
				case SocketMessages.FORWARD:
				case SocketMessages.LEFT:
				case SocketMessages.RIGHT:
				case SocketMessages.STOP:
					this.sendRoverDirection(msgID);
					break;
				case SocketMessages.COLOUR_RED:
				case SocketMessages.COLOUR_BLUE:
				case SocketMessages.COLOUR_GREEN:
				case SocketMessages.COLOUR_PURPLE:
				case SocketMessages.COLOUR_YELLOW:
					this.currentGame.processColour(msgID);
					this.sendBoardColour(msgID);
					break;
				case SocketMessages.GAME_HEALTH_TEST:
					session.getAsyncRemote().sendText(SocketMessages.GAME_HEALTH_ACK);
					break;
				default:
					LOGGER.log(Level.INFO, "Unknown Message received <{0}>", msgID);
				}
				this.stateMachine.incrementState(msgID);
			}
		} catch (final IOException ioe) {
			LOGGER.log(Level.SEVERE, "Failed in message handler", ioe);
			this.stateMachine.setErrorState();
		}

		connectGamePieces();
		if (this.currentGame.isInProgressGameOver()) {
			this.endGameFromServer();
		}
	}

	private void connectGamePieces() {
		if (this.stateMachine.isReadyToConnectGamePieces()) {
			testLeaderboard();
		}

		if (this.stateMachine.isReadyToConnectRover() && !this.stateMachine.hasErrorOccurred()) {
			disconnectRover();
			this.roverClient = connectRoverClient();
		}

		if (this.stateMachine.isReadyToConnectBoard() && !this.stateMachine.hasErrorOccurred()) {
			disconnectBoard();
			this.boardClient = connectBoardClient();
		}
		if (this.stateMachine.isAllConnected()) {
			this.sendTextToGuiSocket(SocketMessages.SERVER_READY);
		}
	}

	private void sendBoardColour(String colour) throws IOException {
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
			this.guiSession.getAsyncRemote().sendText(text);
			LOGGER.log(Level.INFO, "Sent message to GUI <{0}>", text);
		} else {
			LOGGER.log(Level.WARNING, "Failed to send message to GUI <{0}>", text);
		}
	}

	private GameLeaderboard getLeaderboard() {
		return new GameLeaderboard(this.leaderboardHost, this.leaderboardPort);
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
		this.stateMachine.setErrorState();
		this.sendTextToGuiSocket(this.getErrorMessage(errMsg));
	}

	private synchronized WebsocketClientEndpoint connectBoardClient() {
		LOGGER.log(Level.WARNING, "connecting to board: {0} {1}", new Object[] { gameboardIP, gameboardPort });
		WebsocketClientEndpoint client = null;
		try {
			final URI uri = new URI("ws://" + gameboardIP + ":" + gameboardPort);
			client = new WebsocketClientEndpoint(uri, this);
			this.stateMachine.attachGameBoard();
			LOGGER.log(Level.WARNING, "connected to board");

		} catch (URISyntaxException | IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to connect to game board", e);
			this.setErrorStateAndSendError("Failed to connect to game board.");
		}
		return client;
	}

	public static boolean isDirection(final String msgID) {
		return Arrays.asList(SocketMessages.DIRECTIONS).contains(msgID);
	}

	private void sendRoverDirection(final String direction) throws IOException {
		this.roverClient.sendMessage(direction);
	}

	private static class GameHolder {
		static final Game INSTANCE = new Game();
	}

	private synchronized void reInit(boolean isFullReinit) {
		GameServerState state = (isFullReinit ? GameServerState.SERVER_STARTED
				: GameServerState.GUI_AND_GESTURE_CONNECTED);
		LOGGER.log(Level.WARNING, "ReInit called {0}", state);

		this.stateMachine = new GameServerStateMachine(state);
		this.currentGame = new Game();

		if (state == GameServerState.SERVER_STARTED) {
			if (this.guiSession != null) {
				try {
					this.guiSession.close();
				} catch (IOException ioe) {
					LOGGER.log(Level.WARNING, "failure during reInit", ioe);
				}
			}
			if (this.gestureSession != null) {
				try {
					this.gestureSession.close();

				} catch (IOException ioe) {
					LOGGER.log(Level.WARNING, "failure during reInit", ioe);
				}
			}
			disconnectRover();
			disconnectBoard();
		} else {
			disconnectRover();
			disconnectBoard();
			connectGamePieces();
		}

	}

}