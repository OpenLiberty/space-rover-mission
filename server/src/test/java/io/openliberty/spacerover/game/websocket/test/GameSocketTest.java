package io.openliberty.spacerover.game.websocket.test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.junit.jupiter.api.Test;

import jakarta.websocket.DeploymentException;

public class GameSocketTest {
	private static final String port = System.getProperty("http.port", "9080");
//	private static final String context = System.getProperty("context.root", "");
	private static final String url = "ws://spacesentry1.fyre.ibm.com:" + port;
//	private static final String url = "ws://localhost:" + port;
	private static final String PLAYER_ID = "player1";
	public static final String END_GAME = "endGame";
	public static final String START_GAME = "startGame";
	public static final String SOCKET_MESSAGE_DATA_DELIMITER = "|";

	@Test
	public void testSocket() throws DeploymentException, IOException, URISyntaxException {

		URI uri = new URI(url + "/" + "roversocket");
		System.out.println("Attempting socket connection to: " + uri);
		WebsocketClientEndpoint endpoint = new WebsocketClientEndpoint(uri);

		// add listener
		endpoint.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
			public void handleMessage(String message) {
				System.out.println(message);
			}
		});

		endpoint.sendMessage(START_GAME + SOCKET_MESSAGE_DATA_DELIMITER + PLAYER_ID);
		endpoint.sendMessage(END_GAME);

	}
}
