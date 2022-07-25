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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.openliberty.spacerover.game.models.Constants;

public class GameServerStateMachineTest {
	@Test
	void testIncrementState() {
		GameServerStateMachine machine = new GameServerStateMachine();
		verifyAndIncrement(machine, Constants.CONNECT_GUI);
		verifyAndIncrement(machine, Constants.CONNECT_GESTURE);
		assertTrue(machine.isReadyToConnectGamePieces());
		machine.attachLeaderboard();
		assertTrue(machine.isReadyToConnectRover());
		machine.attachRover();

		verifyAndIncrement(machine, Constants.ROVER_ACK);
		assertTrue(machine.isReadyToConnectBoard());
		machine.attachGameBoard();
		verifyAndIncrement(machine, Constants.GAMEBOARD_ACK);
		verifyAndIncrement(machine, Constants.START_GAME);
		verifyAndIncrement(machine, Constants.LEFT);
		verifyAndIncrement(machine, Constants.RIGHT);
		verifyAndIncrement(machine, Constants.FORWARD);
		verifyAndIncrement(machine, Constants.BACKWARD);
		verifyAndIncrement(machine, Constants.STOP);
		verifyAndIncrement(machine, Constants.END_GAME);
		assertTrue(machine.isReadyToConnectGamePieces());
	}

	@Test
	void testGestureConnectsFirst() {
		GameServerStateMachine machine = new GameServerStateMachine();
		verifyAndIncrement(machine, Constants.CONNECT_GESTURE);
		verifyAndIncrement(machine, Constants.CONNECT_GUI);
		assertTrue(machine.isReadyToConnectGamePieces());
		machine.attachLeaderboard();
	}

	@Test
	void testErrorBetweenGestureAndGUI() {
		GameServerStateMachine machine = new GameServerStateMachine();
		verifyAndIncrement(machine, Constants.CONNECT_GESTURE);
		machine.setErrorState();
		verifyAndIncrement(machine, Constants.CONNECT_GUI);
		assertFalse(machine.isReadyToConnectGamePieces());
		verifyAndIncrement(machine, Constants.CONNECT_GESTURE);
		assertTrue(machine.isReadyToConnectGamePieces());
	}

	@Test
	void testErrorBetweenGUIandGesture() {
		GameServerStateMachine machine = new GameServerStateMachine();
		verifyAndIncrement(machine, Constants.CONNECT_GUI);
		machine.setErrorState();
		verifyAndIncrement(machine, Constants.CONNECT_GESTURE);
		assertFalse(machine.isReadyToConnectGamePieces());
		verifyAndIncrement(machine, Constants.CONNECT_GUI);
		assertTrue(machine.isReadyToConnectGamePieces());
	}
	
	@Test
	void testErrorBeforeAnythingConnects()
	{
		GameServerStateMachine machine = new GameServerStateMachine();
		machine.setErrorState();
		assertTrue(machine.isValidState(Constants.CONNECT_GUI));
		assertTrue(machine.isValidState(Constants.CONNECT_GESTURE));
		assertFalse(machine.isValidState(Constants.ROVER_ACK));
		assertTrue(machine.hasErrorOccurred());
		// check that error state is cleared after connecting gui/gesture
		verifyAndIncrement(machine, Constants.CONNECT_GUI);
		assertFalse(machine.hasErrorOccurred());
		verifyAndIncrement(machine, Constants.CONNECT_GESTURE);
		machine.attachLeaderboard();
		assertTrue(machine.isReadyToConnectRover());

	}
	@Test
	void testInvalidStates()
	{
		GameServerStateMachine machine = new GameServerStateMachine();
		verifyAndIncrement(machine, Constants.CONNECT_GESTURE);
		verifyAndIncrement(machine, Constants.CONNECT_GUI);
		assertTrue(machine.isReadyToConnectGamePieces());
		machine.attachLeaderboard();
		assertFalse(machine.isValidState(Constants.CONNECT_GESTURE));
		assertFalse(machine.isValidState(Constants.CONNECT_GUI));
		
		assertTrue(machine.isReadyToConnectRover());
		machine.attachRover();
		verifyAndIncrement(machine, Constants.ROVER_ACK);
	
		assertTrue(machine.isReadyToConnectBoard());
		assertFalse(machine.isValidState(Constants.ROVER_ACK));
		
		machine.attachGameBoard();
		verifyAndIncrement(machine, Constants.GAMEBOARD_ACK);
		
		assertFalse(machine.isValidState(Constants.ROVER_ACK));
		assertFalse(machine.isValidState(Constants.GAMEBOARD_ACK));
		assertFalse(machine.isValidState(Constants.CONNECT_GESTURE));
		assertFalse(machine.isValidState(Constants.CONNECT_GUI));
		assertFalse(machine.isValidState(Constants.END_GAME));
		assertFalse(machine.isValidState(Constants.LEFT));
		assertFalse(machine.isValidState(Constants.RIGHT));
		assertFalse(machine.isValidState(Constants.FORWARD));
		assertFalse(machine.isValidState(Constants.BACKWARD));
		assertFalse(machine.isValidState(Constants.STOP));
		
		
		verifyAndIncrement(machine, Constants.START_GAME);
		assertFalse(machine.isValidState(Constants.ROVER_ACK));
		assertFalse(machine.isValidState(Constants.GAMEBOARD_ACK));
		assertFalse(machine.isValidState(Constants.CONNECT_GESTURE));
		assertFalse(machine.isValidState(Constants.CONNECT_GUI));
		assertFalse(machine.isValidState(Constants.START_GAME));
		
		verifyAndIncrement(machine, Constants.LEFT);
		verifyAndIncrement(machine, Constants.RIGHT);
		verifyAndIncrement(machine, Constants.FORWARD);
		verifyAndIncrement(machine, Constants.BACKWARD);
		verifyAndIncrement(machine, Constants.STOP);
		verifyAndIncrement(machine, Constants.END_GAME);
		assertTrue(machine.isReadyToConnectGamePieces());
		assertEquals(GameServerState.GUI_AND_GESTURE_CONNECTED, machine.getCurrentState());
	}

	private void verifyAndIncrement(GameServerStateMachine machine, String socketMsg) {
		assertTrue(machine.isValidState(socketMsg));
		machine.incrementState(socketMsg);
	}
}
