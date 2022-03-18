package io.openliberty.spacerover.leaderboard.rest;

import jakarta.validation.constraints.NotEmpty;

public class LeaderboardEntry {

	private String player;
	private int score;
	private int time;

	@NotEmpty(message = "All players must have a player ID!")
	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

}
