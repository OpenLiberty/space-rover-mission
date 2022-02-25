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
import React from "react";
import { Navigate } from "react-router-dom";
import PlayerForm from "components/PlayerForm";
import GameScreen from "components/GameScreen";
import useGame, { GameState } from "hooks/useGame";
import { gameSocketURL, gameDurationSeconds } from "lib/config";

const PlayPage = () => {
  const {
    playerName,
    gameState,
    formattedTime,
    health,
    score,
    startGame,
    endGame,
  } = useGame(gameSocketURL, gameDurationSeconds);

  switch (gameState) {
    case GameState.Connecting:
    case GameState.Error:
    case GameState.NotStarted:
      return (
        <PlayerForm
          isConnecting={gameState === GameState.Connecting}
          isError={gameState === GameState.Error}
          onSubmit={startGame}
        />
      );
    case GameState.InGame:
      return (
        <GameScreen
          playerName={playerName}
          health={health}
          score={score}
          time={formattedTime}
          stopGame={endGame}
        />
      );
    case GameState.GameEnded:
      return <Navigate to={`/leaderboard?name=${playerName}`} />;
    default:
      return null;
  }
};

export default PlayPage;
