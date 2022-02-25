package io.openliberty.spacerover.game.websocket;

import java.io.IOException;

import io.openliberty.spacerover.game.Constants;
import io.openliberty.spacerover.game.Game;
import io.openliberty.spacerover.game.GameEvent;
import io.openliberty.spacerover.game.GameEventListener;
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
public class GameSocket implements GameEventListener {

	Game currentGame = null;
	Session lastSession = null;

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

		String[] parsedMsg = message.split("\\" + Constants.SOCKET_MESSAGE_DATA_DELIMITER);
		String msgID = parsedMsg[0];

		if (msgID.equals(Constants.START_GAME)) {
			System.out.println("Start Game received for player ID: " + parsedMsg[1]);
			this.currentGame.startGameSession(parsedMsg[1]);
			this.currentGame.getEventManager().subscribe(GameEvent.HP, this);
			this.currentGame.getEventManager().subscribe(GameEvent.SCORE, this);
		} else if (msgID.equals(Constants.END_GAME)) {
			System.out.println("Stop Game received");
			this.currentGame.endGameSession();
			// TODO update leaderboard here
		}
		if (!session.equals(this.lastSession)) {
			this.lastSession = session;
		}
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

}
