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
package io.openliberty.spacerover.game.models;

import java.util.Map;

public class SocketMessages {

	public static final String CONNECT_GUI = "connectGUI";
	public static final String CONNECT_GESTURE = "connectGesture";
	public static final String SERVER_READY = "serverReady";
	public static final String END_GAME = "endGame";
	public static final String START_GAME = "startGame";
	public static final String SOCKET_MESSAGE_DATA_DELIMITER = "|";
	public static final String RIGHT = "R";
	public static final String LEFT = "L";
	public static final String FORWARD = "F";
	public static final String BACKWARD = "B";
	public static final String STOP = "S";
	public static final String[] DIRECTIONS = { FORWARD, BACKWARD, LEFT, RIGHT, STOP };
	public static final String ROVER_ACK = "Rover Connected";
	public static final String GAMEBOARD_ACK = "Board Connected";
	public static final String ROVER_CONNECTION_FAILED = "roverConnectionFailed";
	public static final String ERROR_MESSAGE = "error";
	public static final String GAME_HEALTH_TEST = "healthCheck";
	public static final String GAME_HEALTH_ACK = "healthAck";
	public static final String COLOUR_RED = "RED";
	public static final String COLOUR_BLUE = "BLU";
	public static final String COLOUR_GREEN = "GRN";
	public static final String COLOUR_YELLOW = "YW";
	public static final String COLOUR_PURPLE = "PUR";
	public static final Map<String, Integer> COLOUR_SCORE_VALUES = Map.of(COLOUR_BLUE, 10, COLOUR_GREEN, 20, COLOUR_YELLOW, 30, COLOUR_PURPLE, 40);
	public static final String[] COLOURS = { COLOUR_RED, COLOUR_BLUE, COLOUR_GREEN, COLOUR_YELLOW, COLOUR_PURPLE };
	public static final String INIT_GAME = "1";

}
