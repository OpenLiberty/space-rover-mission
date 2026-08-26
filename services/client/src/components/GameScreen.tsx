/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import React from "react";
import { useNavigate } from "react-router-dom";
import PromptInput from "./PromptInput";
import { ReactComponent as Combomark } from "assets/openliberty_combomark.svg";
import useGameModes from "hooks/useGameModes";
import usePromptControls from "hooks/usePromptControls";
import useKeyboardControls from "hooks/useKeyboardControls";
import { GameState } from "hooks/useGame";

type Props = {
  playerName: string;
  gameMode: string;
  socket: WebSocket | null;
  gameSocketURL: string;
};

const GameScreen = ({ playerName, gameMode, socket, gameSocketURL }: Props) => {
  const navigate = useNavigate();
  const gameModes = useGameModes();
  const { isExecuting, nearBoundary, executeScript, cancelScript } = usePromptControls(socket, GameState.InGame);
  useKeyboardControls(socket, GameState.InGame, isExecuting);

  return (
    <div className="container mx-auto flex flex-col gap-12 justify-center h-full">
      <div className="flex flex-row">
        <div className="flex-1 flex flex-col items-center">
          <Combomark className="h-24 mr-16" />
          <p className="text-orange text-3xl">Space Rover Mission</p>
        </div>
        <div className="flex-1 text-center">
          <h2 className="text-gray-50 text-7xl font-semibold mb-5">
            {playerName}
          </h2>
          <p className="text-orange text-3xl">
            {gameModes.find(m => String(m.id) === gameMode)?.name}
          </p>
        </div>
      </div>
      <PromptInput
        gameSocketURL={gameSocketURL}
        isExecuting={isExecuting}
        nearBoundary={nearBoundary}
        executeScript={executeScript}
        cancelScript={cancelScript}
      />
      <div className="my-10 mx-auto">
        <button
          className="bg-red-600 hover:bg-red-500 text-3xl px-10 py-5 rounded-lg"
          onClick={() => navigate("/")}
        >
          End mission
        </button>
      </div>
    </div>
  );
};

export default GameScreen;
