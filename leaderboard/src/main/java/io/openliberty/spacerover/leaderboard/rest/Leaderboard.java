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

import org.bson.Document;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.openliberty.spacerover.leaderboard.models.LeaderboardEntry;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import jakarta.ws.rs.Consumes;
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
		StringWriter sb = new StringWriter();

		try {
			MongoCollection<Document> collection = db.getCollection(LEADERBOARD_COLLECTION_NAME);
			sb.append("[");
			boolean first = true;
			FindIterable<Document> docs = collection.find().sort(new BasicDBObject("score", -1).append("time", 1));
			for (Document d : docs) {
				if (!first)
					sb.append(",");
				else
					first = false;
				sb.append(d.toJson());
			}
			sb.append("]");
		} catch (Exception e) {
			e.printStackTrace(System.out);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("[\"Unable to list leaderboard!\"]")
					.build();
		}

		return Response.status(Response.Status.OK).entity(sb.toString()).build();
	}
}