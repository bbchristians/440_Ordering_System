package org.rit.swen440.dataLayer;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class SQLiteClient {

    private final String DB_DIR = "data/";
    private final String SQLITE_PREPEND = "jdbc:sqlite:";

    private Connection conn;

    public SQLiteClient(String db) throws DataLayerException {
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

    public Set<Category> getCategories() throws DataLayerException {
        try {
            PreparedStatement stmt = this.conn.prepareStatement("SELECT * FROM CATEGORY");
            ResultSet rs = stmt.executeQuery();
            Set results = new HashSet<Category>();
            // Fetch each row from the result set
            while (rs.next()) {
                String name = rs.getString("name");
                String description = rs.getString("description");
                int categoryId = rs.getInt("CID");

                //Assuming you have a user object
                Category cat = new Category();
                cat.setName(name);
                cat.setDescription(description);
                cat.setProducts(getProducts(categoryId));

                results.add(cat);
            }
            return results;
        } catch (SQLException e) {
            throw new DataLayerException("An error was encountered when accessing the supplied database. Make sure the database is formatted correctly.");
        }
    }

    private Set<Product> getProducts(int categoryID) throws DataLayerException {
        try {
            PreparedStatement stmt = this.conn.prepareStatement("SELECT * FROM ITEM WHERE CID=?");
            stmt.setInt(1, categoryID);
            ResultSet rs = stmt.executeQuery();
            Set results = new HashSet<Product>();
            // Fetch each row from the result set
            while (rs.next()) {
                String name = rs.getString("name");
                String description = rs.getString("description");
                int itemCount = rs.getInt("stock");
                float cost = rs.getFloat("cost_usd");

                //Assuming you have a user object
                Product product = new Product();
                product.setTitle(name);
                product.setDescription(description);
                product.setItemCount(itemCount);
                product.setCost(new BigDecimal(cost));

                results.add(product);
            }
            return results;
        } catch (SQLException e) {
            throw new DataLayerException("An error was encountered when accessing the database. Make sure the database is formatted correctly.");
        }
    }

    private boolean dbExists(String dbPath) {
        File dbTest = new File(dbPath);
        return dbTest.exists();
    }
}
