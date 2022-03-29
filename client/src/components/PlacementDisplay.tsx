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
import { Link } from "react-router-dom";
import { LeaderboardEntry } from "hooks/useLeaderboard";
import { formatTime } from "lib/utils";

type Props = LeaderboardEntry;

const PlacementDisplay = ({ rank, player, time, health, score }: Props) => {
  return (
    <div className="text-gray-50 text-center pt-5 pb-10">
      <h1 className="text-green text-5xl">Mission completed</h1>
      <div className="flex gap-20 justify-center p-5">
        <div>
          <p className="text-gray-400">Rank</p>
          <p className="text-7xl text-semibold">{rank}</p>
        </div>
        <div>
          <p className="text-gray-400">Time</p>
          <p className="text-7xl text-semibold">{formatTime(time)}</p>
        </div>
        <div>
          <p className="text-gray-400">Health</p>
          <p className="text-7xl text-semibold">{health}%</p>
        </div>
        <div>
          <p className="text-gray-400">Score</p>
          <p className="text-7xl text-semibold">{score}</p>
        </div>
      </div>
      <p className="text-3xl">
        Thank you for playing, <span className="text-orange">{player}</span>
      </p>
      <Link
        className="inline-block mt-5 px-10 py-5 bg-green hover:bg-green-light text-black rounded-full text-2xl"
        to="/play"
      >
        Play again
      </Link>
    </div>
  );
};

export default PlacementDisplay;
