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

import java.util.Set;
import java.util.StringJoiner;

import static com.mongodb.client.model.Filters.*;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.metrics.annotation.Counted;

import io.openliberty.spacerover.leaderboard.models.LeaderboardConstants;
import io.openliberty.spacerover.leaderboard.models.LeaderboardEntry;

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
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@ApplicationScoped
@Path("/")
public class Leaderboard {


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

	@Counted(name = "leaderboardRecordAdded", absolute = true, description = "Number of times that new records were sent to the leaderboard.")
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
		MongoCollection<Document> document = db.getCollection(LeaderboardConstants.LEADERBOARD_COLLECTION_NAME);

		Document newLeaderboardEntry = new Document();
		newLeaderboardEntry.put(LeaderboardConstants.MONGO_LEADERBOARD_PLAYER, entry.getPlayer());
		newLeaderboardEntry.put(LeaderboardConstants.MONGO_LEADERBOARD_SCORE, entry.getScore());
		newLeaderboardEntry.put(LeaderboardConstants.MONGO_LEADERBOARD_TIME, entry.getTime());
		newLeaderboardEntry.put(LeaderboardConstants.MONGO_LEADERBOARD_HEALTH, entry.getHealth());
		newLeaderboardEntry.put(LeaderboardConstants.MONGO_LEADERBOARD_TIMESTAMP, Long.toString(System.currentTimeMillis()));

		document.insertOne(newLeaderboardEntry);

		return Response.status(Response.Status.OK).entity(newLeaderboardEntry.toJson()).build();
	}

	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", description = "Successfully listed the leaderboard.")
	@APIResponse(responseCode = "500", description = "Failed to list the leaderboard.")
	@Operation(summary = "List the leaderboard from the database.")
	@GET
	@Path("/")
	public Response retrieve(@DefaultValue(LeaderboardConstants.QUERY_PARAM_START_TIME_DEFAULT_VALUE) @QueryParam(LeaderboardConstants.QUERY_PARAM_START_TIME) String startTime,
			@DefaultValue(LeaderboardConstants.QUERY_PARAM_END_TIME_DEFAULT_VALUE) @QueryParam(LeaderboardConstants.QUERY_PARAM_END_TIME) String endTime) {
		try {
			if (endTime.isEmpty()) {
				endTime = Long.toString(System.currentTimeMillis());
			}
			MongoCollection<Document> collection = db.getCollection(LeaderboardConstants.LEADERBOARD_COLLECTION_NAME);
			FindIterable<Document> docs = collection.find(and(gte(LeaderboardConstants.MONGO_LEADERBOARD_TIMESTAMP, startTime), lte(LeaderboardConstants.MONGO_LEADERBOARD_TIMESTAMP, endTime)));
			docs.sort(new BasicDBObject(LeaderboardConstants.MONGO_LEADERBOARD_TIMESTAMP, 1));
			String output = getJsonListFromIterableDocuments(docs);
			return Response.status(Response.Status.OK).entity(output).build();
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
	public Response clear(@DefaultValue(LeaderboardConstants.QUERY_PARAM_PLAYER_NAME_DEFAULT) @QueryParam(LeaderboardConstants.QUERY_PARAM_PLAYER_NAME) String playerName) {
		MongoCollection<Document> collection = db.getCollection(LeaderboardConstants.LEADERBOARD_COLLECTION_NAME);
		DeleteResult result;
		if (playerName.isEmpty()) {
			result = collection.deleteMany(new Document());
		} else {
			result = collection.deleteMany(eq(LeaderboardConstants.MONGO_LEADERBOARD_PLAYER, playerName));
		}
		if (result.wasAcknowledged()) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("[\"Unable to clear leaderboard!\"]")
					.build();
		}

	}
	
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", description = "Successfully cleared the leaderboard.")
	@APIResponse(responseCode = "500", description = "Failed to clear the leaderboard.")
	@Operation(summary = "Clear the leaderboard entries from the database.")
	@DELETE
	@Path("/_id")
	public Response clearSingleEntryWithID(@QueryParam(LeaderboardConstants.QUERY_PARAM_ID) String documentID) {
		MongoCollection<Document> collection = db.getCollection(LeaderboardConstants.LEADERBOARD_COLLECTION_NAME);
		DeleteResult result;
		result = collection.deleteOne(new Document(LeaderboardConstants.MONGO_LEADERBOARD_DOCUMENT_ID, new ObjectId(documentID)));
		if (result.wasAcknowledged()) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("[\"Unable to clear leaderboard!\"]")
					.build();
		}

	}

}