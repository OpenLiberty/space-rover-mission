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
    placement = leaderboard.filter((entry) => entry.player === player)[0];
  }

  return (
    <div className="container mx-auto">
      {placement && <PlacementDisplay {...placement} />}
      {leaderboard && <LeaderboardTable data={leaderboard.slice(0, 5)} />}
    </div>
  );
};

export default LeaderboardPage;
