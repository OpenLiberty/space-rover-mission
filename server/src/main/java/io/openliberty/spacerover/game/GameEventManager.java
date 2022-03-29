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
package io.openliberty.spacerover.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.openliberty.spacerover.game.models.GameEvent;

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

	    public void notify(GameEvent eventType, int details) {
	        List<GameEventListener> users = listeners.get(eventType);
	        for (GameEventListener listener : users) {
	            listener.update(eventType, details);
	        }
	    }
	    
}
