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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Constants {

	public static final String CONNECT_GUI = "connectGUI";
	public static final String CONNECT_GESTURE = "connectGesture";
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
	public static final String ROVER_CONNECTION_FAILED = "roverConnectionFailed";
	public static final String ERROR_MESSAGE = "error";
	public static final String GAME_HEALTH_TEST = "healthCheck";
	public static final String GAME_HEALTH_ACK = "healthAck";
	public static final String COLOUR_RED = "RED";
	public static final String COLOUR_BLUE = "BLU";
	public static final String COLOUR_GREEN = "GRN";
	public static final String COLOUR_YELLOW = "YW";
	public static final String COLOUR_PURPLE = "PUR";
	public static final String COLOUR_RED_SUN = "SUN";
	public static final Map<String, Integer> COLOUR_SCORE_VALUES = Map.of(COLOUR_BLUE, 10, COLOUR_GREEN, 20,
			COLOUR_YELLOW, 30, COLOUR_PURPLE, 40);
	public static final String[] COLOURS = { COLOUR_RED, COLOUR_BLUE, COLOUR_GREEN, COLOUR_YELLOW, COLOUR_PURPLE };
	public static final String[] COLOURS_EXCLUDING_RED = { COLOUR_BLUE, COLOUR_GREEN, COLOUR_YELLOW, COLOUR_PURPLE };
	public static final String INIT_GAME_CLASSIC = "1";
	public static final String INIT_GAME_HOP = "2";
	public static final String INIT_GAME_GUIDED = "3";
	public static final String INIT_GAME_SUDDEN_DEATH = "4";
	public static final String GAME_MODE_NAME_PLANET_HOP = "Planet Hop";
	public static final String GAME_MODE_NAME_CLASSIC = "Classic";
	public static final String GAME_MODE_NAME_GUIDED = "Guided";
	public static final String GAME_MODE_NAME_SUDDENDEATH = "Sudden Death";

	public static final Set<String> SUN_RFID_IDENTIFIERS = new HashSet<>(
			List.of("04 37 9A 02 EC 6E 81", "04 3F 9A 02 EC 6E 81", "04 47 9A 02 EC 6E 81", "04 4F 9A 02 EC 6E 81",
					"04 57 9A 02 EC 6E 81", "04 62 9B 02 EC 6E 81", "04 6C 9C 02 EC 6E 81", "04 F1 9C 02 EC 6E 80",
					"04 EA 9B 02 EC 6E 80", "04 E2 9B 02 EC 6E 80", "04 DB 9A 02 EC 6E 80", "04 D3 9A 02 EC 6E 80",
					"04 2F 9A 02 EC 6E 81", "04 FD A8 02 EC 6E 80", "04 D0 A7 02 EC 6E 80", "04 4E D9 02 EC 6E 81",
					"04 1A A9 02 EC 6E 81", "04 E0 A7 02 EC 6E 80", "04 FB AA 02 EC 6E 80", "04 EB A8 02 EC 6E 80",
					"04 F4 A8 02 EC 6E 80", "04 D6 A6 02 EC 6E 80", "04 EE A7 02 EC 6E 80", "04 E5 A7 02 EC 6E 80",
					"04 DE A6 02 EC 6E 80", "04 7A 9A 02 EC 6E 80", "04 70 9B 02 EC 6E 80", "04 68 9B 02 EC 6E 80",
					"04 5F 9B 02 EC 6E 80", "04 94 9A 02 EC 6E 80", "04 32 9B 02 EC 6E 81", "04 27 9A 02 EC 6E 81",
					"04 F6 A7 02 EC 6E 80", "04 D8 A7 02 EC 6E 80", "04 04 AA 02 EC 6E 81", "04 0E A9 02 EC 6E 81",
					"04 2A 9B 02 EC 6E 81", "04 22 9B 02 EC 6E 81", "04 1A 9B 02 EC 6E 81", "04 0E 9C 02 EC 6E 81",
					"04 06 9C 02 EC 6E 81", "04 FC 9C 02 EC 6E 80", "04 F4 9C 02 EC 6E 80"));

}
