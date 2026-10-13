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

import java.util.Set;

public class CommandStep {

    private static final Set<String> VALID_COMMANDS = Set.of(
            Constants.FORWARD, Constants.BACKWARD, Constants.LEFT, Constants.RIGHT, Constants.STOP);

    private String command;
    private int durationMs;

    public CommandStep() {
    }

    public CommandStep(String command, int durationMs) {
        this.command = command;
        this.durationMs = durationMs;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(int durationMs) {
        this.durationMs = durationMs;
    }

    /**
     * Returns true if the command is in the allowed whitelist {F, B, L, R, S}
     * and durationMs is between 0 and maxStepDurationMs (inclusive).
     */
    public boolean isValid(int maxStepDurationMs) {
        return command != null
                && VALID_COMMANDS.contains(command)
                && durationMs >= 0
                && durationMs <= maxStepDurationMs;
    }
}
