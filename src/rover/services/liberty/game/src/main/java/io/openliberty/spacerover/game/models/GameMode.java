package io.openliberty.spacerover.game.models;

import jakarta.json.bind.annotation.JsonbProperty;

public class GameMode {
	@JsonbProperty("name")
	private String gameModeName;
	@JsonbProperty("id")
	private int gameModeID;
	@JsonbProperty("description")
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public GameMode() {

	}

	public String getGameModeName() {
		return gameModeName;
	}

	public void setGameModeName(String gameModeName) {
		this.gameModeName = gameModeName;
	}

	public int getGameModeID() {
		return gameModeID;
	}

	public void setGameModeID(int gameModeID) {
		this.gameModeID = gameModeID;
	}

}
