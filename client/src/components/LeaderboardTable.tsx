import React from "react";
import { LeaderboardEntry } from "hooks/useLeaderboard";
import { formatTime } from "lib/utils";

type Props = {
  data: LeaderboardEntry[];
}

const LeaderboardTable = ({ data }: Props) => {
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
            <th className="p-3">SCORE</th>
          </tr>
        </thead>
        <tbody>
          {data.map((entry) => (
            <tr className="odd:bg-gray-50 even:bg-gray-200">
              <td className="p-3">{entry.rank}</td>
              <td className="p-3">{entry.player}</td>
              <td className="p-3">{formatTime(entry.time)}</td>
              <td className="p-3">{entry.score}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </>
  );
};

export default LeaderboardTable;
