// tag::copyright[]
/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
// end::copyright[]
package io.openliberty.spacerover.messenger.rest;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeMap;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class MessengerManager {

    private Map<String, Properties> systems = Collections.synchronizedMap(new TreeMap<String, Properties>());

    public void addLeaderboardEvent(String hostname, String action, String payload) {
        if (!systems.containsKey(hostname)) {
            Properties p = new Properties();
            p.put("hostname", hostname);
            p.put("leaderboardEvent.action", action);
            p.put("leaderboardEvent.payload", payload);
            systems.put(hostname, p);
        }
    }

    public void updateLeaderboardEvent(String hostname, String action, String payload) {
        Optional<Properties> p = getSystem(hostname);
        if (p.isPresent()) {
            if (p.get().getProperty(hostname) == null && hostname != null) {
                p.get().put("leaderboardEvent.action", action);
                p.get().put("leaderboardEvent.payload", payload);
            }
        }
    }

    public Optional<Properties> getSystem(String hostname) {
        Properties p = systems.get(hostname);
        return Optional.ofNullable(p);
    }

    public Map<String, Properties> getSystems() {
        return new TreeMap<>(systems);
    }

    public void resetSystems() {
        systems.clear();
    }
}