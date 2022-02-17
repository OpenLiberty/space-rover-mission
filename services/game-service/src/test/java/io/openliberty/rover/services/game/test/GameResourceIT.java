package io.openliberty.rover.services.game.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;

import io.openliberty.rover.services.game.model.User;

public class GameResourceIT {
    
    private static final Jsonb JSONB = JsonbBuilder.create();
    private static final String port = System.getProperty("http.port");
    private static final String context = System.getProperty("context.root");
    private static final String url = "http://localhost:" + port + "/" + context + "/";
    private static final String PLAYER_ID = "player1";
    
    @Test
    public void testNewGame() {

        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(url + "game/new");
        Response response = target.request().post(Entity.text(PLAYER_ID));

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                     "Incorrect response code from " + url);

        response.close();
    }
    
    @Test
    public void testEndGame() {

        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(url + "game/end");
        Response response = target.request().post(null);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
                     "Incorrect response code from " + url);
        
        response.close();
    }

    @Test
    public void testGetResult() {

        Client client = ClientBuilder.newClient();

        WebTarget target = client.target(url + "game/result");
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
