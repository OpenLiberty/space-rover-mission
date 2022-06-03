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
