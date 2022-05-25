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
package io.openliberty.spacerover.settings.rest;

import java.util.Set;
import java.util.StringJoiner;

import org.bson.Document;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

import io.openliberty.spacerover.settings.constants.*;
import io.openliberty.spacerover.settings.model.SettingsEntry;

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
public class Settings {

	@Inject
	MongoDatabase db;

	@Inject
	Validator validator;

	private JsonArray getViolations(SettingsEntry entry) {
		Set<ConstraintViolation<SettingsEntry>> violations = validator.validate(entry);

		JsonArrayBuilder messages = Json.createArrayBuilder();

		for (ConstraintViolation<SettingsEntry> v : violations) {
			messages.add(v.getMessage());
		}

		return messages.build();
	}

	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)

	@APIResponse(responseCode = "200", description = "Successfully added updated game settings.")
	@APIResponse(responseCode = "400", description = "Invalid settings entry.")
	@Operation(summary = "Add game settings.")
	@POST
	@Path("/")
	public Response add(SettingsEntry entry) {
		JsonArray violations = getViolations(entry);

		if (!violations.isEmpty()) {
			return Response.status(Response.Status.BAD_REQUEST).entity(violations.toString()).build();
		}
		MongoCollection<Document> document = db.getCollection(SettingsConstants.SETTINGS_COLLECTION_NAME);

		Document newSettingEntry = new Document();
		newSettingEntry.put("horizontalMoveSpeed", entry.getHorizontalMoveSpeed());
		newSettingEntry.put("verticalMoveSpeed", entry.getVerticalMoveSpeed());
		newSettingEntry.put("gameLengthSeconds", entry.getGameLengthSeconds());
		newSettingEntry.put("meteorDamage", entry.getMeteorDmg());
		newSettingEntry.put("sunDamage", entry.getSunDmg());

		document.insertOne(newSettingEntry);

		return Response.status(Response.Status.OK).entity(newSettingEntry.toJson()).build();
	}

	@Produces(MediaType.APPLICATION_JSON)
	@APIResponse(responseCode = "200", description = "Successfully listed the Settings.")
	@APIResponse(responseCode = "500", description = "Failed to list the settingss.")
	@Operation(summary = "List the Settings from the database.")
	@GET
	@Path("/")
	public Response retrieve() {
		try {
			MongoCollection<Document> collection = db.getCollection(SettingsConstants.SETTINGS_COLLECTION_NAME);
			FindIterable<Document> docs = collection.find(new Document());
			String output = getJsonListFromIterableDocuments(docs);
			return Response.status(Response.Status.OK).entity(output).build();

		} catch (Exception e) {
			e.printStackTrace(System.out);
			return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("[\"Unable to list settings!\"]")
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

}