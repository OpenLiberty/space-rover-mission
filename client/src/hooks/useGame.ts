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

import useSound from "use-sound";
import crashSoundFile from "assets/sounds/crash.wav";
import scoreSoundFile from "assets/sounds/score.mp3";
import timerSoundFile from "assets/sounds/timer.mp3";

import useTimer from "./useTimer";
import useKeyboardControls from "./useKeyboardControls";

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
  End = "endGame",
  Error = "error",
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

  const [, { sound: crashSound }] = useSound(crashSoundFile, {
    volume: 0.5,
    playbackRate: 1.5,
  });
  const [, { sound: scoreSound }] = useSound(scoreSoundFile);
  const [, { sound: timerSound }] = useSound(timerSoundFile);

  const { formattedTime, timeRemaining, startTimer, stopTimer } =
    useTimer(durationInSeconds);

  useKeyboardControls(socket.current);

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
          const newHealth = parseInt(data);
          if (newHealth < health) {
            crashSound.play();
          }
          setHealth(newHealth);
          break;
        case Event.Score:
          const newScore = parseInt(data);
          if (newScore > score) {
            scoreSound.play();
          }
          setScore(newScore);
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
        default:
          console.log(`Received unknown event: ${event}`);
      }
    };
  }, [socket, crashSound, scoreSound, timerSound, health, score]);

  useEffect(() => {
    if (timeRemaining === 10) {
      timerSound?.play();
    }
    if (timeRemaining === 0) {
      endGame();
    }
  }, [timeRemaining, timerSound]);

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
      sendMessage(Event.End, String(durationInSeconds - timeRemaining));
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