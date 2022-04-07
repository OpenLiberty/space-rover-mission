package io.openliberty.spacerover.client;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.openliberty.spacerover.game.models.GameScore;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public interface LeaderboardClient {
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", description = "Successfully added player to leaderboard.")
	@APIResponse(responseCode = "400", description = "Invalid leaderboard entry.")
	@Operation(summary = "Add a new entry to the leaderboard.")
	@POST
	@Path("/")
	public Response add(GameScore entry);

    @Produces(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", description = "Successfully listed the leaderboard.")
	@APIResponse(responseCode = "500", description = "Failed to list the leaderboard.")
	@Operation(summary = "List the leaderboard from the database.")
	@GET
	@Path("/")
	public List<GameScore> retrieve();
}
