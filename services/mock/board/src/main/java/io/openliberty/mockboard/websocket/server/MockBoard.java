/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.mockboard.websocket.server;

import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

@ApplicationScoped
@ServerEndpoint(value = "/")
public class MockBoard {
	private static final Logger LOGGER = Logger.getLogger(MockBoard.class.getName());

	@OnOpen
	public void onOpen(final Session session, @PathParam("path") final String path) {
		LOGGER.log(Level.WARNING, "Websocket open! client {0} connected on path {1} timeout: {2}, params{3}",
				new Object[] { session.getId(), path, session.getMaxIdleTimeout(), session.getRequestParameterMap() });
		String welcomeText = "Board Connected";
		session.getAsyncRemote().sendText(welcomeText);
	}

	@OnClose
	public void onClose(final Session session, final CloseReason reason) {
		LOGGER.log(Level.WARNING, "Websocket closed! client ID: {0} close reason: {1} close reason phrase: {2}",
				new Object[] { session.getId(), reason, reason.getReasonPhrase() });
	}

	@OnMessage
	public void receiveMessage(final String message, final Session session) {
		LOGGER.log(Level.WARNING, "Message Recieved: {0}", message);
	}
}
