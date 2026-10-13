/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import { useState, useEffect, useRef } from "react";
import { GameState } from "./useGame";

// Calibration constants from specs/free-roam-spec.md
const MS_PER_DEGREE = 8;
const MS_PER_CM = 18;
const SOFT_BOUNDARY_RADIUS_CM = 150;

export interface CommandStep {
  command: string;
  durationMs: number;
}

export interface CommandScript {
  steps: CommandStep[];
}

const usePromptControls = (websocket: WebSocket | null, gameState: GameState) => {
  const [isExecuting, setIsExecuting] = useState(false);
  const [nearBoundary, setNearBoundary] = useState(false);

  const timeoutIds = useRef<number[]>([]);
  const deadReckoning = useRef({ x: 0, y: 0, headingDeg: 0 });

  const cancelScript = () => {
    timeoutIds.current.forEach((id) => window.clearTimeout(id));
    timeoutIds.current = [];
    if (websocket && websocket.readyState === WebSocket.OPEN) {
      websocket.send("S");
    }
    setIsExecuting(false);
  };

  const executeScript = (script: CommandScript) => {
    if (isExecuting || !websocket || gameState !== GameState.InGame) {
      return;
    }

    // Dead reckoning simulation
    let x = 0;
    let y = 0;
    let headingDeg = 0;
    let boundary = false;

    for (const step of script.steps) {
      const headingRad = (headingDeg * Math.PI) / 180;
      switch (step.command) {
        case "F":
          x += Math.cos(headingRad) * (step.durationMs / MS_PER_CM);
          y += Math.sin(headingRad) * (step.durationMs / MS_PER_CM);
          break;
        case "B":
          x -= Math.cos(headingRad) * (step.durationMs / MS_PER_CM);
          y -= Math.sin(headingRad) * (step.durationMs / MS_PER_CM);
          break;
        case "L":
          headingDeg -= step.durationMs / MS_PER_DEGREE;
          break;
        case "R":
          headingDeg += step.durationMs / MS_PER_DEGREE;
          break;
      }
      if (Math.sqrt(x * x + y * y) > SOFT_BOUNDARY_RADIUS_CM) {
        boundary = true;
      }
    }

    // Reset dead reckoning ref for this execution
    deadReckoning.current = { x: 0, y: 0, headingDeg: 0 };
    setNearBoundary(boundary);
    setIsExecuting(true);

    // Build setTimeout chain
    let elapsed = 0;
    const ids: number[] = [];

    for (let i = 0; i < script.steps.length; i++) {
      const step = script.steps[i];
      const id = window.setTimeout(() => {
        websocket.send(step.command);
      }, elapsed);
      ids.push(id);
      elapsed += step.durationMs;
    }

    // Final stop command
    const stopId = window.setTimeout(() => {
      websocket.send("S");
      setIsExecuting(false);
    }, elapsed);
    ids.push(stopId);

    timeoutIds.current = ids;
  };

  useEffect(() => {
    if (gameState !== GameState.InGame && isExecuting) {
      cancelScript();
    }
  }, [gameState]); // eslint-disable-line react-hooks/exhaustive-deps

  return { isExecuting, nearBoundary, executeScript, cancelScript };
};

export default usePromptControls;
