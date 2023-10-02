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
	public static final String GAME_MODE_DESC_CLASSIC = "Visit all the planets in any order.";
	public static final String GAME_MODE_DESC_GUIDED = "Visit planets in specified order.";
	public static final String GAME_MODE_DESC_SUDDENDEATH = "Instant death Classic mode.";
	public static final String GAME_MODE_DESC_PLANET_HOP = "Whack-a-mole with planets.";

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
					"04 06 9C 02 EC 6E 81", "04 FC 9C 02 EC 6E 80", "04 F4 9C 02 EC 6E 80",
					// Gameboard 3
					"04 20 AD 02 EC 6E 80", "04 18 AD 02 EC 6E 80", "04 0C AC 02 EC 6E 80", "04 14 AB 02 EC 6E 80",
					"04 1B AC 02 EC 6E 80", "04 23 AC 02 EC 6E 80", "04 28 AD 02 EC 6E 80", "04 8D AA 02 EC 6E 80",
					"04 AE A9 02 EC 6E 80", "04 95 AA 02 EC 6E 80", "04 A4 A8 02 EC 6E 80", "04 9C A8 02 EC 6E 80",
					"04 4E AA 02 EC 6E 80", "04 46 AA 02 EC 6E 80", "04 B6 A9 02 EC 6E 80", "04 BE A9 02 EC 6E 80",
					"04 C6 A9 02 EC 6E 80", "04 C7 AA 02 EC 6E 80", "04 BF AA 02 EC 6E 80", "04 51 AD 02 EC 6E 80",
					"04 64 AC 02 EC 6E 80", "04 59 AD 02 EC 6E 80", "04 62 AD 02 EC 6E 80", "04 3F AE 02 EC 6E 80",
					"04 47 AB 02 EC 6E 80", "04 4F AB 02 EC 6E 80", "04 57 AB 02 EC 6E 80", "04 5F AB 02 EC 6E 80",
					"04 66 AA 02 EC 6E 80", "04 55 D8 02 EC 6E 81", "04 56 AA 02 EC 6E 80", "04 39 AF 02 EC 6E 80",
					"04 41 AF 02 EC 6E 80", "04 48 AE 02 EC 6E 80", "04 50 AE 02 EC 6E 80", "04 58 AE 02 EC 6E 80",
					"04 3D AC 02 EC 6E 80", "04 45 AD 02 EC 6E 80", "04 54 AC 02 EC 6E 80", "04 4C AC 02 EC 6E 80",
					"04 5C AC 02 EC 6E 80", "04 33 AC 02 EC 6E 80", "04 76 AB 02 EC 6E 80", "04 60 AE 02 EC 6E 80",
					"04 58 AE 02 EC 6E 80", "04 76 AB 02 EC 6E 80", "04 6A AF 02 EC 6E 80", "04 6E AB 02 EC 6E 80",
					// Gameboard 4
					"04 57 2E 52 14 6F 80", "04 57 2D 52 14 6F 80", "04 57 2F 52 14 6F 80", "04 57 30 52 14 6F 80",
					"04 57 2C 52 14 6F 80", "04 57 2B 52 14 6F 80", "04 57 2C 52 14 6F 80", "04 57 2A 52 14 6F 80",
					"04 57 29 52 14 6F 80", "04 57 26 52 14 6F 80", "04 57 25 52 14 6F 80", "04 58 F2 52 14 6F 80",
					"04 57 27 52 14 6F 80", "04 57 28 52 14 6F 80", "04 57 F5 52 14 6F 80", "04 58 F9 52 14 6F 80",
					"04 57 F3 52 14 6F 80", "04 57 F4 52 14 6F 80", "04 57 EB 52 14 6F 80", "04 57 4B 52 14 6F 80",
					"04 57 EC 52 14 6F 80", "04 57 ED 52 14 6F 80", "04 57 EE 52 14 6F 80", "04 57 EF 52 14 6F 80",
					"04 57 F0 52 14 6F 80", "04 57 F1 52 14 6F 80", "04 56 99 52 14 6F 80", "04 56 98 52 14 6F 80",
					"04 56 97 52 14 6F 80", "04 56 96 52 14 6F 80", "04 56 95 52 14 6F 80", "04 56 94 52 14 6F 80",
					"04 56 93 52 14 6F 80", "04 56 90 52 14 6F 80", "04 58 F3 52 14 6F 80", "04 56 91 52 14 6F 80",
					"04 56 92 52 14 6F 80", "04 57 CE 52 14 6F 80", "04 58 EF 52 14 6F 80", "04 57 CD 52 14 6F 80",
					"04 57 CC 52 14 6F 80", "04 57 CF 52 14 6F 80", "04 57 D0 52 14 6F 80", "04 57 D1 52 14 6F 80",
					"04 57 D2 52 14 6F 80", "04 57 D3 52 14 6F 80", "04 57 D4 52 14 6F 80", "04 57 D5 52 14 6F 80"));

	public static final String ROVER_SOCKET_NAME = "Rover";
	public static final String BOARD_SOCKET_NAME = "Gameboard";
	public static final String GUI_BATTERY_PCT = "battery";

}
