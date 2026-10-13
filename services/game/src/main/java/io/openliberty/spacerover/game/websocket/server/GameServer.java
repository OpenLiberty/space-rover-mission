/*******************************************************************************
 * Copyright (c) 2022, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.spacerover.game.websocket.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.openliberty.spacerover.game.Game;
import io.openliberty.spacerover.game.GameEventListener;
import io.openliberty.spacerover.game.GameServerState;
import io.openliberty.spacerover.game.GameServerStateMachine;
import io.openliberty.spacerover.game.GameSession;
import io.openliberty.spacerover.game.models.GameEvent;
import io.openliberty.spacerover.game.models.Constants;
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
	private Game currentGame = new Game();
	private int percentageBatteryLeft = 100;
	private float batteryVoltage = 0;

	@Singleton
	GameServerStateMachine stateMachine = new GameServerStateMachine();

	Session guiSession = null;
	WebsocketClientEndpoint roverClient = null;
	WebsocketClientEndpoint boardClient = null;

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
				"roverIP = {0}, roverPort = {1}, gameboardIP = {2}, gameboardPort = {3}",
				new Object[] { roverIP, roverPort, gameboardIP, gameboardPort });
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
		}
		return detectedSession;
	}

	@OnMessage
	public void receiveMessage(final String message, final Session session) {
		this.handleMessage(message, session);
	}

	private String getErrorMessage(final String errorText) {
		return Constants.ERROR_MESSAGE + Constants.SOCKET_MESSAGE_DATA_DELIMITER + errorText;
	}

	private void startGame(final String[] properties) {
		String playerId = properties[0];
		try {
			playerId = URLDecoder.decode(playerId, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			// utf-8 always supported
		}

		int gameMode = Integer.parseInt(properties[1]);
		LOGGER.log(Level.INFO, "Start Game received for player ID: {0}, GameMode: {1}",
				new Object[] { playerId, gameMode });
		if (gameMode == Integer.parseInt(Constants.INIT_GAME_FREE_ROAM)) {
			this.currentGame = new Game();
			registerGameEventManager();
		}
		this.currentGame.startGameSession(playerId);
	}

	private void registerGameEventManager() {
		this.currentGame.getEventManager().subscribe(GameEvent.HP, this);
		this.currentGame.getEventManager().subscribe(GameEvent.HP_SUN, this);
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
			String msg = eventType.toString().toLowerCase() + Constants.SOCKET_MESSAGE_DATA_DELIMITER + value;
			if (eventType == GameEvent.HP_SUN) {
				msg = GameEvent.HP.toString().toLowerCase() + Constants.SOCKET_MESSAGE_DATA_DELIMITER + value
						+ Constants.SOCKET_MESSAGE_PAYLOAD_DELIMITER + "sun";
			}
			this.sendTextToGuiSocket(msg);
		}
	}

	private void endGameFromServer(boolean isEndOnError) {
		if (isEndOnError) {
			LOGGER.log(Level.WARNING, "Game ended from server side due to error. {0}", this.currentGame);
			this.setErrorStateAndSendError("Game ended unexpectedly");
		} else {
			LOGGER.log(Level.INFO, "Ending game from server side. {0}", this.currentGame);
			this.sendTextToGuiSocket(Constants.END_GAME);
		}
	}

	@Override
	public void handleMessage(final String message) {
		handleMessage(message, null);
	}

	public synchronized void handleMessage(final String message, final Session session) {
		LOGGER.log(Level.INFO, "Message received: <{0}>", message);
		final String[] parsedMsg = message.split("\\" + Constants.SOCKET_MESSAGE_DATA_DELIMITER);
		String msgID = parsedMsg[0];

		if (this.stateMachine.isValidState(msgID)) {
			switch (msgID) {
				case Constants.CONNECT_GUI:
					this.guiSession = session;
					break;
				case Constants.ROVER_ACK:
					parseBatteryMeasurements(parsedMsg);
					this.sendTextToGuiSocket(Constants.GUI_BATTERY_PCT + Constants.SOCKET_MESSAGE_DATA_DELIMITER
							+ this.getBatteryPercentage());
					this.roverClient.getEventManager().subscribe(GameEvent.SOCKET_DISCONNECT, this);
					break;
				case Constants.GAMEBOARD_ACK:
					this.boardClient.getEventManager().subscribe(GameEvent.SOCKET_DISCONNECT, this);
					break;
				case Constants.START_GAME:
					assert (parsedMsg.length == 2);
					String[] properties = parsedMsg[1].split(Constants.SOCKET_MESSAGE_PAYLOAD_DELIMITER);
					this.roverClient.sendMessage(properties[1]);
					this.boardClient.sendMessage(properties[1]);
					startGame(properties);
					break;
				case Constants.END_GAME:
					if (parsedMsg.length == 2) {
						// timeout
						this.currentGame.endGameSession(parsedMsg[1]);
					} else {
						this.currentGame.endGameSession();
					}
					break;
				case Constants.BACKWARD:
				case Constants.FORWARD:
				case Constants.LEFT:
				case Constants.RIGHT:
				case Constants.STOP:
					this.sendRoverDirection(msgID);
					break;
				case Constants.GAME_HEALTH_TEST:
					session.getAsyncRemote().sendText(Constants.GAME_HEALTH_ACK);
					break;
				default:
					LOGGER.log(Level.INFO, "Unknown Message received <{0}>", msgID);
			}
			this.stateMachine.incrementState(msgID);

			if (!msgID.equals(Constants.END_GAME)) {
				connectGamePieces();
			}
		}
	}

	private void parseBatteryMeasurements(final String[] parsedMsg) {
		if (parsedMsg.length == 3) {
			String batteryPercentage = parsedMsg[2];
			String batteryVoltage = parsedMsg[1];
			try {
				this.percentageBatteryLeft = Integer.parseInt(batteryPercentage);
				this.batteryVoltage = Float.parseFloat(batteryVoltage);
			} catch (NumberFormatException nfe) {
				LOGGER.log(Level.SEVERE, "Battery voltage {0} or Battery Percentage not valid: {1}",
						new Object[] { batteryVoltage, batteryPercentage });
				this.percentageBatteryLeft = 100;
				this.batteryVoltage = 0.0f;
			}
		}
	}

	private synchronized void connectGamePieces() {
		if (this.stateMachine.isReadyToConnectGamePieces()) {
			this.stateMachine.attachLeaderboard();
		}
		if (this.stateMachine.isReadyToConnectRover() && !this.stateMachine.hasErrorOccurred()) {
			connectRover();
		} else if (this.stateMachine.isReadyToConnectBoard() && !this.stateMachine.hasErrorOccurred()) {
			connectBoard();
		} else if (this.stateMachine.isAllConnected()) {
			this.sendTextToGuiSocket(Constants.SERVER_READY);
		} else if (this.stateMachine.hasErrorOccurred()) {
			this.endGameFromServer(true);
		}
	}

	private void connectRover() {
		disconnectRover();
		String roverConnectionString = WEBSOCKET_PROTOCOL + roverIP + COLON + roverPort;
		this.stateMachine.attachRover();
		this.roverClient = new WebsocketClientEndpoint(this, roverConnectionString, Constants.ROVER_SOCKET_NAME);
		try {
			this.roverClient.connect();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to connect to rover", e);
			this.setErrorStateAndSendError("Failed to connect to rover.");
		}
	}

	private void connectBoard() {
		disconnectBoard();
		String boardConnectionString = WEBSOCKET_PROTOCOL + gameboardIP + COLON + gameboardPort;
		this.stateMachine.attachGameBoard();
		this.boardClient = new WebsocketClientEndpoint(this, boardConnectionString, Constants.BOARD_SOCKET_NAME);
		try {
			this.boardClient.connect();
		} catch (IOException e) {
			LOGGER.log(Level.SEVERE, "Failed to connect to board", e);
			this.setErrorStateAndSendError("Failed to connect to board.");
		}
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

	private void setErrorStateAndSendError(String errMsg) {
		LOGGER.log(Level.SEVERE, "setErrorStateAndSendError called: {0}", errMsg);
		this.stateMachine.setErrorState();
		this.sendTextToGuiSocket(this.getErrorMessage(errMsg));
		this.reInit();
	}

	public static boolean isDirection(final String msgID) {
		return Arrays.asList(Constants.DIRECTIONS).contains(msgID);
	}

	private void sendRoverDirection(final String direction) {
		this.roverClient.sendMessage(direction, false);
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
		disconnectRover();
		disconnectBoard();
		this.stateMachine = new GameServerStateMachine(state);
		this.currentGame = new Game();
	}

	public long getBatteryPercentage() {
		return this.percentageBatteryLeft;
	}

	public float getBatteryVoltage() {
		return this.batteryVoltage;
	}

}
