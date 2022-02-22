package io.openliberty.spacerover.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameEventManager {

	 Map<GameEvent, List<GameEventListener>> listeners = new HashMap<>();

	    public GameEventManager(GameEvent... operations) {
	        for (GameEvent operation : operations) {
	            this.listeners.put(operation, new ArrayList<>());
	        }
	    }

	    public void subscribe(GameEvent eventType, GameEventListener listener) {
	        List<GameEventListener> users = listeners.get(eventType);
	        users.add(listener);
	    }

	    public void unsubscribe(GameEvent eventType, GameEventListener listener) {
	        List<GameEventListener> users = listeners.get(eventType);
	        users.remove(listener);
	    }

	    public void notify(GameEvent eventType, long file) {
	        List<GameEventListener> users = listeners.get(eventType);
	        for (GameEventListener listener : users) {
	            listener.update(eventType, file);
	        }
	    }
	    
}
