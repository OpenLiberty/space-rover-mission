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
import { useState, useEffect, useRef } from "react";
import useTimer from "./useTimer";

export enum GameState {
  Connecting,
  Error,
  Waiting,
  NotStarted,
  InGame,
  GameEnded,
}

enum Event {
  ConnectGUI = "connectGUI",
  ServerReady = "serverReady",
  Start = "startGame",
  Health = "hp",
  Score = "score",
  End = "endGame",
  Error = "error"
}

const MSG_DELMITER = "|";

const formatMessage = (event: Event, data: string = "") => {
  return `${event}${MSG_DELMITER}${data}`;
};

const useGame = (gameSocketURL: string, durationInSeconds: number) => {
  const [gameState, setGameState] = useState(GameState.Connecting);

  const socket = useRef<WebSocket | null>(null);

  const [playerName, setPlayerName] = useState("");
  const [health, setHealth] = useState(100);
  const [score, setScore] = useState(0);

  const [error, setError] = useState("");

  const {
    timeRemaining,
    formattedTime,
    startTimer,
    stopTimer,
  } = useTimer(durationInSeconds);

  useEffect(() => {
    const ws = new WebSocket(gameSocketURL);
    ws.onopen = (ev) => {
      sendMessage(Event.ConnectGUI);
      setGameState(GameState.Waiting);
    };
    ws.onerror = (ev) => {
      setError("Failed to connect to game service.")
      setGameState(GameState.Error);
    };
    ws.onmessage = (ev) => {
      const [event, data] = ev.data.split(MSG_DELMITER);

      switch (event) {
        case Event.ConnectGUI:
        case Event.Start:
          // do nothing; these are client events
          break;
        case Event.ServerReady:
          setGameState(GameState.NotStarted);
          break;
        case Event.Health:
          setHealth(parseInt(data));
          break;
        case Event.Score:
          setScore(parseInt(data));
          break;
        case Event.End:
          endGame();
          break;
        case Event.Error:
          setError(data);
          setGameState(GameState.Error);
          break;
        default:
          console.log(`Received unknown event: ${event}`);
      }
    };

    socket.current = ws;

    return () => ws.close();
  }, [gameSocketURL]);

  useEffect(() => {
    if (timeRemaining === 0 || health === 0) {
      endGame();
    }
  }, [timeRemaining, health]);

  function startGame(playerName: string) {
    if (gameState === GameState.NotStarted) {
      setPlayerName(playerName);
      sendMessage(Event.Start, playerName);
      setGameState(GameState.InGame);
      startTimer();
    }
  }

  function endGame() {
    if (gameState === GameState.InGame) {
      stopTimer();
      sendMessage(Event.End);
      setGameState(GameState.GameEnded);
    }
  }

  function sendMessage(event: Event, data: string = "") {
    const message = formatMessage(event, data);
    socket.current?.send(message);
  }

  return {
    playerName,
    gameState,
    formattedTime,
    health,
    score,
    startGame,
    endGame,
    error,
  };
};

export default useGame;
