package io.openliberty.spacerover.game;

import org.junit.jupiter.api.Test;

import io.openliberty.spacerover.game.models.GameEvent;

public class SpaceHopTest implements GameEventListener {

	SpaceHop game;
	@Test
	public void testGameTimer()
	{
		game = new SpaceHop();
		game.getEventManager().subscribe(GameEvent.FIVE_SECONDS_LEFT, this);
		game.getEventManager().subscribe(GameEvent.PLANET_CHANGED, this);
		game.startGameSession("test");

		while(game.isInProgress())
		{
		}
		game.endGameSession();
		
	}

	@Override
	public void update(GameEvent eventType, long value) {
		System.out.println(eventType);
		System.out.println(game.getCurrentPlanetColour());
		
	}
}
