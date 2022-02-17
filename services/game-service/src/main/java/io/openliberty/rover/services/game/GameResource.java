package io.openliberty.rover.services.game;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.microprofile.metrics.MetricRegistry;

import io.openliberty.rover.services.game.model.Game;
import io.openliberty.rover.services.game.model.User;

@Path("game")
public class GameResource {

    @Inject
    Game game;
    
    @Inject
    MetricRegistry registry;
    
    @POST
    @Path("new")
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newGame(String playerId){
        game.startGameSession(playerId);
        return Response.ok().build();
    }

    @POST
    @Path("end")
    @Produces(MediaType.APPLICATION_JSON)
    public Response endGame() {
        try {
            game.endGameSession();
        } catch (IllegalStateException e) {
            return Response.serverError().build();
        }
        return Response.ok().build();
    }

    @GET
    @Path("result")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResult() {
        User user = new User(game.getPlayerId());
        user.setDuration(game.getGameDuration());
        user.setScore(0);
        return Response.ok(user).build();
    }
}
