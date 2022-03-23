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
package io.openliberty.spacerover.game.websocket.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import io.openliberty.spacerover.game.websocket.server.GameServerConstants;
import jakarta.websocket.DeploymentException;

public class GameSocketTest {
	private static final String port = System.getProperty("http.port", "9070");
//	private static final String context = System.getProperty("context.root", "");
	// private static final String url = "ws://spacesentry1.fyre.ibm.com:" + port;
	private static final String url = "ws://localhost:" + port;
	private static final String PLAYER_ID = "player1";
	public static final String END_GAME = "endGame";
	public static final String START_GAME = "startGame";
	public static final String SOCKET_MESSAGE_DATA_DELIMITER = "|";

	@Test
	public void testSocket() throws DeploymentException, IOException, URISyntaxException, InterruptedException {

		URI uri = new URI(url + "/" + GameServerConstants.WEBSOCKET_ENDPOINT);
		System.out.println("Attempting socket connection to: " + uri);
		WebsocketClientEndpoint endpoint = new WebsocketClientEndpoint(uri);

		// add listener
		endpoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
			public void handleMessage(String message) {
				System.out.println(message);
			}
		});
		Thread.sleep(10000);
		endpoint.sendMessage(START_GAME + SOCKET_MESSAGE_DATA_DELIMITER + PLAYER_ID);
		endpoint.sendMessage("forward");
		endpoint.sendMessage("backward");
		endpoint.sendMessage("left");
		endpoint.sendMessage("right");
		
		endpoint.sendMessage(END_GAME);

	}
}
