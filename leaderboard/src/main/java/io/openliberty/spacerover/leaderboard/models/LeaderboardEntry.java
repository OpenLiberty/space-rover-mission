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
package io.openliberty.spacerover.leaderboard.models;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotEmpty;

public class LeaderboardEntry {

    @JsonbProperty("player")
	private String player;
	@JsonbProperty("score")
	private int score;
	@JsonbProperty("time")
	private int time;
	@JsonbProperty("health")
	private int health;
	@JsonbProperty ("timestamp")
	private long timestamp;

	public int getHealth() {
		return health;
	}

	@NotEmpty(message = "All players must have a player ID!")
	public String getPlayer() {
		return player;
	}

	public int getScore() {
		return score;
	}

	public int getTime() {
		return time;
	}

	public long getTimestamp() {
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

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

}
