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
package io.openliberty.spacerover.leaderboard.mongo;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.ibm.websphere.crypto.PasswordUtil;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import java.lang.Integer;
import java.lang.Exception;

@ApplicationScoped
public class MongoProducer {

	@Inject
	@ConfigProperty(name = "mongo.hostname", defaultValue = "mongo")
	String hostname;

	@Inject
	@ConfigProperty(name = "mongo.port", defaultValue = "27017")
	int port;

	@Inject
	@ConfigProperty(name = "mongo.dbname", defaultValue = "spaceDB")
	String dbName;

	@Inject
	@ConfigProperty(name = "mongo.user")
	String user;

	@Inject
	@ConfigProperty(name = "mongo.pass.encoded")
	String encodedPass;

	@Produces
	public MongoClient createMongo() {
		String password = PasswordUtil.passwordDecode(encodedPass);
		String connectionString = "mongodb://" + user + ":" + password + "@" + hostname + ":" + Integer.toString(port) + "/" + dbName + "?authSource=admin";
		MongoClient mongoClient = null; 
		try {
			mongoClient = MongoClients.create(connectionString);
			MongoDatabase database = mongoClient.getDatabase(dbName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mongoClient;
	}

	@Produces
	public MongoDatabase createDB(MongoClient client) {
		return client.getDatabase(dbName);
	}

	public void close(@Disposes MongoClient toClose) {
		toClose.close();
	}
}