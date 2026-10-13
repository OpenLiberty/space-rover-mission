/*******************************************************************************
 * Copyright (c) 2022, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import React from "react";
import PlayerForm from "components/PlayerForm";
import GameScreen from "components/GameScreen";
import GameStateMessage from "components/GameStateMessage";
import BatteryStatus from "components/BatteryStatus";
import useGame, { GameState } from "hooks/useGame";
import { gameSocketURL } from "lib/config";

const PlayPage = () => {
  const {
    playerName,
    gameMode,
    gameModes,
    gameState,
    startGame,
    error,
    battery,
    socket,
  } = useGame(gameSocketURL);

  switch (gameState) {
    case GameState.Connecting:
    case GameState.Error:
    case GameState.Waiting:
    case GameState.NotStarted:
      return (
        <div className="flex flex-col gap-7 justify-center h-full">
          <PlayerForm
            gameModes={gameModes}
            isDisabled={gameState !== GameState.NotStarted}
            onSubmit={startGame}
          />
          <GameStateMessage state={gameState} errorMessage={error} />
          <BatteryStatus batteryPercentage={battery} />
        </div>
      );
    case GameState.InGame:
      return (
        <GameScreen
          playerName={playerName}
          gameMode={gameMode}
          socket={socket}
          gameSocketURL={gameSocketURL}
        />
      );
    case GameState.GameEnded:
      return (
        <div className="flex flex-col items-center justify-center h-full gap-6">
          <p className="text-white text-4xl">Mission complete.</p>
          <button
            className="bg-blue-600 hover:bg-blue-500 text-2xl px-8 py-4 rounded-lg"
            onClick={() => window.location.reload()}
          >
            Play again
          </button>
        </div>
      );
    default:
      return null;
  }
};

export default PlayPage;
