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
package io.openliberty.rover.services.game;

import static io.openliberty.rover.services.game.Constants.PATH_GAME;
import static io.openliberty.rover.services.game.Constants.PATH_GAME_END;
import static io.openliberty.rover.services.game.Constants.PATH_GAME_NEW;
import static io.openliberty.rover.services.game.Constants.PATH_GAME_RESULT;

import org.eclipse.microprofile.metrics.MetricRegistry;

import io.openliberty.rover.services.game.model.Game;
import io.openliberty.rover.services.game.model.User;
import jakarta.inject.Inject;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path(PATH_GAME)
public class GameResource {

    
    @Inject
    Game game;
    
    @Inject
    MetricRegistry registry;
    
    @POST
    @Path(PATH_GAME_NEW)
    @Consumes(MediaType.TEXT_PLAIN)
    @Produces(MediaType.APPLICATION_JSON)
    public Response newGame(String playerId){
        game.startGameSession(playerId);
        return Response.ok().build();
    }

    @POST
    @Path(PATH_GAME_END)
    @Consumes(MediaType.TEXT_PLAIN)
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
    @Path(PATH_GAME_RESULT)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getResult() {
        User user = new User(game.getPlayerId());
        user.setDuration(game.getGameDuration());
        user.setScore(0);
        return Response.ok(JsonbBuilder.create().toJson(user)).build();
    }
}
