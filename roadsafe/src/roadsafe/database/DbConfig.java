package roadsafe.database;

// just the db settings in one place
// sqlite file, no server needed
class DbConfig {

    static final String DB_FILE = "roadsafe.db";
    static final String DB_URL = "jdbc:sqlite:" + DB_FILE;

    private DbConfig() {
        // not needed
    }
}
