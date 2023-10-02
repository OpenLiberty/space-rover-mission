/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import { useState, useEffect } from "react";
import { formatTime } from "lib/utils";

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
