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

		GameMode freeRoam = new GameMode();
		freeRoam.setGameModeID(Integer.parseInt(Constants.INIT_GAME_FREE_ROAM));
		freeRoam.setGameModeName(Constants.GAME_MODE_NAME_FREE_ROAM);
		freeRoam.setDescription(Constants.GAME_MODE_DESC_FREE_ROAM);
		supportedGameModes.add(freeRoam);

		Jsonb jsonb = JsonbBuilder.create();
		return Response.status(Response.Status.OK).entity(jsonb.toJson(supportedGameModes)).build();
	}
}
