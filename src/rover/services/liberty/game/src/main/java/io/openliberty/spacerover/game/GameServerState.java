/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package io.openliberty.spacerover.game;

public enum GameServerState {
	SERVER_STARTED, GUI_CONNECTED, GESTURE_CONNECTED, GUI_AND_GESTURE_CONNECTED, LEADERBOARD_CONNECTED,
	ROVER_CONNECT_TEST, ROVER_CONNECTED, GAMEBOARD_CONNECT_TEST,
	ALL_CONNECTED, GAME_STARTED, GAME_ENDED, ERROR_OCCURRED;
}
