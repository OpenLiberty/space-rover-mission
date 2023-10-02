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
import { Link } from "react-router-dom";
import { ReactComponent as Combomark } from "assets/openliberty_combomark.svg";

const HomePage = () => {
  return (
    <div className="flex flex-col items-center justify-center mx-auto h-full">
      <div className="flex flex-col items-center">
        <Combomark className="h-24 mr-16" />
        <p className="text-orange text-3xl">Space Rover Mission</p>
      </div>
      <Link
        className="block text-5xl font-medium px-32 py-8 my-14 rounded-lg bg-green hover:bg-green-light"
        to="/play"
      >
        Play
      </Link>
    </div>
  );
};

export default HomePage;
