/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.spacerover.leaderboard.mongo;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.ibm.websphere.crypto.PasswordUtil;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

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
		MongoCredential creds = MongoCredential.createCredential(user, dbName, password.toCharArray());

		return new MongoClient(new ServerAddress(hostname, port), creds, new MongoClientOptions.Builder().build());
	}

	@Produces
	public MongoDatabase createDB(MongoClient client) {
		return client.getDatabase(dbName);
	}

	public void close(@Disposes MongoClient toClose) {
		toClose.close();
	}
}