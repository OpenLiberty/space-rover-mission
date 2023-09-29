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

import useSound from "use-sound";
import crashSoundFile from "assets/sounds/crash.wav";
import sunCrashSoundFile from "assets/sounds/sun_crash.wav";
import scoreSoundFile from "assets/sounds/score.mp3";
import timerSoundFile from "assets/sounds/timer.mp3";
import shortTimerSoundFile from "assets/sounds/short_timer.wav";

import useTimer from "./useTimer";
import useKeyboardColours from "./useKeyboardColours";
import useKeyboardControls from "./useKeyboardControls";
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
  ConnectGesture = "connectGesture",
  ServerReady = "serverReady",
  Start = "startGame",
  Health = "hp",
  Score = "score",
  PlanetChange = "planetChange",
  End = "endGame",
  Error = "error",
  Battery = "battery",
}

const MSG_DELMITER = "|";

const formatMessage = (event: Event, data: string = "") => {
  return `${event}${MSG_DELMITER}${data}`;
};

const useGame = (gameSocketURL: string, durationInSeconds: number) => {
  const [gameState, setGameState] = useState(GameState.Connecting);

  const socket = useRef<WebSocket | null>(null);

  const [playerName, setPlayerName] = useState("");
  const [gameMode, setGameMode] = useState("1");
  const [health, setHealth] = useState(100);
  const [score, setScore] = useState(0);
  const [battery, setBattery] = useState(-1);

  const [error, setError] = useState("");

  const [, { sound: crashSound }] = useSound(crashSoundFile, {
    volume: 0.5,
    playbackRate: 1.5,
  });
  const [, { sound: sunCrashSound }] = useSound(sunCrashSoundFile);
  const [, { sound: scoreSound }] = useSound(scoreSoundFile);
  const [, { sound: timerSound }] = useSound(timerSoundFile);
  const [, { sound: shortTimerSound }] = useSound(shortTimerSoundFile);

  const {
    formattedTime,
    timeRemaining,
    startTimer,
    stopTimer,
  } = useTimer(durationInSeconds);

  useKeyboardColours(socket.current, gameState);
  useKeyboardControls(socket.current, gameState);

  const gameModes = useGameModes();

  // setup socket
  useEffect(() => {
    socket.current = new WebSocket(gameSocketURL);

    return () => {
      socket.current?.close();
      socket.current = null;
    };
  }, [gameSocketURL]);

  // update socket handlers
  useEffect(() => {
    if (!socket.current) {
      return;
    }

    socket.current.onopen = (ev) => {
      sendMessage(Event.ConnectGUI);
      sendMessage(Event.ConnectGesture);
      setGameState(GameState.Waiting);
    };
    socket.current.onerror = (ev) => {
      setError("Failed to connect to game service.");
      setGameState(GameState.Error);
    };
    socket.current.onmessage = (ev) => {
      const [event, data] = ev.data.split(MSG_DELMITER);

      switch (event) {
        case Event.ConnectGUI:
        case Event.ConnectGesture:
        case Event.Start:
          // do nothing; these are client events
          break;
        case Event.ServerReady:
          setGameState(GameState.NotStarted);
          break;
        case Event.Health:
          const [newHealth, obstacle] = data.split(",");
          if (parseInt(newHealth) < health) {
            if (obstacle === "sun") {
              sunCrashSound.play();
            } else {
              crashSound.play();
            }
          }
          setHealth(newHealth);
          break;
        case Event.Score:
          const newScore = parseInt(data);
          if (newScore > score) {
            scoreSound.play();
            shortTimerSound.stop();
          }
          setScore(newScore);
          break;
        case Event.PlanetChange:
          shortTimerSound.play();
          break;
        case Event.End:
          stopTimer();
          timerSound.stop();
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
  }, [socket, crashSound, scoreSound, timerSound, shortTimerSound, health, score, battery]);

  useEffect(() => {
    if (timeRemaining === 10) {
      timerSound?.play();
    }
    if (timeRemaining === 0) {
      endGame();
    }
  }, [timeRemaining, timerSound]);

  function startGame(playerName: string, gameMode: string) {
    if (gameState === GameState.NotStarted) {
      setPlayerName(playerName);
      setGameMode(gameMode);
      sendMessage(Event.Start, [encodeURIComponent(playerName), gameMode].join(","));
      setGameState(GameState.InGame);
      startTimer();
    }
  }

  function endGame() {
    if (gameState === GameState.InGame) {
      sendMessage(Event.End, String(durationInSeconds - timeRemaining));
    }
  }

  function sendMessage(event: Event, data: string = "") {
    const message = formatMessage(event, data);
    socket.current?.send(message);
  }

  return {
    playerName,
    gameMode,
    gameModes,
    gameState,
    formattedTime,
    health,
    score,
    startGame,
    endGame,
    error,
    battery,
  };
};

export default useGame;
