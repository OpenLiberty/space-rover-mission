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
import { useNavigate } from "react-router-dom";
import Stat from "./Stat";
import HealthBar from "./HealthBar";
import { ReactComponent as Combomark } from "assets/openliberty_combomark.svg";
import useGameModes from "hooks/useGameModes";

type Props = {
  playerName: string;
  gameMode: string;
  health: number;
  score: number;
  time: string;
};

const GameScreen = ({ playerName, gameMode, health, score, time }: Props) => {
  const navigate = useNavigate();
  const gameModes = useGameModes();

  return (
    <div className="container mx-auto flex flex-col gap-12 justify-center h-full">
      <div className="flex flex-row">
        <div className="flex-1 flex flex-col items-center">
          <Combomark className="h-24 mr-16" />
          <p className="text-orange text-3xl">Space Rover Mission</p>
        </div>
        <div className="flex-1 text-center">
          <h2 className="text-gray-50 text-7xl font-semibold mb-5">
            {playerName}
          </h2>
          <p className="text-orange text-3xl">
            {gameModes[parseInt(gameMode) - 1]?.name}
          </p>
        </div>
      </div>
      <div className="mx-auto w-4/5">
        <HealthBar health={health} />
      </div>
      <div className="flex flex-row">
        <Stat title="Time remaining" value={time} />
        <Stat title="Total score" value={score} />
      </div>
      <div className="my-10 mx-auto">
        <button
          className="bg-red-600 hover:bg-red-500 text-3xl px-10 py-5 rounded-lg"
          onClick={() => navigate("/")}
        >
          End mission
        </button>
      </div>
    </div>
  );
};

export default GameScreen;
