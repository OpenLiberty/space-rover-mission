/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import React, { useState, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import useGameModes from "hooks/useGameModes";
import { LeaderboardEntry } from "hooks/useLeaderboard";
import { formatTime, playerFinished } from "lib/utils";
import usePagination from "hooks/usePagination";

type Props = {
  gameMode: string;
  data: LeaderboardEntry[];
};

const LeaderboardTable = ({ gameMode, data }: Props) => {
  const [nameFilter, setNameFilter] = useState("");
  const [showFails, setShowFails] = useState(false);

  const filteredData = useMemo(() => {
    return data
      .filter((entry) =>
        showFails || playerFinished(entry.health, entry.time, gameMode)
      )
      .filter((entry) =>
        entry.player.toLowerCase().includes(nameFilter.toLowerCase())
      );
  }, [nameFilter, showFails]);

  const { page, totalPages, changePage, paginatedData } =
    usePagination(filteredData);

  const navigate = useNavigate();
  const gameModes = useGameModes();

  return (
    <>
      <div className="flex flex-row justify-between items-end my-2">
        <div>
          <h1 className="text-gray-50 text-5xl">Leaderboard</h1>
          <h2 className="text-orange text-xl mt-2">
            Open Liberty - Space Rover Mission
          </h2>
        </div>
        <div className="flex flex-row gap-2">
          <div>
            <label className="block text-gray-50 text-sm">Game mode</label>
            <div className="relative">
              <select
                className="w-48 rounded-lg px-5 py-3 appearance-none"
                value={gameMode}
                onChange={(e) =>
                  navigate(`/leaderboard?gameMode=${e.target.value}`)
                }
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
          </div>
          <div>
            <label className="block text-gray-50 text-sm">Player filter</label>
            <input
              className="px-5 py-3 rounded-md"
              type="text"
              autoComplete="false"
              autoCorrect="false"
              spellCheck="false"
              placeholder="Filter by player name"
              onChange={(e) => setNameFilter(e.target.value)}
            />
          </div>
        </div>
      </div>
      <table className="w-full rounded-t-md overflow-hidden text-center text-xl">
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
          {paginatedData.map((entry) => (
            <tr
              className={
                playerFinished(entry.health, entry.time, gameMode)
                  ? "odd:bg-gray-50 even:bg-gray-200"
                  : "odd:bg-red-200 even:bg-red-300"
              }
            >
              <td className="p-3">{entry.rank}</td>
              <td className="p-3">{entry.player}</td>
              <td className="p-3">{formatTime(entry.time)}</td>
              <td className="p-3">{entry.health}%</td>
              <td className="p-3">{entry.score}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {filteredData.length === 0 && (
        <div className="text-center text-xl bg-gray-50 py-3">
          No players to show
        </div>
      )}
      <div className="flex flex-row justify-between items-center px-1 bg-blue-dark text-white rounded-b-md">
        <div className="flex flex-row items-center">
          <label className="mx-5">
            <input
              className="accent-green h-3 w-3 mr-2"
              type="checkbox"
              checked={showFails}
              onChange={(e) => setShowFails(e.target.checked)}
            />
            Show failed missions
          </label>
        </div>
        <div className="flex flex-row items-center">
          <button
            className="px-5 py-1 text-xl disabled:text-gray-500"
            onClick={() => changePage(page - 1)}
            disabled={page === 1}
          >
            &#x25C0;
          </button>
          <div>
            Page {page} of {totalPages}
          </div>
          <button
            className="px-5 py-3 text-xl disabled:text-gray-500"
            onClick={() => changePage(page + 1)}
            disabled={page === totalPages}
          >
            &#x25B6;
          </button>
        </div>
      </div>
    </>
  );
};

export default LeaderboardTable;
