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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import io.openliberty.spacerover.game.models.SocketMessages;

public class GameServerStateMachineTest {
    @Test
    void testIncrementState() {
        GameServerStateMachine machine = new GameServerStateMachine();
        verifyAndIncrement(machine, SocketMessages.CONNECT_GUI);
        verifyAndIncrement(machine, SocketMessages.CONNECT_GESTURE);
        assertTrue(machine.isReadyToConnectGamePieces());
        machine.attachLeaderboard();
        machine.attachRover();
        verifyAndIncrement(machine, SocketMessages.ROVER_ACK);
        machine.attachGameBoard();
        verifyAndIncrement(machine, SocketMessages.GAMEBOARD_ACK);
        verifyAndIncrement(machine, SocketMessages.START_GAME);
        verifyAndIncrement(machine, SocketMessages.LEFT);
        verifyAndIncrement(machine, SocketMessages.RIGHT);
        verifyAndIncrement(machine, SocketMessages.FORWARD);
        verifyAndIncrement(machine, SocketMessages.BACKWARD);
        verifyAndIncrement(machine, SocketMessages.STOP);
        verifyAndIncrement(machine, SocketMessages.END_GAME);
        assertTrue(machine.isReadyToConnectGamePieces());
    }

    private void verifyAndIncrement(GameServerStateMachine machine, String socketMsg) {
        assertTrue(machine.isValidState(socketMsg));
        machine.incrementState(socketMsg);
    }
}
