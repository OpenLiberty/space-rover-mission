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

import io.openliberty.spacerover.game.Constants;
import io.openliberty.spacerover.game.Game;
import io.openliberty.spacerover.game.GameEvent;
import io.openliberty.spacerover.game.GameEventListener;
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
public class GameServer implements GameEventListener, io.openliberty.spacerover.game.websocket.server.MessageHandler {

	Game currentGame = null;
	Session lastSession = null;
	WebsocketClientEndpoint roverClient = null;
	WebsocketClientEndpoint boardClient = null;

	@OnOpen
	public void onOpen(Session session, @PathParam("path") String path) throws Exception {
		// (lifecycle) Called when the connection is opened
		System.out.println("Websocket open! client: " + session.getId() + " connected on path " + path + " timeout: "
				+ session.getMaxIdleTimeout());
		session.getAsyncRemote().sendText("Welcome space explorer!");
		if (this.currentGame == null) {
			this.currentGame = new Game();
		} else {
			System.out.println("Websocket opened while a game is running, opening socket for game with player ID: "
					+ this.currentGame.getPlayerId());
		}
	}

	@OnClose
	public void onClose(Session session, CloseReason reason) throws IOException {
		// (lifecycle) Called when the connection is closed
		System.out.println("Websocket closed! " + session.getId() + " reason: " + reason.toString() + " "
				+ reason.getReasonPhrase());

		if (this.currentGame.isInProgress()) {
			try {
				this.currentGame.endGameSession();
				System.out.println("Forcively ended game on socket close!");
			} catch (IllegalStateException ise) {
				// intentionally empty
				System.out.println(ise);
			}
		}

		this.currentGame = null;
		this.lastSession = null;
	}

	@OnMessage
	public void receiveMessage(String message, Session session) throws Exception {
		// Called when a message is received.
		System.out.println("Got a message: '" + message + "'");
		this.handleMessage(message);
		if (!session.equals(this.lastSession)) {
			this.lastSession = session;
		}
	}

	private WebsocketClientEndpoint connectRoverClient() {
		String roverIP = System.getProperty("io.openliberty.spacerover.ip", "192.168.0.111");
		String roverPort = System.getProperty("io.openliberty.spacerover.port", "5045");
		WebsocketClientEndpoint client = null;
		try {
			URI uri = new URI("ws://" + roverIP + ":" + roverPort);
			client = new WebsocketClientEndpoint(uri);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		client.addMessageHandler(this);
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
		if (this.lastSession.isOpen()) {
			try {
				this.lastSession.getBasicRemote()
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
		String[] parsedMsg = message.split("\\" + Constants.SOCKET_MESSAGE_DATA_DELIMITER);
		String msgID = parsedMsg[0];

		try {
		if (msgID.equals(Constants.START_GAME)) {
			System.out.println("Start Game received for player ID: " + parsedMsg[1]);
			startGame(parsedMsg);
			this.roverClient = connectRoverClient();
//			connectToGameBoard();
		} else if (msgID.equals(Constants.END_GAME)) {
			System.out.println("Stop Game received");
			this.currentGame.endGameSession();
			// TODO update leaderboard here
		}
		else if (msgID.equals(Constants.FORWARD))
		{
			this.roverClient.sendMessage("F");
			this.roverClient.sendMessage("S");
			
		}
		else if (msgID.equals(Constants.BACKWARD))
		{
			this.roverClient.sendMessage("B");
			this.roverClient.sendMessage("S");
		}
		else if (msgID.equals(Constants.LEFT))
		{
			this.roverClient.sendMessage("L");
			this.roverClient.sendMessage("S");
		}
		else if (msgID.equals(Constants.RIGHT))
		{
			this.roverClient.sendMessage("R");
			this.roverClient.sendMessage("S");
		}}
		catch (IOException ioe)
		{
			ioe.printStackTrace();
			// inform gui something went wrong
		}
	}

}
