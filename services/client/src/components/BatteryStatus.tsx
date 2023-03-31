/*******************************************************************************
 * Copyright (c) 2023 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
import React from "react";

type Props = {
  batteryPercentage: number;
};

const BatteryStatus = ({ batteryPercentage }: Props) => {
  let color;
  if (batteryPercentage < 20) {
    color = "bg-red-600";
  } else if (batteryPercentage < 50) {
    color = "bg-yellow-500";
  } else {
    color = "bg-green-600";
  }

  return (
    <div className="flex justify-center">
      <div className="bg-gray-200 w-64 rounded-md overflow-hidden">
        <div className="relative h-12 flex justify-center items-center">
          <div
            className={`absolute h-12 bottom-0 left-0 ${color}`}
            style={{
              width: `${batteryPercentage >= 0 ? batteryPercentage : 0}%`,
            }}
          ></div>
          <p className="relative text-md">
            Rover battery: {batteryPercentage >= 0 ? batteryPercentage : "--"}%
          </p>
        </div>
      </div>
    </div>
  );
};

export default BatteryStatus;
