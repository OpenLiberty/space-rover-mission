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

import java.util.ArrayList;
import java.util.List;

public class CommandScript {

    private List<CommandStep> steps;

    public CommandScript() {
        this.steps = new ArrayList<>();
    }

    public CommandScript(List<CommandStep> steps) {
        this.steps = steps;
    }

    public List<CommandStep> getSteps() {
        return steps;
    }

    public void setSteps(List<CommandStep> steps) {
        this.steps = steps;
    }

    /**
     * Returns true if every step passes {@link CommandStep#isValid(int)} and the
     * sum of all durationMs values does not exceed maxTotalDurationMs.
     */
    public boolean isValid(int maxStepDurationMs, int maxTotalDurationMs) {
        if (steps == null) {
            return false;
        }
        long total = 0;
        for (CommandStep step : steps) {
            if (!step.isValid(maxStepDurationMs)) {
                return false;
            }
            total += step.getDurationMs();
        }
        return total <= maxTotalDurationMs;
    }
}
