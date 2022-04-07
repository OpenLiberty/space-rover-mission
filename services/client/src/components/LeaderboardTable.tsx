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
import { LeaderboardEntry } from "hooks/useLeaderboard";
import { formatTime } from "lib/utils";
import { gameDurationSeconds } from "lib/config";

type Props = {
  data: LeaderboardEntry[];
};

const LeaderboardTable = ({ data }: Props) => {
  const filteredData = data
    .filter((entry) => entry.health > 0 && entry.time < gameDurationSeconds)
    .slice(0, 5);

  return (
    <>
      <div>
        <h1 className="text-gray-50 text-5xl">Leaderboard</h1>
        <h2 className="text-orange text-xl my-2">
          Open Liberty - Space Rover Mission
        </h2>
      </div>
      <table className="w-full rounded-md overflow-hidden text-center text-xl">
        <thead className="bg-green">
          <tr>
            <th className="p-3">RANK</th>
            <th className="p-3">PLAYER</th>
            <th className="p-3">TIME</th>
            <th className="p-3">HEALTH</th>
            <th className="p-3">SCORE</th>
          </tr>
        </thead>
        <tbody>
          {filteredData.map((entry) => (
            <tr className="odd:bg-gray-50 even:bg-gray-200">
              <td className="p-3">{entry.rank}</td>
              <td className="p-3">{entry.player}</td>
              <td className="p-3">{formatTime(entry.time)}</td>
              <td className="p-3">{entry.health}%</td>
              <td className="p-3">{entry.score}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
};

export default LeaderboardTable;
