/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.spacerover.leaderboard.models;

public class LeaderboardConstants {
	public static final String QUERY_PARAM_ID = "id";

	public static final String MONGO_LEADERBOARD_DOCUMENT_ID = "_id";

	public static final String QUERY_PARAM_START_TIME_DEFAULT_VALUE = "0";
	
	public static final String EMPTY_STRING = "";

	public static final String QUERY_PARAM_END_TIME_DEFAULT_VALUE = EMPTY_STRING;

	public static final String QUERY_PARAM_END_TIME = "endTime";

	public static final String QUERY_PARAM_START_TIME = "startTime";

	public static final String MONGO_LEADERBOARD_TIMESTAMP = "timestamp";

	public static final String MONGO_LEADERBOARD_HEALTH = "health";

	public static final String MONGO_LEADERBOARD_TIME = "time";

	public static final String MONGO_LEADERBOARD_SCORE = "score";

	public static final String MONGO_LEADERBOARD_PLAYER = "player";

	public static final String LEADERBOARD_COLLECTION_NAME = "Leaderboard";

	public static final String QUERY_PARAM_PLAYER_NAME_DEFAULT = EMPTY_STRING;
	
	public static final String QUERY_PARAM_PLAYER_NAME = "playerName";

	public static final String MONGO_LEADERBOARD_GAME_MODE = "gameMode";

}
