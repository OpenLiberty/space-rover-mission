/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
export const gameURL = process.env.REACT_APP_GAME_URL ?? "http://localhost:3001";
export const gameSocketURL = process.env.REACT_APP_GAME_SOCKET_URL ?? "ws://localhost:3001";
export const gameDurationSeconds = process.env.REACT_APP_GAME_DURATION_SECONDS ? parseInt(process.env.REACT_APP_GAME_DURATION_SECONDS) : 300;

export const leaderboardURL = process.env.REACT_APP_LEADERBOARD_URL ?? "http://localhost:3002";
