/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import { gameDurationSeconds } from "./config"; 

export const formatTime = (time: number) => {
  const seconds = time % 60;
  const minutes = Math.floor(time / 60);

  const formattedMinutes = minutes < 10 ? `0${minutes}` : minutes;
  const formattedSeconds = seconds < 10 ? `0${seconds}` : seconds;

  return `${formattedMinutes}:${formattedSeconds}`;
};

export const playerFinished = (health: number, time: number, gameMode: string) => {
  if (gameMode === "2") { // planet hop
    return health > 0 && time === gameDurationSeconds;
  }
  return health > 0 && time < gameDurationSeconds;
}
