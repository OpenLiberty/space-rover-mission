/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.spacerover.game.models;

public class Constants {

	public static final String CONNECT_GUI = "connectGUI";
	public static final String SERVER_READY = "serverReady";
	public static final String END_GAME = "endGame";
	public static final String START_GAME = "startGame";
	public static final String SOCKET_MESSAGE_DATA_DELIMITER = "|";
	public static final String SOCKET_MESSAGE_PAYLOAD_DELIMITER = ",";
	public static final String RIGHT = "R";
	public static final String LEFT = "L";
	public static final String FORWARD = "F";
	public static final String BACKWARD = "B";
	public static final String STOP = "S";
	public static final String[] DIRECTIONS = { FORWARD, BACKWARD, LEFT, RIGHT, STOP };
	public static final String ROVER_ACK = "Rover Connected";
	public static final String GAMEBOARD_ACK = "Board Connected";
	public static final String ERROR_MESSAGE = "error";
	public static final String GAME_HEALTH_TEST = "healthCheck";
	public static final String GAME_HEALTH_ACK = "healthAck";
	public static final String INIT_GAME_FREE_ROAM = "5";
	public static final String GAME_MODE_NAME_FREE_ROAM = "Free Roam";
	public static final String GAME_MODE_DESC_FREE_ROAM = "Type plain-English rover instructions and execute them in open space without a physical game board.";

	public static final String ROVER_SOCKET_NAME = "Rover";
	public static final String BOARD_SOCKET_NAME = "Gameboard";
	public static final String GUI_BATTERY_PCT = "battery";

}
