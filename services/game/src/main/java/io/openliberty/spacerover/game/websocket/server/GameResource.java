/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.spacerover.game.websocket.server;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.openliberty.spacerover.game.models.GameMode;
import io.openliberty.spacerover.game.models.Constants;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/modes")
public class GameResource {

	@Produces(MediaType.APPLICATION_JSON)

	@APIResponse(responseCode = "200", description = "Successfully returned list of supported game modes")
	@APIResponse(responseCode = "400", description = "Failed to return a list of supported game modes")
	@Operation(summary = "Returns a list of supported game modes from this game server.")
	@GET
	@Path("/")
	@Retry(maxRetries = 5)
	public Response retrieve() {
		List<GameMode> supportedGameModes = new ArrayList<>();
		GameMode classic = new GameMode();
		classic.setGameModeID(Integer.parseInt(Constants.INIT_GAME_CLASSIC));
		classic.setGameModeName(Constants.GAME_MODE_NAME_CLASSIC);
		classic.setDescription(Constants.GAME_MODE_DESC_CLASSIC);
		supportedGameModes.add(classic);

		GameMode planetHop = new GameMode();
		planetHop.setGameModeID(Integer.parseInt(Constants.INIT_GAME_HOP));
		planetHop.setGameModeName(Constants.GAME_MODE_NAME_PLANET_HOP);
		planetHop.setDescription(Constants.GAME_MODE_DESC_PLANET_HOP);
		supportedGameModes.add(planetHop);
		
		GameMode guided = new GameMode();
		guided.setGameModeID(Integer.parseInt(Constants.INIT_GAME_GUIDED));
		guided.setGameModeName(Constants.GAME_MODE_NAME_GUIDED);
		guided.setDescription(Constants.GAME_MODE_DESC_GUIDED);

		supportedGameModes.add(guided);
		
		GameMode suddenDeath = new GameMode();
		suddenDeath.setGameModeID(Integer.parseInt(Constants.INIT_GAME_SUDDEN_DEATH));
		suddenDeath.setGameModeName(Constants.GAME_MODE_NAME_SUDDENDEATH);
		suddenDeath.setDescription(Constants.GAME_MODE_DESC_SUDDENDEATH);

		supportedGameModes.add(suddenDeath);
		
		Jsonb jsonb = JsonbBuilder.create();
		return Response.status(Response.Status.OK).entity(jsonb.toJson(supportedGameModes)).build();

	}
}
