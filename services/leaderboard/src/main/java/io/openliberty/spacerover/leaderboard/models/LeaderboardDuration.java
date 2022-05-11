package io.openliberty.spacerover.leaderboard.models;

import jakarta.json.bind.annotation.JsonbProperty;

public class LeaderboardDuration {
	@JsonbProperty("startTime")
	private String startTime;
	
	@JsonbProperty("endTime")
	private String endTime;

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

}
