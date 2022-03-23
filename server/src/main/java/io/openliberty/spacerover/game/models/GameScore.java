package io.openliberty.spacerover.game.models;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotEmpty;

public class GameScore {
    @JsonbProperty("player")
    private String player;

    @JsonbProperty("score")
	private long score;

    @JsonbProperty("time")
	private long time;

    @NotEmpty(message = "All players must have a player ID!")
	public String getPlayer() {
		return player;
	}

	public void setPlayer(String player) {
		this.player = player;
	}

	public long getScore() {
		return score;
	}

	public void setScore(long score) {
		this.score = score;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

    @Override
    public String toString() {
        return "GameStat [player=" + player + ", score=" + score + ", time=" + time + "]";
    }

    
}
