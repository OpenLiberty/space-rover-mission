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

