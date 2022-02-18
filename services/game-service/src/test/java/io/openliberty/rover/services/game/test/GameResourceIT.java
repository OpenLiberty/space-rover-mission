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
package io.openliberty.rover.services.game.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static io.openliberty.rover.services.game.Constants.PATH_GAME;
import static io.openliberty.rover.services.game.Constants.PATH_GAME_NEW;
import static io.openliberty.rover.services.game.Constants.PATH_GAME_END;
import static io.openliberty.rover.services.game.Constants.PATH_GAME_RESULT;

import org.junit.jupiter.api.Test;

import io.openliberty.rover.services.game.model.User;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

public class GameResourceIT {
    
    private static final Jsonb JSONB = JsonbBuilder.create();
    private static final String port = System.getProperty("http.port");
    private static final String context = System.getProperty("context.root");
    private static final String url = "http://localhost:" + port + "/" + context + "/";
    private static final String PLAYER_ID = "player1";
    
    @Test
    public void testGame() {

        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(url + PATH_GAME + "/" + PATH_GAME_NEW);
        Response response = target.request().post(Entity.text(PLAYER_ID));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                     "Incorrect response code from " + url);

        target = client.target(url + PATH_GAME + "/" + PATH_GAME_END);
        response = target.request().post(Entity.text(""));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                     "Incorrect response code from " + url);
        
        response.close();
    }

    @Test
    public void testGetResult() {

        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(url + PATH_GAME + "/" + PATH_GAME_RESULT);
        Response response = target.request().get();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                     "Incorrect response code from " + url);

        String json = response.readEntity(String.class);
        User user = JSONB.fromJson(json, User.class);
        assertEquals(PLAYER_ID, user.getName(), "Player name does not match");
        assertTrue(user.getDuration() > 0, "Game duration should not be 0");
        
        response.close();
    }

}
