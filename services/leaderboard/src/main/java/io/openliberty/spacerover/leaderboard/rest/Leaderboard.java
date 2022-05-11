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
package io.openliberty.spacerover.leaderboard.rest;

import java.io.StringWriter;
import java.util.Set;
import java.util.StringJoiner;

import static com.mongodb.client.model.Filters.*;
import org.bson.Document;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.openliberty.spacerover.leaderboard.models.LeaderboardDuration;
import io.openliberty.spacerover.leaderboard.models.LeaderboardEntry;
import io.openliberty.spacerover.leaderboard.models.PlayerID;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/")
public class Leaderboard {

	private static final String LEADERBOARD_COLLECTION_NAME = "Leaderboard";

	@Inject
	MongoDatabase db;

	@Inject
	Validator validator;

	private JsonArray getViolations(LeaderboardEntry entry) {
		Set<ConstraintViolation<LeaderboardEntry>> violations = validator.validate(entry);

		JsonArrayBuilder messages = Json.createArrayBuilder();

		for (ConstraintViolation<LeaderboardEntry> v : violations) {
			messages.add(v.getMessage());
		}

		return messages.build();
	}

	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@APIResponse(responseCode = "200", description = "Successfully added player to leaderboard.")
	@APIResponse(responseCode = "400", description = "Invalid leaderboard entry.")
	@Operation(summary = "Add a new entry to the leaderboard.")
	@POST
	@Path("/")
	public Response add(LeaderboardEntry entry) {
		JsonArray violations = getViolations(entry);

		if (!violations.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).entity(violations.toString()).build();
		}
		MongoCollection<Document> document = db.getCollection(LEADERBOARD_COLLECTION_NAME);

		Document newLeaderboardEntry = new Document();
		newLeaderboardEntry.put("player", entry.getPlayer());
		newLeaderboardEntry.put("score", entry.getScore());
		newLeaderboardEntry.put("time", entry.getTime());
		newLeaderboardEntry.put("health", entry.getHealth());
		newLeaderboardEntry.put("timestamp", Long.toString(System.currentTimeMillis()));

		document.insertOne(newLeaderboardEntry);

		return Response.status(Response.Status.OK).entity(newLeaderboardEntry.toJson()).build();
	}

	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", description = "Successfully listed the leaderboard.")
	@APIResponse(responseCode = "500", description = "Failed to list the leaderboard.")
	@Operation(summary = "List the leaderboard from the database.")
	@GET
	@Path("/")
	public Response retrieve() {
		try {
			MongoCollection<Document> collection = db.getCollection(LEADERBOARD_COLLECTION_NAME);
			FindIterable<Document> docs = collection.find().sort(new BasicDBObject("score", -1).append("time", 1));
			String jsonOutput = getJsonListFromIterableDocuments(docs);
			return Response.status(Response.Status.OK).entity(jsonOutput).build();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("[\"Unable to list leaderboard!\"]")
					.build();
		}

	}

	private String getJsonListFromIterableDocuments(FindIterable<Document> docs) {
		StringJoiner sj = new StringJoiner(",");
		for (Document d : docs) {
			sj.add(d.toJson());
		}
		return "[" + sj.toString() + "]";
	}

	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", description = "Successfully cleared the leaderboard.")
	@APIResponse(responseCode = "500", description = "Failed to clear the leaderboard.")
	@Operation(summary = "Clear the leaderboard entries from the database.")
	@DELETE
	@Path("/")
	public Response clear() {
		MongoCollection<Document> collection = db.getCollection(LEADERBOARD_COLLECTION_NAME);
		DeleteResult result = collection.deleteMany(new Document());
		if (result.wasAcknowledged()) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("[\"Unable to clear leaderboard!\"]")
					.build();
		}

	}

	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", description = "Deleted user entries from the leaderboard.")
	@APIResponse(responseCode = "500", description = "Failed to delete user entry from the leaderboard.")
	@Operation(summary = "Deletes a user's entries from the leaderboard.")
	@DELETE
	@Path("/")
	public Response remUser(PlayerID playerID) {
		MongoCollection<Document> collection = db.getCollection(LEADERBOARD_COLLECTION_NAME);
		String pID = playerID.getPlayer();
		DeleteResult result = collection.deleteMany(eq("player", pID));
		if (result.wasAcknowledged()) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("[\"Unable to delete " + pID + "from leaderboard!\"]").build();
		}

	}

	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", description = "Selected records from a specific duration from the leaderboard db.")
	@APIResponse(responseCode = "500", description = "Failed to query database.")
	@Operation(summary = "Selects records from a specific duration from the leaderboard db.")
	@GET
	@Path("/")
	public Response getStatisticsFromDuration(LeaderboardDuration duration) {
		try {
			MongoCollection<Document> collection = db.getCollection(LEADERBOARD_COLLECTION_NAME);
			FindIterable<Document> docs = collection
					.find(and(gte("timestamp", duration.getStartTime()), lte("timestamp", duration.getEndTime())));
			docs.sort(new BasicDBObject("timestamp", 1));
			String output = getJsonListFromIterableDocuments(docs);
			return Response.status(Response.Status.OK).entity(output).build();
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("[\"Unable to list leaderboard!\"]")
					.build();
		}

	}

}