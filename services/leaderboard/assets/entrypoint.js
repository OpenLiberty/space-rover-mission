/*******************************************************************************
 * Copyright (c) 2022 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
var db = connect("mongodb://root:openliberty@localhost:27017/admin");
db = db.getSiblingDB('spaceDB'); // we can not use "use" statement here to switch db
// tag::createUser[]
db.createUser({
	user: "spaceUser",
	pwd: "openliberty",
	roles: [{ role: "readWrite", db: "spaceDB" }]
});
// end::createUser[]

// tag::createCollection[]
db.createCollection("Leaderboard");
// end::createCollection[]

