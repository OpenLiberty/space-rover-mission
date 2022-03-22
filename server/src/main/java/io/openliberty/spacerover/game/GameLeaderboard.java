package io.openliberty.spacerover.game;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.microprofile.rest.client.RestClientBuilder;

import io.openliberty.spacerover.client.LeaderboardClient;
import io.openliberty.spacerover.game.models.GameScore;

public class GameLeaderboard {

    private static final String HTTP_PROTOCOL = "http://";    
    private static final String MONGO_LEADERBOARD_ENDPOINT = "/mongo/leaderboard";
    private static final int GET_DEFAULT_NUMBER_OF_LEADERBOARD_RESULTS = 0;
	private static final Logger LOGGER = Logger.getLogger(GameLeaderboard.class.getName());

    String host;
    int port;
    public String getHost() {
        return host;
    }
    public void setHost(String host) {
        this.host = host;
    }
    public int getPort() {
        return port;
    }
    public void setPort(int port) {
        this.port = port;
    }
    public GameLeaderboard(String host, int port) {
        this.host = host;
        this.port = port;
    }
	private String getLeaderboardConnectionURL(String host, int port) {
		return HTTP_PROTOCOL + host + ":" + port + MONGO_LEADERBOARD_ENDPOINT;
	}

    private String getLeaderboardConnectionURL()
    {
        return getLeaderboardConnectionURL(this.host, this.port);
    }

    public GameScore getStat(final String playerID, final int score, final int timeInSeconds) {
		final GameScore stat = new GameScore();
		stat.setPlayer("p1");
		stat.setScore(100);
		stat.setTime(123);
		return stat;
	}

    public void testLeaderboard() {
		getLeaderboard();
	}

	public List<GameScore> getLeaderboard(final int size) {
		final String leaderboardURLString = getLeaderboardConnectionURL();
		List<GameScore> scores = new ArrayList<>();
		try {
			final LeaderboardClient leaderboardClient = RestClientBuilder.newBuilder().baseUrl(new URL(leaderboardURLString))
					.build(LeaderboardClient.class);
			scores = leaderboardClient.retrieve();
			if (size != GET_DEFAULT_NUMBER_OF_LEADERBOARD_RESULTS) {
				scores = scores.subList(0, size - 1);
			}
		} catch (final MalformedURLException e) {
			LOGGER.log(Level.SEVERE, "The given URL {0} is malformed: {1}", new Object[] { leaderboardURLString, e });
            
		}
		return scores;
	}

	public List<GameScore> getLeaderboard() {
		return getLeaderboard(GET_DEFAULT_NUMBER_OF_LEADERBOARD_RESULTS);
	}

	public void updateLeaderboard(final GameScore stat) {
		final String leaderboardURLString = getLeaderboardConnectionURL();
		try {
			final LeaderboardClient leaderboardClient = RestClientBuilder.newBuilder().baseUrl(new URL(leaderboardURLString))
					.build(LeaderboardClient.class);
			leaderboardClient.add(stat);

		} catch (final MalformedURLException e) {
			LOGGER.log(Level.SEVERE, "The given URL {0} is malformed: {1}", new Object[] { leaderboardURLString, e });

		}
	}
}