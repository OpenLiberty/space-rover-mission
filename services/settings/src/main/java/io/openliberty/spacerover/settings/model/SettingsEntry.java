package io.openliberty.spacerover.settings.model;

public class SettingsEntry {

	private int horizontalMoveSpeed;
	private int verticalMoveSpeed;
	private int gameLengthSeconds;
	private int meteorDmg;
	private int sunDmg;

	public SettingsEntry() {
	}

	public SettingsEntry(int horizontalMoveSpeed, int verticalMoveSpeed, int gameLengthSeconds, int meteorDmg,
			int sunDmg) {
		super();
		this.horizontalMoveSpeed = horizontalMoveSpeed;
		this.verticalMoveSpeed = verticalMoveSpeed;
		this.gameLengthSeconds = gameLengthSeconds;
		this.meteorDmg = meteorDmg;
		this.sunDmg = sunDmg;
	}

	public int getHorizontalMoveSpeed() {
		return horizontalMoveSpeed;
	}

	public void setHorizontalMoveSpeed(int horizontalMoveSpeed) {
		this.horizontalMoveSpeed = horizontalMoveSpeed;
	}

	public int getVerticalMoveSpeed() {
		return verticalMoveSpeed;
	}

	public void setVerticalMoveSpeed(int verticalMoveSpeed) {
		this.verticalMoveSpeed = verticalMoveSpeed;
	}

	public int getGameLengthSeconds() {
		return gameLengthSeconds;
	}

	public void setGameLengthSeconds(int gameLengthSeconds) {
		this.gameLengthSeconds = gameLengthSeconds;
	}

	public int getMeteorDmg() {
		return meteorDmg;
	}

	public void setMeteorDmg(int meteorDmg) {
		this.meteorDmg = meteorDmg;
	}

	public int getSunDmg() {
		return sunDmg;
	}

	public void setSunDmg(int sunDmg) {
		this.sunDmg = sunDmg;
	}

}
