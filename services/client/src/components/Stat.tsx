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
import React from "react";

type Props = {
  title: string;
  value: string | number;
};

const Stat = ({ title, value }: Props) => {
  return (
    <div className="text-center w-full">
      <h3 className="text-gray-300 text-2xl">{title}</h3>
      <p className="text-gray-50 text-8xl font-semibold">{value}</p>
    </div>
  );
};

export default Stat;
