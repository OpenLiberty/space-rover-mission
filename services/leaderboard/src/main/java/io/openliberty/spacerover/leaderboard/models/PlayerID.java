package io.openliberty.spacerover.leaderboard.models;

import jakarta.json.bind.annotation.JsonbProperty;

public class PlayerID {
	@JsonbProperty("player")
	private String player;

	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

}
