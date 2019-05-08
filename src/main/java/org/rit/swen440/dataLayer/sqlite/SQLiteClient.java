package org.rit.swen440.dataLayer.sqlite;

import org.rit.swen440.dataLayer.DataLayerException;
import org.rit.swen440.dataLayer.Logger;

import java.io.File;
import java.sql.*;

public class SQLiteClient {

    private final String DB_DIR = "data/";
    private final String SQLITE_PREPEND = "jdbc:sqlite:";
    final Logger LOGGER = Logger.OSLogger;

    Connection conn;

    SQLiteClient(String db) throws DataLayerException {
        String dbLoc = DB_DIR + db;
        try {
            if( dbExists(dbLoc) ) {
                this.conn = DriverManager.getConnection(SQLITE_PREPEND + dbLoc);
            } else {
                throw new DataLayerException("Could not find database at " + dbLoc +
                        ". Please make sure that a database with this name exists in the " +
                        DB_DIR + " directory.");
            }
        } catch (SQLException e) {
            throw new DataLayerException("An error occurred while loading the database. Try again and if the problem persists, contact your system administrator");
        }
    }

    public void close() {
        try {
            this.conn.close();
        } catch (SQLException e) { }
    }

    private boolean dbExists(String dbPath) {
        File dbTest = new File(dbPath);
        return dbTest.exists();
    }
}
