/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.spacerover.game.websocket.client;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.openliberty.spacerover.game.GameEventManager;
import io.openliberty.spacerover.game.models.GameEvent;

@ClientEndpoint
public class WebsocketClientEndpoint {

	private static final Logger LOGGER = Logger.getLogger(WebsocketClientEndpoint.class.getName());
	Session userSession = null;
	private io.openliberty.spacerover.game.websocket.client.MessageHandler messageHandler;
	private GameEventManager manager;
	private String uri;
	private String name;

	public WebsocketClientEndpoint(String uri) {
		this(uri, "Unnamed");
	}

	public WebsocketClientEndpoint(String uri, String name) {
		this(null, uri, name);
	}

	public WebsocketClientEndpoint(URI uri) {
		this(uri.toString());
	}

	public WebsocketClientEndpoint(io.openliberty.spacerover.game.websocket.client.MessageHandler handler,
			String uriStr, String name) {
		manager = new GameEventManager(GameEvent.SOCKET_DISCONNECT);
		this.messageHandler = handler;
		this.uri = uriStr;
		this.name = name;
	}

	public GameEventManager getEventManager() {
		return manager;
	}

	public String getName() {
		return name;
	}

	public void connect() throws IOException {
		LOGGER.log(Level.WARNING, "Connecting to {0} websocket client on URI {1}",
				new Object[] { this.name, this.uri });
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		try {
			container.connectToServer(this, new URI(this.uri));
		} catch (DeploymentException | URISyntaxException e) {
			throw new IOException(e);
		}
	}

	/**
	 * Callback hook for Connection open events.
	 *
	 * @param userSession the userSession which is opened.
	 */
	@OnOpen
	public void onOpen(Session userSession) {
		this.userSession = userSession;
	}

	/**
	 * Callback hook for Connection close events.
	 *
	 * @param userSession the userSession which is getting closed.
	 * @param reason      the reason for connection close
	 */
	@OnClose
	public void onClose(Session userSession, CloseReason reason) {
		LOGGER.log(Level.WARNING, "{0} Websocket with URI {1} has closed.", new Object[] { this.name, this.uri });
		this.manager.notify(GameEvent.SOCKET_DISCONNECT, reason.getCloseCode().getCode());
		this.userSession = null;
	}

	/**
	 * Callback hook for Message Events. This method will be invoked when a client
	 * send a message.
	 *
	 * @param message The text message
	 */
	@OnMessage
	public void onMessage(String message) {
		if (this.messageHandler != null) {
			this.messageHandler.handleMessage(message);
		} else {
			LOGGER.log(Level.WARNING, "message handler is null");
		}
	}

	@OnMessage
	public void onMessage(ByteBuffer bytes) {
		LOGGER.log(Level.WARNING, "got a byte buffer message");
	}

	/**
	 * Send a message.
	 *
	 * @param message
	 * @throws IOException
	 */
	public void sendMessage(String message) {
		this.sendMessage(message, false);
	}

	public void sendMessage(String message, boolean isAsync) {
		if (this.userSession != null && this.userSession.isOpen()) {
			try {
				if (isAsync) {
					this.userSession.getAsyncRemote().sendText(message);

				} else {
					this.userSession.getBasicRemote().sendText(message);
				}
				LOGGER.log(Level.INFO, "Sent Message {0}", message);
			} catch (IOException e) {
				LOGGER.log(Level.SEVERE, "failed to send message {0}", message);
			}
		}
	}

	/**
	 * register message handler
	 *
	 * @param msgHandler
	 */
	public void addMessageHandler(io.openliberty.spacerover.game.websocket.client.MessageHandler msgHandler) {
		this.messageHandler = msgHandler;
	}

	public synchronized void disconnect() throws IOException {
		if (this.userSession != null && this.userSession.isOpen()) {
			this.userSession.close();
		}
	}
}
