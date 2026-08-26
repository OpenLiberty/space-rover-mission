/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import React, { useState, useEffect } from "react";
import { CommandScript } from "hooks/usePromptControls";
import { scripts } from "../specs/scripts/index";

// UI label strings from specs/free-roam-spec.md
const EXECUTE_BUTTON_LABEL = "Execute";
const CANCEL_BUTTON_LABEL = "Stop";
const STATUS_EXECUTING = "Executing rover script...";
const STATUS_COMPLETED = "Rover script completed.";
const STATUS_CANCELLED = "Rover script cancelled.";
const WARNING_BOUNDARY =
  "Warning: this script may take the rover near the boundary.";

type Props = {
  gameSocketURL: string;
  isExecuting: boolean;
  nearBoundary: boolean;
  executeScript: (script: CommandScript) => void;
  cancelScript: () => void;
};

const PromptInput = ({
  isExecuting,
  nearBoundary,
  executeScript,
  cancelScript,
}: Props) => {
  const [selectedIndex, setSelectedIndex] = useState(0);
  const [status, setStatus] = useState<string | null>(null);
  const [wasExecuting, setWasExecuting] = useState(false);

  useEffect(() => {
    if (isExecuting) {
      setWasExecuting(true);
    } else if (wasExecuting) {
      setWasExecuting(false);
      setStatus(STATUS_COMPLETED);
    }
  }, [isExecuting]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleExecute = () => {
    if (isExecuting || scripts.length === 0) {
      return;
    }
    setStatus(STATUS_EXECUTING);
    executeScript({ steps: scripts[selectedIndex].steps });
  };

  const handleStop = () => {
    cancelScript();
    setStatus(STATUS_CANCELLED);
  };

  return (
    <div className="flex flex-col gap-4 w-full">
      <label className="text-gray-300 text-sm" htmlFor="rover-script-select">
        Select a rover script:
      </label>
      <select
        id="rover-script-select"
        className="bg-gray-800 text-gray-100 rounded-lg p-3 w-full"
        value={selectedIndex}
        onChange={(e) => setSelectedIndex(Number(e.target.value))}
        disabled={isExecuting}
      >
        {scripts.map((script, index) => (
          <option key={index} value={index}>
            {script.name}
          </option>
        ))}
      </select>
      {scripts.length > 0 && (
        <p className="text-gray-500 text-sm">
          {scripts[selectedIndex].description}
        </p>
      )}
      {nearBoundary && (
        <p className="text-yellow-400 text-sm">{WARNING_BOUNDARY}</p>
      )}
      <div className="flex flex-row gap-4">
        <button
          className="bg-blue-600 hover:bg-blue-500 disabled:opacity-50 disabled:cursor-not-allowed text-white px-6 py-3 rounded-lg"
          onClick={handleExecute}
          disabled={isExecuting || scripts.length === 0}
        >
          {EXECUTE_BUTTON_LABEL}
        </button>
        <button
          className="bg-red-600 hover:bg-red-500 text-white px-6 py-3 rounded-lg"
          onClick={handleStop}
        >
          {CANCEL_BUTTON_LABEL}
        </button>
      </div>
      {status && <p className="text-gray-300 text-sm">{status}</p>}
    </div>
  );
};

export default PromptInput;
