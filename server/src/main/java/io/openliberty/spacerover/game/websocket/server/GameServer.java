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
import io.openliberty.spacerover.game.GameServerStateMachine;
import io.openliberty.spacerover.game.GameSession;
import io.openliberty.spacerover.game.models.GameEvent;
import io.openliberty.spacerover.game.models.SocketMessages;
import io.openliberty.spacerover.game.websocket.client.WebsocketClientEndpoint;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

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
	GameLeaderboard leaderboard = null;

	@Inject
	@ConfigProperty(name = "leaderboard.hostname", defaultValue = "leaderboard")
	String leaderboardHost;

	@Inject
	@ConfigProperty(name = "leaderboard.port", defaultValue = "9080")
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
		// (lifecycle) Called when the connection is opened
		LOGGER.log(Level.INFO, "Websocket open! client {0} connected on path {1} timeout: {2}, params{3}",
				new Object[] { session.getId(), path, session.getMaxIdleTimeout(), session.getRequestParameterMap() });
		session.getAsyncRemote().sendText("Welcome space explorer!");
		LOGGER.log(Level.WARNING,
				"roverIP = {0}, roverPort = {1}, gameboardIP = {2}, gameboardPort = {3}, leaderboardHost = {4}, leaderboardPort = {5}",
				new Object[] { roverIP, roverPort, gameboardIP, gameboardPort, leaderboardHost, leaderboardPort });
	}

	@OnClose
	public void onClose(final Session session, final CloseReason reason) {
		// (lifecycle) Called when the connection is closed
		LOGGER.log(Level.INFO, "Websocket closed! client {0} connected on path {1} timeout: {2}",
				new Object[] { session.getId(), reason, reason.getReasonPhrase() });

		final GameSession sessionName = getGameSession(session);
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
				} catch (final InterruptedException e) {
					LOGGER.log(Level.SEVERE, "Failed to wait for reconnection", e);
				}
			}
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
		// Called when a message is received.
		this.handleMessage(message, session);
	}

	private WebsocketClientEndpoint connectRoverClient() {
		WebsocketClientEndpoint client = null;
		try {
			final URI uri = new URI("ws://" + roverIP + ":" + roverPort);
			client = new WebsocketClientEndpoint(uri);
			client.addMessageHandler(this);
			client.sendMessage(SocketMessages.ROVER_TEST);
			this.stateMachine.attachRover();
		} catch (URISyntaxException | IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to connect to rover", e);
			this.guiSession.getAsyncRemote().sendText(getErrorMessage("Failed to connect to rover."));
			this.stateMachine.setErrorState();
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
	}

	@OnError
	public void onError(final Throwable t) {
		// (lifecycle) Called if/when an error occurs and the connection is disrupted
		LOGGER.log(Level.SEVERE, "Error received {0}", t);
	}

	@Override
	public void update(final GameEvent eventType, final long value) {
		if (this.guiSession.isOpen()) {
			try {
				this.guiSession.getBasicRemote()
						.sendText(eventType.toString() + SocketMessages.SOCKET_MESSAGE_DATA_DELIMITER + value);
			} catch (final IOException ioe) {
				LOGGER.severe(ioe.toString());
			}

		} else {
			LOGGER.info("update game event failed because session is closed");
		}
	}

	@Override
	public void handleMessage(final String message) {
		handleMessage(message, null);
	}

	public void handleMessage(final String message, final Session session) {
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
					case SocketMessages.ROVER_TEST:
						this.roverClient.sendMessage(SocketMessages.ROVER_ACK);
						break;
					case SocketMessages.GAMEBOARD_TEST:
						this.boardClient.sendMessage((SocketMessages.GAMEBOARD_ACK));
						break;
					case SocketMessages.START_GAME:
						LOGGER.log(Level.INFO, "Start Game received for player ID: {}", parsedMsg[1]);
						startGame(parsedMsg);
						this.roverClient.sendMessage(SocketMessages.START_GAME);
						this.guiSession.getAsyncRemote().sendText("Game Started!");
						break;
					case SocketMessages.END_GAME:
						LOGGER.info("Stop Game received");
						this.currentGame.endGameSession();
						this.roverClient.sendMessage(SocketMessages.END_GAME);
						this.roverClient.disconnect();
						this.boardClient.disconnect();
						break;
					case SocketMessages.BACKWARD:
					case SocketMessages.FORWARD:
					case SocketMessages.LEFT:
					case SocketMessages.RIGHT:
					case SocketMessages.STOP:
						this.sendRoverDirection(msgID);
						break;
					default:
						LOGGER.log(Level.INFO, "Unknown Message received <{}>", msgID);
						break;
				}
			}
		} catch (final IOException ioe) {
			LOGGER.log(Level.SEVERE, "Failed in message handler", ioe);
		}

		this.stateMachine.incrementState(msgID);

		if (this.stateMachine.isReadyToConnectGamePieces()) {
			testLeaderboard();
			this.roverClient = connectRoverClient();
			this.boardClient = connectBoardClient();
			if (!this.stateMachine.hasErrorOccurred()) {
				this.guiSession.getAsyncRemote().sendText(SocketMessages.SERVER_READY);
			}
		}
	}



	private void testLeaderboard() {
		GameLeaderboard leaderboard = new GameLeaderboard(this.leaderboardHost, this.leaderboardPort);
		leaderboard.testLeaderboard();
	}

	private WebsocketClientEndpoint connectBoardClient() {
		// TODO add logic for connecting the board
		this.stateMachine.attachGameBoard();
		return null;
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




}