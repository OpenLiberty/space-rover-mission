/*******************************************************************************
 * Copyright (c) 2022, 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import { useState, useEffect, useRef } from "react";
import useGameModes from "./useGameModes";

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
  End = "endGame",
  Error = "error",
  Battery = "battery",
}

const MSG_DELIMITER = "|";

const formatMessage = (event: Event, data: string = "") => {
  return `${event}${MSG_DELIMITER}${data}`;
};

const useGame = (gameSocketURL: string) => {
  const [gameState, setGameState] = useState(GameState.Connecting);
  const socket = useRef<WebSocket | null>(null);
  const [playerName, setPlayerName] = useState("");
  const [gameMode, setGameMode] = useState("5");
  const [battery, setBattery] = useState(-1);
  const [error, setError] = useState("");
  const gameModes = useGameModes();

  useEffect(() => {
    socket.current = new WebSocket(gameSocketURL);
    return () => {
      socket.current?.close();
      socket.current = null;
    };
  }, [gameSocketURL]);

  useEffect(() => {
    if (!socket.current) return;

    socket.current.onopen = () => {
      sendMessage(Event.ConnectGUI);
      setGameState(GameState.Waiting);
    };
    socket.current.onerror = () => {
      setError("Failed to connect to game service.");
      setGameState(GameState.Error);
    };
    socket.current.onmessage = (ev) => {
      const [event, data] = ev.data.split(MSG_DELIMITER);
      switch (event) {
        case Event.ConnectGUI:
        case Event.Start:
          break;
        case Event.ServerReady:
          setGameState(GameState.NotStarted);
          break;
        case Event.End:
          setGameState(GameState.GameEnded);
          break;
        case Event.Error:
          setError(data);
          setGameState(GameState.Error);
          break;
        case Event.Battery:
          setBattery(data);
          break;
        default:
          console.log(`Received unknown event: ${event}`);
      }
    };
  }, [socket, battery]);

  function startGame(playerName: string, gameMode: string) {
    if (gameState === GameState.NotStarted) {
      setPlayerName(playerName);
      setGameMode(gameMode);
      sendMessage(Event.Start, [encodeURIComponent(playerName), gameMode].join(","));
      setGameState(GameState.InGame);
    }
  }

  function sendMessage(event: Event, data: string = "") {
    socket.current?.send(formatMessage(event, data));
  }

  return {
    playerName,
    gameMode,
    gameModes,
    gameState,
    startGame,
    error,
    battery,
    socket: socket.current,
  };
};

export default useGame;
