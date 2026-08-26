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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.openliberty.spacerover.game.models.CommandStep;
import io.openliberty.spacerover.game.models.Constants;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.JsonObject;

@ApplicationScoped
public class MoveExpander {

    @Inject
    @ConfigProperty(name = "io.openliberty.freeroam.ms_per_degree", defaultValue = "8")
    int msPerDegree;

    @Inject
    @ConfigProperty(name = "io.openliberty.freeroam.ms_per_cm", defaultValue = "18")
    int msPerCm;

    @Inject
    @ConfigProperty(name = "io.openliberty.freeroam.max_duration_ms", defaultValue = "30000")
    int maxDurationMs;

    @Inject
    @ConfigProperty(name = "io.openliberty.freeroam.max_step_duration_ms", defaultValue = "10000")
    int maxStepDurationMs;

    // --- move primitives ---

    public List<CommandStep> expandSpinLeft(int durationMs) {
        List<CommandStep> steps = new ArrayList<>();
        steps.add(new CommandStep(Constants.LEFT, cap(durationMs)));
        steps.add(new CommandStep(Constants.STOP, 0));
        return steps;
    }

    public List<CommandStep> expandSpinRight(int durationMs) {
        List<CommandStep> steps = new ArrayList<>();
        steps.add(new CommandStep(Constants.RIGHT, cap(durationMs)));
        steps.add(new CommandStep(Constants.STOP, 0));
        return steps;
    }

    public List<CommandStep> expandForward(int distanceCm) {
        List<CommandStep> steps = new ArrayList<>();
        steps.add(new CommandStep(Constants.FORWARD, cap(distanceCm * msPerCm)));
        steps.add(new CommandStep(Constants.STOP, 0));
        return steps;
    }

    public List<CommandStep> expandBackward(int distanceCm) {
        List<CommandStep> steps = new ArrayList<>();
        steps.add(new CommandStep(Constants.BACKWARD, cap(distanceCm * msPerCm)));
        steps.add(new CommandStep(Constants.STOP, 0));
        return steps;
    }

    public List<CommandStep> expandTurnLeft(int degrees) {
        List<CommandStep> steps = new ArrayList<>();
        steps.add(new CommandStep(Constants.LEFT, cap(degrees * msPerDegree)));
        steps.add(new CommandStep(Constants.STOP, 0));
        return steps;
    }

    public List<CommandStep> expandTurnRight(int degrees) {
        List<CommandStep> steps = new ArrayList<>();
        steps.add(new CommandStep(Constants.RIGHT, cap(degrees * msPerDegree)));
        steps.add(new CommandStep(Constants.STOP, 0));
        return steps;
    }

    public List<CommandStep> expandFigure8(int repetitions) {
        // one figure-8 = full left spin + full right spin (full = 360 degrees)
        int fullSpinMs = cap(360 * msPerDegree);
        List<CommandStep> steps = new ArrayList<>();
        for (int i = 0; i < repetitions; i++) {
            steps.add(new CommandStep(Constants.LEFT, fullSpinMs));
            steps.add(new CommandStep(Constants.RIGHT, fullSpinMs));
        }
        steps.add(new CommandStep(Constants.STOP, 0));
        return steps;
    }

    public List<CommandStep> expandSquare(int sideCm) {
        // square = 4 × (forward(sideCm) + turn_left 90°)
        int forwardMs = cap(sideCm * msPerCm);
        int turnMs = cap(90 * msPerDegree);
        List<CommandStep> steps = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            steps.add(new CommandStep(Constants.FORWARD, forwardMs));
            steps.add(new CommandStep(Constants.LEFT, turnMs));
        }
        steps.add(new CommandStep(Constants.STOP, 0));
        return steps;
    }

    public List<CommandStep> expandPatrol(int durationMs) {
        // alternating L/R sweeps of maxStepDurationMs each, filling total durationMs
        int sweepMs = cap(maxStepDurationMs);
        List<CommandStep> steps = new ArrayList<>();
        int elapsed = 0;
        boolean leftTurn = true;
        while (elapsed + sweepMs <= durationMs) {
            steps.add(new CommandStep(leftTurn ? Constants.LEFT : Constants.RIGHT, sweepMs));
            elapsed += sweepMs;
            leftTurn = !leftTurn;
        }
        steps.add(new CommandStep(Constants.STOP, 0));
        return steps;
    }

    /**
     * Dispatches a semantic move JsonObject (with "name" and "parameters" fields)
     * to the correct expand* method and returns the resulting steps.
     */
    public List<CommandStep> expand(JsonObject semanticMove) {
        String name = semanticMove.getString("name", "");
        JsonObject params = semanticMove.containsKey("parameters")
                ? semanticMove.getJsonObject("parameters")
                : JsonObject.EMPTY_JSON_OBJECT;

        switch (name) {
            case "spin_left":
                return expandSpinLeft(params.getInt("duration_ms", 0));
            case "spin_right":
                return expandSpinRight(params.getInt("duration_ms", 0));
            case "forward":
                return expandForward(params.getInt("distance_cm", 0));
            case "backward":
                return expandBackward(params.getInt("distance_cm", 0));
            case "turn_left":
                return expandTurnLeft(params.getInt("degrees", 0));
            case "turn_right":
                return expandTurnRight(params.getInt("degrees", 0));
            case "figure_8":
                return expandFigure8(params.getInt("repetitions", 1));
            case "square":
                return expandSquare(params.getInt("side_cm", 0));
            case "patrol":
                return expandPatrol(params.getInt("duration_ms", 0));
            default:
                return new ArrayList<>();
        }
    }

    /** Caps a computed duration at maxStepDurationMs. */
    private int cap(int durationMs) {
        return Math.min(durationMs, maxStepDurationMs);
    }
}
