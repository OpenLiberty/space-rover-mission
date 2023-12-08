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
import org.eclipse.microprofile.faulttolerance.Retry;
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
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import io.openliberty.spacerover.models.LeaderboardEvent;
import io.openliberty.spacerover.models.LeaderboardEvent.LeaderboardEventSerializer;
import java.util.Properties;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.apache.kafka.clients.CommonClientConfigs;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
@Path("/")
public class Leaderboard {

	@Inject
	MongoDatabase db;

	@Inject
	Validator validator;

	@Inject
	@ConfigProperty(name = "io.openliberty.leaderboard.sendToMessenger", defaultValue = "false")
	Boolean sendToMessenger;

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
	@Retry(maxRetries = 5)
	public Response add(LeaderboardEntry entry) {
		JsonArray violations = getViolations(entry);

		if (!violations.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).entity(violations.toString()).build();
		}

		Document newLeaderboardEntry = new Document();
		newLeaderboardEntry.put(LeaderboardConstants.MONGO_LEADERBOARD_PLAYER, entry.getPlayer());
		newLeaderboardEntry.put(LeaderboardConstants.MONGO_LEADERBOARD_SCORE, entry.getScore());
		newLeaderboardEntry.put(LeaderboardConstants.MONGO_LEADERBOARD_TIME, entry.getTime());
		newLeaderboardEntry.put(LeaderboardConstants.MONGO_LEADERBOARD_HEALTH, entry.getHealth());
		newLeaderboardEntry.put(LeaderboardConstants.MONGO_LEADERBOARD_TIMESTAMP, Long.toString(System.currentTimeMillis()));
		newLeaderboardEntry.put(LeaderboardConstants.MONGO_LEADERBOARD_GAME_MODE, entry.getGameMode());

		// Send "add" action to messenger
		if (sendToMessenger) {
			System.out.println("Send to messenger: true");
			try {
				runProducer("add", newLeaderboardEntry.toJson());
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("Send to messenger: false");
			MongoCollection<Document> document = db.getCollection(LeaderboardConstants.LEADERBOARD_COLLECTION_NAME);
			document.insertOne(newLeaderboardEntry);
		}
		return Response.status(Response.Status.OK).entity(newLeaderboardEntry.toJson()).build();
	}
	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", description = "Successfully listed the leaderboard for gameMode 1.")
	@APIResponse(responseCode = "500", description = "Failed to list the leaderboard.")
	@Operation(summary = "List the leaderboard from the database for gameMode = \"1\".")
	@GET
	@Path("/")
	public Response retrieveDefault(@DefaultValue(LeaderboardConstants.QUERY_PARAM_START_TIME_DEFAULT_VALUE) @QueryParam(LeaderboardConstants.QUERY_PARAM_START_TIME) String startTime,
			@DefaultValue(LeaderboardConstants.QUERY_PARAM_END_TIME_DEFAULT_VALUE) @QueryParam(LeaderboardConstants.QUERY_PARAM_END_TIME) String endTime) {
		return retrieve(startTime, endTime, "1");
	}

	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", description = "Successfully listed the leaderboard.")
	@APIResponse(responseCode = "500", description = "Failed to list the leaderboard.")
	@Operation(summary = "List the leaderboard from the database.")
	@GET
	@Path("/{gameMode}")
	public Response retrieve(
			@DefaultValue(LeaderboardConstants.QUERY_PARAM_START_TIME_DEFAULT_VALUE) @QueryParam(LeaderboardConstants.QUERY_PARAM_START_TIME) String startTime,
			@DefaultValue(LeaderboardConstants.QUERY_PARAM_END_TIME_DEFAULT_VALUE) @QueryParam(LeaderboardConstants.QUERY_PARAM_END_TIME) String endTime,
			@DefaultValue("1") @PathParam("gameMode") final String gameMode) {
		try {
			return retrieveEntriesFromMongodb(startTime, endTime, gameMode);

		} catch (Exception e) {
			e.printStackTrace(System.out);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("[\"Unable to list leaderboard!\"]")
					.build();
		}

	}

	@Retry(maxRetries = 5)
	private Response retrieveEntriesFromMongodb(String startTime, String endTime, String gameMode) {
		if (endTime.isEmpty()) {
			endTime = Long.toString(System.currentTimeMillis());
		}
		MongoCollection<Document> collection = db.getCollection(LeaderboardConstants.LEADERBOARD_COLLECTION_NAME);
		FindIterable<Document> docs = collection
				.find(and(gte(LeaderboardConstants.MONGO_LEADERBOARD_TIMESTAMP, startTime),
						lte(LeaderboardConstants.MONGO_LEADERBOARD_TIMESTAMP, endTime),
						eq(LeaderboardConstants.MONGO_LEADERBOARD_GAME_MODE, gameMode)))
				.sort(new BasicDBObject("score", -1).append("time", 1));
		String output = getJsonListFromIterableDocuments(docs);
		return Response.status(Response.Status.OK).entity(output).build();
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
	@Retry(maxRetries = 5)
	public Response clear(
			@DefaultValue(LeaderboardConstants.QUERY_PARAM_PLAYER_NAME_DEFAULT) @QueryParam(LeaderboardConstants.QUERY_PARAM_PLAYER_NAME) String playerName) {
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
	@APIResponse(responseCode = "200", description = "Successfully cleared a row from the leaderboard.")
	@APIResponse(responseCode = "500", description = "Failed to delete an entry from the leaderboard.")
	@Operation(summary = "Clear a leaderboard entry from the database.")
	@DELETE
	@Path("/{id}")
	@Retry(maxRetries = 5)
	public Response clearSingleEntryWithID(@PathParam(LeaderboardConstants.QUERY_PARAM_ID) String documentID) {
		MongoCollection<Document> collection = db.getCollection(LeaderboardConstants.LEADERBOARD_COLLECTION_NAME);
		DeleteResult result;
		result = collection
				.deleteOne(new Document(LeaderboardConstants.MONGO_LEADERBOARD_DOCUMENT_ID, new ObjectId(documentID)));
		if (result.wasAcknowledged()) {
			return Response.status(Response.Status.OK).build();
		} else {
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
					.entity("[\"Unable to clear an entry (" + documentID + ") from the leaderboard!\"]").build();
		}

	}

	private final static String TOPIC = "leaderboard.event";
    private final static String BOOTSTRAP_SERVERS = "my-cluster-kafka-bootstrap-space-rover.apps.kc-ocp.cp.fyre.ibm.com:443";

	static String runProducer(final String type, final String message) throws Exception {
		final Producer<String, LeaderboardEvent> producer = createProducer();
		long time = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		sb.append("test1: ");
		try {
			final ProducerRecord<String, LeaderboardEvent> record = 
				new ProducerRecord<>(TOPIC, String.format("%s", type), new LeaderboardEvent(type, type, message));

			RecordMetadata metadata = producer.send(record).get();

			long elapsedTime = System.currentTimeMillis() - time;

			System.out.printf("sent record(key=%s value=%s) " +
							"meta(partition=%d, offset=%d) time=%d\n",
					record.key(), record.value(), metadata.partition(),
					metadata.offset(), elapsedTime);
			sb.append(String.format("sent record(key=%s value=%s) " +
			"meta(partition=%d, offset=%d) time=%d\n", record.key(), 
			record.value(), metadata.partition(), metadata.offset(), elapsedTime));  
		} finally {
			producer.flush();
			producer.close();
		}
		return sb.toString();
	}

    private static Producer<String, LeaderboardEvent> createProducer() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        props.put(ProducerConfig.CLIENT_ID_CONFIG, "KafkaExampleProducer");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, LeaderboardEventSerializer.class.getName());
		props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
		props.put("ssl.truststore.location", "/config/truststore.jks");
		props.put("ssl.truststore.password", "password");
        return new KafkaProducer<>(props);
    }
}