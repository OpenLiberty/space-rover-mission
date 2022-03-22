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
	public static final String ROVER_ACK = "roverAck";
	public static final String GAMEBOARD_ACK = "gameboardAck";
	public static final String ROVER_CONNECTION_FAILED = "roverConnectionFailed";
	public static final String ERROR_MESSAGE = "error";

}
