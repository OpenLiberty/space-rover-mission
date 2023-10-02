/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.spacerover.leaderboard.models;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotEmpty;

public class LeaderboardEntry {
	
	@NotEmpty(message = "All players must have a player ID!")
    @JsonbProperty("player")
	private String player;
	
	@JsonbProperty("score")
	private int score;
	@JsonbProperty("time")
	private int time;
	@JsonbProperty("health")
	private int health;
	@JsonbProperty ("timestamp")
	private String timestamp;
	
	@NotEmpty(message = "All entries must have an associated game mode!")
	@JsonbProperty ("gameMode")
	private String gameMode;
	
	public String getGameMode() {
		return gameMode;
	}

	public void setGameMode(String gameMode) {
		this.gameMode = gameMode;
	}

	public int getHealth() {
		return health;
	}

	public String getPlayer() {
		return player;
	}

	public int getScore() {
		return score;
	}

	public int getTime() {
		return time;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
