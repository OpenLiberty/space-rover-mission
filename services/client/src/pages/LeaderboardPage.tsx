/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import React, { useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import useSound from "use-sound";
import highscoreSoundFile from "assets/sounds/highscore.wav";
import successSoundFile from "assets/sounds/success.mp3";
import failureSoundFile from "assets/sounds/failure.wav";
import useLeaderboard from "hooks/useLeaderboard";
import PlacementDisplay from "components/PlacementDisplay";
import LeaderboardTable from "components/LeaderboardTable";
import { playerFinished } from "lib/utils";

const LeaderboardPage = () => {
  const [searchParams] = useSearchParams();
  const player = searchParams.get("player");
  const gameMode = searchParams.get("gameMode") ?? "1";

  const { leaderboard, placement } = useLeaderboard(player, gameMode);

  const [playHighscore] = useSound(highscoreSoundFile, {
    volume: 0.25,
  });
  const [playSuccess] = useSound(successSoundFile);
  const [playFailure] = useSound(failureSoundFile);

  useEffect(() => {
    if (!placement || !playHighscore || !playSuccess || !playFailure) {
      return;
    }

    const { health, time, rank } = placement;

    if (playerFinished(health, time, gameMode)) {
      if (rank <= 5) {
        playHighscore();
      } else {
        playSuccess();
      }
    } else {
      playFailure();
    }
  }, [placement, playHighscore, playSuccess, playFailure]);

  return (
    <div className="container mx-auto">
      {placement && (
        <PlacementDisplay gameMode={gameMode} {...placement} />
      )}
      {leaderboard && (
        <LeaderboardTable gameMode={gameMode} data={leaderboard} />
      )}
    </div>
  );
};

export default LeaderboardPage;
