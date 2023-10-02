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
import { GameState } from "hooks/useGame";

type Props = {
  state: GameState;
  errorMessage: string;
};

const GameStateMessage = ({ state, errorMessage }: Props) => {
  let colour;
  let message;

  switch (state) {
    case GameState.Connecting:
      colour = "text-yellow-500";
      message = "● Connecting to game service.";
      break;
    case GameState.Waiting:
      colour = "text-yellow-500";
      message = "● Waiting for game server to be ready.";
      break;
    case GameState.NotStarted:
      colour = "text-green-600";
      message = "● Connected to game service.";
      break;
    case GameState.Error:
      colour = "text-red-600";
      message = `● ${errorMessage}`;
      break;
    default:
      colour = "invisible";
      message = "";
  }

  return <p className={`text-xl text-center ${colour}`}>{message}</p>;
};

export default GameStateMessage;
