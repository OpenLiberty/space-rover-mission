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

import org.junit.jupiter.api.Test;

import io.openliberty.spacerover.game.models.GameEvent;

public class SpaceHopTest implements GameEventListener {

	SpaceHop game;
	@Test
	public void testGameTimer()
	{
		game = new SpaceHop();
		game.getEventManager().subscribe(GameEvent.FIVE_SECONDS_LEFT, this);
		game.getEventManager().subscribe(GameEvent.PLANET_CHANGED, this);
		game.startGameSession("test");

		while(game.isInProgress())
		{
		}
		game.endGameSession();
		
	}

	@Override
	public void update(GameEvent eventType, long value) {
		System.out.println(eventType);
		System.out.println(game.getCurrentPlanetColour());
		
	}
}
