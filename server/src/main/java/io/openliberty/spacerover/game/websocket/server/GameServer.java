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

import io.openliberty.spacerover.game.Constants;
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

@ApplicationScoped
@ServerEndpoint(value = "/roversocket")
public class GameServer implements GameEventListener, io.openliberty.spacerover.game.websocket.client.MessageHandler {

	Game currentGame = GameHolder.INSTANCE;
	GameServerStateMachine stateMachine = new GameServerStateMachine();
	Session guiSession = null;
	Session gestureSession = null;
	WebsocketClientEndpoint roverClient = null;
	WebsocketClientEndpoint boardClient = null;

	@OnOpen
	public void onOpen(Session session, @PathParam("path") String path) {
		// (lifecycle) Called when the connection is opened
		System.out.println("Websocket open! client: " + session.getId() + " connected on path " + path + " timeout: "
				+ session.getMaxIdleTimeout());
		session.getAsyncRemote().sendText("Welcome space explorer!");
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) throws IOException {
		// (lifecycle) Called when the connection is closed
		String sessionID = session.getId();
		System.out.println(
				"Websocket closed! " + sessionID + " reason: " + reason.toString() + " " + reason.getReasonPhrase());

		GameSession sessionName = getGameSession(session);
		if (this.currentGame.isInProgress()) {
			if (sessionName == GameSession.GUI || sessionName == GameSession.GESTURE) {
				waitForSessionsToReconnect();
				if (!this.guiSession.isOpen() || !this.gestureSession.isOpen()) {
					System.out.println("ending game after waiting for reconnect");
					this.currentGame.endGameSession();
				}
			}
		}
		System.out.println("Lost connection with " + sessionName);
	}

	private void waitForSessionsToReconnect() {
		for (int i = 0; i < 5; i++) {
			if (!this.guiSession.isOpen() || !this.gestureSession.isOpen()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private GameSession getGameSession(Session session) {
		String sessionID = session.getId();
		GameSession detectedSession = GameSession.UNKNOWN;
		// TODO Auto-generated method stub
		if (sessionID == guiSession.getId()) {
			detectedSession = GameSession.GUI;

		} else if (sessionID == gestureSession.getId()) {
			detectedSession = GameSession.GESTURE;

		}
		return detectedSession;
	}

	@OnMessage
	public void receiveMessage(String message, Session session) throws Exception {
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
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.stateMachine.attachRover();
		return client;
	}

	private void startGame(String[] parsedMsg) {
		this.currentGame.startGameSession(parsedMsg[1]);
		this.currentGame.getEventManager().subscribe(GameEvent.HP, this);
		this.currentGame.getEventManager().subscribe(GameEvent.SCORE, this);
	}

	@OnError
	public void onError(Throwable t) throws Throwable {
		// (lifecycle) Called if/when an error occurs and the connection is disrupted
		t.printStackTrace();
	}

	@Override
	public void update(GameEvent eventType, long value) {
		if (this.guiSession.isOpen()) {
			try {
				this.guiSession.getBasicRemote()
						.sendText(eventType.toString() + Constants.SOCKET_MESSAGE_DATA_DELIMITER + value);
			} catch (IOException ioe) {
				System.out.println(ioe);
			}

		} else {
			System.out.println("update game event failed because session is closed");
		}
	}

	@Override
	public void handleMessage(String message) {
		handleMessage(message, null);
	}

	public void handleMessage(String message, Session session) {
		System.out.println("Message received! " + message);
		String[] parsedMsg = message.split("\\" + Constants.SOCKET_MESSAGE_DATA_DELIMITER);
		String msgID = parsedMsg[0];

		if (this.stateMachine.isValidState(msgID)) {
			try {
				if (msgID.equals(Constants.CONNECT_GUI)) {
					this.guiSession = session;
				} else if (msgID.equals(Constants.CONNECT_GESTURE)) {
					this.gestureSession = session;
				} else if (msgID.equals(Constants.START_GAME)) {
					System.out.println("Start Game received for player ID: " + parsedMsg[1]);
					startGame(parsedMsg);
					this.roverClient.sendMessage(Constants.START_GAME);
					this.guiSession.getAsyncRemote().sendText("Game Started!");
				} else if (msgID.equals(Constants.END_GAME)) {
					System.out.println("Stop Game received");
					this.currentGame.endGameSession();
					this.roverClient.sendMessage(Constants.END_GAME);
//					this.roverClient.disconnect(); TODO we dont really want to disconnect from the rover and board here do we? We can leave them on but let them know the game's over.
					// TODO update leaderboard here
				}
				else if (isDirection(msgID)) {
					this.sendRoverDirection(msgID);
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
				// TODO inform gui something went wrong
			}
			this.stateMachine.incrementState(msgID);

			if (this.stateMachine.isReadyToConnectGamePieces()) {
				this.roverClient = connectRoverClient();
				this.boardClient = connectBoardClient();
				connectLeaderboard();
				this.guiSession.getAsyncRemote().sendText(Constants.SERVER_READY);
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
		return Arrays.asList(Constants.DIRECTIONS).contains(msgID);
	}

	private void sendRoverDirection(String direction) throws IOException {
		this.roverClient.sendMessage(direction);
	}

	private static class GameHolder {
		static final Game INSTANCE = new Game();
	}

}
