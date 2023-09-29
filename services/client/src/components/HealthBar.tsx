/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import React from "react";

type Props = {
  health: number;
};

const HealthBar = ({ health }: Props) => {
  return (
    <div>
      <h3 className="text-gray-300 text-2xl">Health</h3>
      <div className="bg-gray-200">
        <div className="py-5 bg-green" style={{ width: `${health}%` }}>
          <p className="px-5 text-xl">{health}%</p>
        </div>
      </div>
    </div>
  );
};

export default HealthBar;
