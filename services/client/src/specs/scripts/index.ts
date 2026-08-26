/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

// Static registry of rover scripts for the Free Roam mode dropdown.
//
// Bob's authoring workflow:
//   1. Write the script JSON to specs/scripts/<kebab-case-name>.json
//   2. Copy the same JSON to services/client/src/specs/scripts/<kebab-case-name>.json
//   3. Add an import and entry below so the script appears in the dropdown

import moveForward from "./move-forward.json";
import moveBackward from "./move-backward.json";
import turnLeft from "./turn-left.json";
import turnRight from "./turn-right.json";
import threePointTurn from "./three-point-turn.json";
import figure8 from "./figure-8.json";
import donut from "./donut.json";
import forwardThenCircle from "./forward-then-circle.json";

export interface ScriptEntry {
  name: string;
  description: string;
  steps: Array<{ command: string; durationMs: number }>;
}

export const scripts: ScriptEntry[] = [
  moveForward as ScriptEntry,
  moveBackward as ScriptEntry,
  turnLeft as ScriptEntry,
  turnRight as ScriptEntry,
  threePointTurn as ScriptEntry,
  figure8 as ScriptEntry,
  donut as ScriptEntry,
  forwardThenCircle as ScriptEntry,
];
