package io.openliberty.spacerover.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import io.openliberty.spacerover.game.models.Constants;
import io.openliberty.spacerover.game.models.GameEvent;
import io.openliberty.spacerover.game.models.GameScore;
import jakarta.validation.constraints.AssertTrue;

public class GameTest {

	private static final String PLAYER_NAME = "PLAYER1";

	@Test
	public void testGameEventManager() {
		Set<GameEvent> events = new HashSet<>();
		Game game = new Game();
		getGameManager(events, game);
		game.startGameSession(PLAYER_NAME);
		game.decrementHP(1);
		assertTrue(events.contains(GameEvent.HP));
		assertEquals(1, events.size());
		game.decrementScore(10);
		assertTrue(events.contains(GameEvent.SCORE));
		assertEquals(2, events.size());
		game.decrementHPSun(10);
		assertTrue(events.contains(GameEvent.HP_SUN));
		assertEquals(3, events.size());
		assertTrue(game.isInProgress());
		game.endGameSession();
		assertTrue(events.contains(GameEvent.GAME_OVER));
		assertEquals(4, events.size());
		assertFalse(game.isInProgress());

	}

	@Test
	public void testProcessColour() {
		Game game = new Game();
		Set<GameEvent> events = new HashSet<>();
		getGameManager(events, game);
		game.startGameSession(PLAYER_NAME);
		int curr_HP = 100;
		int curr_score = 0;
		game.processColour(Constants.COLOUR_RED);
		assertTrue(events.contains(GameEvent.HP));
		assertTrue(events.contains(GameEvent.SCORE));
		assertEquals(2, events.size());
		curr_HP -= 10;
		curr_score -= 10;
		assertEquals(curr_HP, game.getHealth());
		assertEquals(curr_score, game.getScore());

		events.clear();
		game.processColour(Constants.COLOUR_BLUE);
		assertTrue(events.contains(GameEvent.SCORE));
		assertEquals(1, events.size());
		curr_score += 10;
		assertEquals(curr_HP, game.getHealth());
		assertEquals(curr_score, game.getScore());

		events.clear();
		game.processColour(Constants.COLOUR_BLUE); // repeat blue
		assertFalse(events.contains(GameEvent.SCORE));
		assertEquals(0, events.size());
		assertEquals(curr_HP, game.getHealth());
		assertEquals(curr_score, game.getScore());

		events.clear();
		game.processColour(Constants.COLOUR_GREEN);
		assertTrue(events.contains(GameEvent.SCORE));
		assertEquals(1, events.size());
		curr_score += 20;
		assertEquals(curr_HP, game.getHealth());
		assertEquals(curr_score, game.getScore());

		events.clear();
		game.processColour(Constants.COLOUR_YELLOW);
		assertTrue(events.contains(GameEvent.SCORE));
		assertEquals(1, events.size());
		curr_score += 30;
		assertEquals(curr_HP, game.getHealth());
		assertEquals(curr_score, game.getScore());

		events.clear();
		game.processColour(Constants.COLOUR_RED_SUN);
		assertTrue(events.contains(GameEvent.SCORE));
		assertTrue(events.contains(GameEvent.HP_SUN));
		assertEquals(2, events.size());
		curr_score -= 10;
		curr_HP -= 20;
		assertEquals(curr_HP, game.getHealth());
		assertEquals(curr_score, game.getScore());

		events.clear();
		game.processColour(Constants.COLOUR_PURPLE);
		assertTrue(events.contains(GameEvent.SCORE));
		assertTrue(events.contains(GameEvent.GAME_OVER));
		assertEquals(2, events.size());
		curr_score += 40;
		assertEquals(curr_HP, game.getHealth());
		assertEquals(curr_score, game.getScore());
		assertFalse(game.isInProgress());
	}

	@Test
	public void endGameBeforeStarted() {
		boolean success = false;
		Game game = new Game();
		try {
			game.endGameSession();
		} catch (IllegalStateException ise) {
			success = true;
		}
		assertTrue(success);
	}

	@Test
	public void playerHPReachesZero() {
		Game game = new Game();
		Set<GameEvent> events = new HashSet<>();
		getGameManager(events, game);
		game.startGameSession(PLAYER_NAME);
		game.processColour(Constants.COLOUR_RED_SUN);
		game.processColour(Constants.COLOUR_RED_SUN);
		game.processColour(Constants.COLOUR_RED_SUN);
		game.processColour(Constants.COLOUR_RED_SUN);
		game.processColour(Constants.COLOUR_RED_SUN);
		assertTrue(events.contains(GameEvent.SCORE));
		assertTrue(events.contains(GameEvent.HP_SUN));
		assertTrue(events.contains(GameEvent.GAME_OVER));
		assertFalse(game.isInProgress());
		assertEquals(0, game.getHealth());
		assertEquals(-50, game.getScore());
		
	}
	
	@Test
	public void testLeaderboardStats()
	{
		Game game = new Game();
		Set<GameEvent> events = new HashSet<>();
		getGameManager(events, game);
		game.startGameSession(PLAYER_NAME);
		game.processColour(Constants.COLOUR_BLUE);
		game.processColour(Constants.COLOUR_GREEN);
		game.processColour(Constants.COLOUR_YELLOW);
		game.processColour(Constants.COLOUR_PURPLE);
		assertFalse(game.isInProgress());
		GameScore score = game.getGameLeaderboardStat();
		assertEquals(Constants.INIT_GAME_CLASSIC,score.getGameMode());
		assertEquals(PLAYER_NAME, score.getPlayer());
		assertEquals(100, score.getHealth());
		assertEquals(100, score.getScore());
		assertEquals(0,score.getTime());
		
	}

	private GameEventListener getGameManager(Set<GameEvent> events, Game game) {
		GameEventListener listener = new GameEventListener() {

			@Override
			public void update(GameEvent eventType, long value) {
				events.add(eventType);
			}
		};
		game.getEventManager().subscribe(GameEvent.HP, listener);
		game.getEventManager().subscribe(GameEvent.HP_SUN, listener);
		game.getEventManager().subscribe(GameEvent.GAME_OVER, listener);
		game.getEventManager().subscribe(GameEvent.SCORE, listener);
		return listener;
	}
}
