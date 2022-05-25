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
db.createCollection("Settings");
// end::createCollection[]

