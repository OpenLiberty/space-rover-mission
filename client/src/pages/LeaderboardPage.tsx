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
import { useSearchParams } from "react-router-dom";
import useLeaderboard from "hooks/useLeaderboard";
import PlacementDisplay from "components/PlacementDisplay";
import LeaderboardTable from "components/LeaderboardTable";

const LeaderboardPage = () => {
  const [searchParams] = useSearchParams();
  const player = searchParams.get("player");

  const { data: leaderboard } = useLeaderboard();

  let placement;
  if (player && leaderboard) {
    placement = leaderboard
      .filter((entry) => entry.player === player)
      .sort((a, b) => a.timestamp - b.timestamp)[0];
  }

  return (
    <div className="container mx-auto">
      {placement && <PlacementDisplay {...placement} />}
      {leaderboard && <LeaderboardTable data={leaderboard.slice(0, 5)} />}
    </div>
  );
};

export default LeaderboardPage;
