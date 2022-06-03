package io.openliberty.spacerover.game;

import java.util.logging.Level;
import java.util.logging.Logger;

import io.openliberty.spacerover.game.models.Constants;

public class GuidedGame extends Game {
	private static final Logger LOGGER = Logger.getLogger(GuidedGame.class.getName());
	private int nextColourIndex;

	public GuidedGame() {
		this.nextColourIndex = 0;
	}

	@Override
	public void processColour(String msgID) {
		this.setLastColourVisited(msgID);
		if (msgID.equals(Constants.COLOUR_RED)) {
			this.decrementScore(OBSTACLE_SCORE_DECREMENT);
			this.decrementHP(OBSTACLE_HP_DECREMENT);
		} else if (msgID.equals(Constants.COLOUR_RED_SUN)) {
			this.decrementScore(OBSTACLE_SUN_SCORE_DECREMENT);
			this.decrementHPSun(OBSTACLE_SUN_HP_DECREMENT);
		} else if (!this.getColoursVisited().contains(msgID)
				&& msgID.equals(Constants.COLOURS_EXCLUDING_RED[this.nextColourIndex])) {
			LOGGER.log(Level.INFO, "Colour visited: {0}", msgID);
			this.getColoursVisited().add(msgID);
			this.incrementScore(getScore());
			this.nextColourIndex++;
		}
		if (this.isInProgressGameOver()) {
			this.endGameSession();
		}
	}

	@Override
	protected String getGameMode() {
		return Constants.INIT_GAME_GUIDED;
	}
}
