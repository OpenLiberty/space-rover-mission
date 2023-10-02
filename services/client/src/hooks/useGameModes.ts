/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
import axios from "axios";
import { useQuery } from "react-query";
import { gameURL } from "lib/config";

export type GameMode = {
  id: number;
  name: string;
  description: string;
};

const getGameModes = async () => {
  const { data } = await axios.get<GameMode[]>(`${gameURL}/modes`);
  return data;
};

const useGameModes = () => {
  const defaultGameModes: GameMode[] = [
    {
      id: 1,
      name: "Classic",
      description: "Visit all the planets in any order.",
    },
  ];

  const { data: gameModes = defaultGameModes } = useQuery(
    "game-modes",
    getGameModes
  );

  return gameModes;
};

export default useGameModes;
