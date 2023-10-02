/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import { useState, useEffect } from "react";
import { GameState } from "./useGame";

enum ArrowKeys {
  Left = "ArrowLeft",
  Right = "ArrowRight",
  Up = "ArrowUp",
  Down = "ArrowDown",
}

enum Commands {
  Left = "L",
  Right = "R",
  Forward = "F",
  Reverse = "B",
  Stop = "S",
}

const useKeyboardControls = (websocket: WebSocket | null, gameState: GameState) => {
  const [leftActive, setLeftActive] = useState(false);
  const [rightActive, setRightActive] = useState(false);
  const [upActive, setUpActive] = useState(false);
  const [downActive, setDownActive] = useState(false);

  const [lastCommand, setLastCommand] = useState(Commands.Stop);

  useEffect(() => {
    function keydownHandler(event: KeyboardEvent) {
      if (!websocket) {
        return;
      }

      if (gameState !== GameState.InGame) {
        return;
      }

      // only allow 1 key pressed at a time
      if (leftActive || rightActive || upActive || downActive) {
        websocket.send(lastCommand);
        return;
      }

      const key = event.key;
      switch (key) {
        case ArrowKeys.Left:
          setLeftActive(true);
          websocket.send(Commands.Left);
          setLastCommand(Commands.Left);
          break;
        case ArrowKeys.Right:
          setRightActive(true);
          websocket.send(Commands.Right);
          setLastCommand(Commands.Right);
          break;
        case ArrowKeys.Up:
          setUpActive(true);
          websocket.send(Commands.Forward);
          setLastCommand(Commands.Forward);
          break;
        case ArrowKeys.Down:
          setDownActive(true);
          websocket.send(Commands.Reverse);
          setLastCommand(Commands.Reverse);
          break;
      }
    }

    function keyupHandler(event: KeyboardEvent) {
      if (!websocket) {
        return;
      }

      const key = event.key;
      switch (key) {
        case ArrowKeys.Left:
          if (lastCommand === Commands.Left) {
            setLeftActive(false);
            websocket.send(Commands.Stop);
            setLastCommand(Commands.Stop);
          }
          break;
        case ArrowKeys.Right:
          if (lastCommand === Commands.Right) {
            setRightActive(false);
            websocket.send(Commands.Stop);
            setLastCommand(Commands.Stop);
          }
          break;
        case ArrowKeys.Up:
          if (lastCommand === Commands.Forward) {
            setUpActive(false);
            websocket.send(Commands.Stop);
            setLastCommand(Commands.Stop);
          }
          break;
        case ArrowKeys.Down:
          if (lastCommand === Commands.Reverse) {
            setDownActive(false);
            websocket.send(Commands.Stop);
            setLastCommand(Commands.Stop);
          }
          break;
      }
    }

    window.addEventListener("keydown", keydownHandler);
    window.addEventListener("keyup", keyupHandler);

    return () => {
      window.removeEventListener("keydown", keydownHandler);
      window.removeEventListener("keyup", keyupHandler);
    };
  }, [websocket, leftActive, rightActive, upActive, downActive, lastCommand, gameState]);

  return { leftActive, rightActive, upActive, downActive };
};

export default useKeyboardControls;
