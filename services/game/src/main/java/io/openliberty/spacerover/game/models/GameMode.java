/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.spacerover.game.models;

import jakarta.json.bind.annotation.JsonbProperty;

public class GameMode {
	@JsonbProperty("name")
	private String gameModeName;
	@JsonbProperty("id")
	private int gameModeID;
	@JsonbProperty("description")
	private String description;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public GameMode() {

	}

	public String getGameModeName() {
		return gameModeName;
	}

	public void setGameModeName(String gameModeName) {
		this.gameModeName = gameModeName;
	}

	public int getGameModeID() {
		return gameModeID;
	}

	public void setGameModeID(int gameModeID) {
		this.gameModeID = gameModeID;
	}

}
