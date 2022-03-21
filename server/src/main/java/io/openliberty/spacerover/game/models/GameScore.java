package io.openliberty.spacerover.game.models;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotEmpty;

public class GameScore {
    @JsonbProperty("player")
    private String player;

    @JsonbProperty("score")
	private int score;

    @JsonbProperty("time")
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

    @Override
    public String toString() {
        return "GameStat [player=" + player + ", score=" + score + ", time=" + time + "]";
    }

    
}
