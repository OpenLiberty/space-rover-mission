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
export const gameSocketURL = process.env.REACT_APP_GAME_SOCKET_URL ?? "ws://localhost:3001";
export const gameDurationSeconds = process.env.REACT_APP_GAME_DURATION_SECONDS ? parseInt(process.env.REACT_APP_GAME_DURATION_SECONDS) : 300;
