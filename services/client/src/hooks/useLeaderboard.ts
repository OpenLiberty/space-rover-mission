/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import { useMemo } from "react";
import axios from "axios";
import { useQuery } from "react-query";
import { leaderboardURL } from "lib/config";

export type LeaderboardEntry = {
  rank: number;
  player: string;
  time: number;
  health: number;
  score: number;
  timestamp: string;
};

const getLeaderboard = async (gameMode: string) => {
  const { data } = await axios.get<LeaderboardEntry[]>(`${leaderboardURL}/${gameMode}`);
  data.forEach((entry, i) => {
    entry.rank = i + 1;
  });
  return data;
};

const useLeaderboard = (player: string | null, gameMode: string) => {
  const { data: leaderboard, ...rest } = useQuery(
    ["leaderboard", gameMode],
    () => getLeaderboard(gameMode)
  );

  const placement = useMemo(() => {
    if (player) {
      return leaderboard
        ?.filter((entry) => entry.player === player)
        .sort((a, b) => Number(b.timestamp) - Number(a.timestamp))[0];
    }
  }, [leaderboard, player]);

  return {
    leaderboard,
    placement,
    ...rest,
  };
};

export default useLeaderboard;
