/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import React, { useState } from "react";
import { Link } from "react-router-dom";
import { ReactComponent as Combomark } from "assets/openliberty_combomark.svg";
import { GameMode } from "hooks/useGameModes";

type Props = {
  gameModes: GameMode[];
  isDisabled: boolean;
  onSubmit: (playerName: string, gameMode: string) => void;
};

const PlayerForm = ({ gameModes, isDisabled, onSubmit }: Props) => {
  const [playerName, setPlayerName] = useState("");
  const [gameMode, setGameMode] = useState("1");

  return (
    <form
      className="max-w-screen-md flex flex-col gap-5 mx-auto text-xl"
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit(playerName, gameMode);
      }}
    >
      <div className="flex flex-col items-center">
        <Combomark className="h-24 mr-16" />
        <p className="text-orange text-3xl">Space Rover Mission</p>
      </div>
      <div>
        <label className="block text-gray-300 text-2xl">Enter your name</label>
        <input
          className="w-full rounded-lg px-5 py-5"
          type="text"
          autoFocus
          autoComplete="false"
          autoCorrect="false"
          spellCheck="false"
          value={playerName}
          onChange={(e) => setPlayerName(e.target.value)}
        />
      </div>
      <div>
        <label className="block text-gray-300 text-2xl">Select game mode</label>
        <div className="relative">
          <select
            className="w-full rounded-lg px-5 py-5 appearance-none"
            value={gameMode}
            onChange={(e) => setGameMode(e.target.value)}
          >
            {gameModes.map((gameMode) => (
              <option key={gameMode.id} value={gameMode.id}>
                {gameMode.name}
              </option>
            ))}
          </select>
          <div className="absolute right-0 inset-y-0 flex items-center mx-3">
            &#x25BC;
          </div>
        </div>
        <div className="text-green-light text-center text-base py-3">
          {gameModes[parseInt(gameMode) - 1].description}
        </div>
      </div>
      <div className="flex flex-row gap-2">
        <Link
          className="w-1/3 bg-gray-200 hover:bg-gray-300 text px-5 py-5 rounded-lg"
          to="/"
        >
          Go back
        </Link>
        <input
          className="w-2/3 hover:cursor-pointer bg-green hover:bg-green-light disabled:bg-gray-500 disabled:cursor-not-allowed px-5 py-5 rounded-lg"
          type="submit"
          disabled={isDisabled || playerName === ""}
          value="Begin mission"
        />
      </div>
    </form>
  );
};

export default PlayerForm;
