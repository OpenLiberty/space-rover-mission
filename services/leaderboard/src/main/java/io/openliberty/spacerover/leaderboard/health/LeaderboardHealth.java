/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.spacerover.leaderboard.health;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.health.HealthCheck;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.eclipse.microprofile.health.Liveness;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Liveness
@ApplicationScoped
public class LeaderboardHealth implements HealthCheck {
	private static final Logger LOGGER = Logger.getLogger(LeaderboardHealth.class.getName());

	@Inject
	@ConfigProperty(name = "io.openliberty.leaderboard.port", defaultValue = "9080")
	int leaderboardPort;

	@Override
	public HealthCheckResponse call() {
		HealthCheckResponse resp;
		if (!isHealthy()) {
			resp = HealthCheckResponse.named(LeaderboardHealth.class.getSimpleName())
					.withData("LeaderboardHealth", "not available").down().build();
			LOGGER.warning("LeaderboardHealth check failed.");
		} else {
			resp = HealthCheckResponse.named(LeaderboardHealth.class.getSimpleName())
					.withData("LeaderboardHealth", "available").up().build();
			LOGGER.info("LeaderboardHealth check passed.");
		}
		return resp;
	}

	private boolean isHealthy() {
		boolean isHealthy = true;
		try {
			URI uri = new URI("http", null, "localhost", leaderboardPort, "/mongo/leaderboard", null, null);
			String url = uri.toString();
			Client client = ClientBuilder.newClient();
			Response response = client.target(url).request(MediaType.APPLICATION_JSON).get();
			if (response.getStatus() != 200) {
				isHealthy = false;
			}
		} catch (URISyntaxException e) {
			LOGGER.log(Level.SEVERE, "Failed to check leaderboard health", e);
			isHealthy = false;
		}
		return isHealthy;
	}

}
