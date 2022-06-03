package io.openliberty.spacerover.game;

import io.openliberty.spacerover.game.models.GameEvent;
import io.openliberty.spacerover.game.models.Constants;

public class SuddenDeathGame extends Game {

	@Override
	public void decrementHP(int amount) {
		this.setHealth(0);
		getEventManager().notify(GameEvent.HP, this.getHealth());
	}
	@Override
	public void decrementHPSun(int amount) {
		this.setHealth(0);
		getEventManager().notify(GameEvent.HP_SUN, this.getHealth());
	}


	@Override
	protected String getGameMode() {
		return Constants.INIT_GAME_SUDDEN_DEATH;
	}
}
