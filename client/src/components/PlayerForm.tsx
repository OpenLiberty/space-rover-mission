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
import React, { useState } from "react";
import { Link } from "react-router-dom";
import { ReactComponent as Combomark } from "assets/openliberty_combomark.svg";

type Props = {
  isConnecting: boolean;
  isError: boolean;
  onSubmit: (playerName: string) => void;
};

const PlayerForm = ({ isConnecting, isError, onSubmit }: Props) => {
  const [playerName, setPlayerName] = useState("");
  
  return (
    <form
      className="max-w-screen-md flex flex-col gap-5 justify-center mx-auto h-full text-xl"
      onSubmit={(e) => {
        e.preventDefault();
        onSubmit(playerName);
      }}
    >
      <div className="flex flex-col items-center">
        <Combomark className="h-24 mr-16" />
        <p className="text-orange text-3xl">Space Rover Mission</p>
      </div>
      <div>
        <label className="block text-gray-300 text-2xl">Enter your name</label>
        <input
          className="w-full rounded-lg px-5 py-5"
          type="text"
          autoFocus
          autoComplete="false"
          autoCorrect="false"
          value={playerName}
          onChange={(e) => setPlayerName(e.target.value)}
        />
      </div>
      <div className="flex flex-row gap-2">
        <Link
          className="w-1/3 bg-gray-200 hover:bg-gray-300 text px-5 py-5 rounded-lg"
          to="/"
        >
          Go back
        </Link>
        <input
          className="w-2/3 hover:cursor-pointer bg-green hover:bg-green-light disabled:bg-green-dark disabled:cursor-not-allowed px-5 py-5 rounded-lg"
          type="submit"
          disabled={isConnecting || isError}
          value="Begin mission"
        />
      </div>
      <div className="text-center my-5">
        {isConnecting && <p className="text-yellow-500">● Connecting to game service.</p>}
        {isError && <p className="text-red-600">● Issue connecting to game service.</p>}
        {!isConnecting && !isError && <p className="text-green-600">● Connected to game service.</p>}
      </div>
    </form>
  );
};

export default PlayerForm;
