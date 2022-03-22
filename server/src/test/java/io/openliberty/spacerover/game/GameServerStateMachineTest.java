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
