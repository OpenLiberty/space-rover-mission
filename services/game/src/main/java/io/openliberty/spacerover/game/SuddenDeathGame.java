package io.openliberty.spacerover.game;

import io.openliberty.spacerover.game.models.GameEvent;
import io.openliberty.spacerover.game.models.SocketMessages;

public class SuddenDeathGame extends Game {

	@Override
	public void decrementHP(int amount) {
		this.setHealth(0);
		getEventManager().notify(GameEvent.HP, this.getHealth());
	}

	@Override
	protected String getGameMode() {
		return SocketMessages.INIT_GAME_SUDDEN_DEATH;
	}
}
