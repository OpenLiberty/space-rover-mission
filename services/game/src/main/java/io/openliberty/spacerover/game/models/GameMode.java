package io.openliberty.spacerover.game.models;

import jakarta.json.bind.annotation.JsonbProperty;

public class GameMode {
	@JsonbProperty("name")
	private String gameModeName;
	@JsonbProperty("id")
	private int gameModeID;

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
