/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.spacerover.game;

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
        machine.attachRover();
        verifyAndIncrement(machine, Constants.ROVER_ACK);
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

    private void verifyAndIncrement(GameServerStateMachine machine, String socketMsg) {
        assertTrue(machine.isValidState(socketMsg));
        machine.incrementState(socketMsg);
    }
}
