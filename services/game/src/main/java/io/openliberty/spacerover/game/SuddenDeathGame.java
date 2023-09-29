/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package io.openliberty.spacerover.game;

import io.openliberty.spacerover.game.models.GameEvent;
import io.openliberty.spacerover.game.models.Constants;

public class SuddenDeathGame extends Game {

	@Override
	public void decrementHP(int amount) {
		this.setHealth(0);
		getEventManager().notify(GameEvent.HP, this.getHealth());
	}
	@Override
	public void decrementHPSun(int amount) {
		this.setHealth(0);
		getEventManager().notify(GameEvent.HP_SUN, this.getHealth());
	}


	@Override
	public String getGameMode() {
		return Constants.INIT_GAME_SUDDEN_DEATH;
	}
}
