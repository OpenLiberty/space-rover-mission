package io.openliberty.spacerover.game.models;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.validation.constraints.NotEmpty;

public class GameScore {
    @JsonbProperty("player")
    private String player;

    @JsonbProperty("score")
	private int score;

    @JsonbProperty("time")
	private long time;
    
    @JsonbProperty("health")
    private int health;

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

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

    public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	@Override
    public String toString() {
        return "GameStat [player=" + player + ", score=" + score + ", time=" + time + "]";
    }

    
}
