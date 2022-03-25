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
import { useState, useEffect } from "react";

const formatTime = (timeRemaining: number) => {
  const seconds = timeRemaining % 60;
  const minutes = Math.floor(timeRemaining / 60);

  const formattedMinutes = minutes < 10 ? `0${minutes}` : minutes;
  const formattedSeconds = seconds < 10 ? `0${seconds}` : seconds;

  return `${formattedMinutes}:${formattedSeconds}`;
};

const useTimer = (totalTimeInSeconds: number) => {
  const [timeRemaining, setTimeRemaining] = useState(totalTimeInSeconds);
  const [isCounting, setIsCounting] = useState(false);

  useEffect(() => {
    if (timeRemaining > 0 && isCounting) {
      setTimeout(() => {
        setTimeRemaining(timeRemaining - 1);
      }, 1000);
    }
  }, [timeRemaining, isCounting]);

  const formattedTime = formatTime(timeRemaining);
  
  const startTimer = () => setIsCounting(true);
  const stopTimer = () => setIsCounting(false);

  return {
    timeRemaining,
    formattedTime,
    startTimer,
    stopTimer,
  };
};

export default useTimer;
