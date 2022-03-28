import axios from "axios";
import { useQuery } from "react-query";
import { leaderboardURL } from "lib/config";

export type LeaderboardEntry = {
  rank: number;
  player: string;
  time: number;
  score: number;
};

const getLeaderboard = async () => {
  const { data } = await axios.get<LeaderboardEntry[]>(`${leaderboardURL}/`);
  data.forEach((entry, i) => {
    entry.rank = i + 1;
  });
  return data;
};

const useLeaderboard = () => {
  return useQuery("leaderboard", getLeaderboard);
};

export default useLeaderboard;
