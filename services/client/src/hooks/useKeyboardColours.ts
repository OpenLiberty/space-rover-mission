/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import { useEffect } from "react";
import { GameState } from "./useGame";

enum Commands {
  b = "BLU",
  g = "GRN",
  p = "PUR",
  y = "YW",
  r = "RED",
}

type K = keyof typeof Commands;

const useKeyboardColours = (websocket: WebSocket | null, gameState: GameState) => {
  useEffect(() => {
    function keydownHandler(event: KeyboardEvent) {
      if (!websocket) {
        return;
      }

      if (gameState !== GameState.InGame) {
        return;
      }

      const key = event.key.toLowerCase();
      if (!(key in Commands)) {
        return;
      }

      websocket.send(Commands[key as K]);
    }

    window.addEventListener("keydown", keydownHandler);

    return () => {
      window.removeEventListener("keydown", keydownHandler);
    };
  }, [websocket, gameState]);
};

export default useKeyboardColours;
