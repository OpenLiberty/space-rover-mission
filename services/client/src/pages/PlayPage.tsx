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
import { Navigate } from "react-router-dom";
import PlayerForm from "components/PlayerForm";
import GameScreen from "components/GameScreen";
import GameStateMessage from "components/GameStateMessage";
import BatteryStatus from "components/BatteryStatus";
import useGame, { GameState } from "hooks/useGame";
import { gameSocketURL, gameDurationSeconds } from "lib/config";

const PlayPage = () => {
  const {
    playerName,
    gameMode,
    gameModes,
    gameState,
    formattedTime,
    health,
    score,
    startGame,
    error,
    battery,
  } = useGame(gameSocketURL, gameDurationSeconds);

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
          <GameStateMessage
            state={gameState}
            errorMessage={error}
          />
          <BatteryStatus
            batteryPercentage={battery}
          />
        </div>
      );
    case GameState.InGame:
      return (
        <GameScreen
          playerName={playerName}
          gameMode={gameMode}
          health={health}
          score={score}
          time={formattedTime}
        />
      );
    case GameState.GameEnded:
      return (
        <Navigate
          to={`/leaderboard?player=${encodeURIComponent(playerName)}&gameMode=${gameMode}`}
        />
      );
    default:
      return null;
  }
};

export default PlayPage;
