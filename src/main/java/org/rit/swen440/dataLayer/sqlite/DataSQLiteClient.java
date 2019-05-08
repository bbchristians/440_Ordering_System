package org.rit.swen440.dataLayer.sqlite;

import org.rit.swen440.dataLayer.Category;
import org.rit.swen440.dataLayer.DataLayerException;
import org.rit.swen440.dataLayer.Logger;
import org.rit.swen440.dataLayer.Product;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class DataSQLiteClient extends SQLiteClient {

    public DataSQLiteClient(String db) throws DataLayerException {
        super(db);
    }

    public Set<Category> getCategories() throws DataLayerException {
        LOGGER.log("INFO", "System retrieving categories from relational database...");
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
            LOGGER.log("ERROR", "System encountered an SQL exception while retrieving categories. Error readout: \r\n" + e.toString());
            throw new DataLayerException("An error was encountered when accessing the supplied database. Make sure the database is formatted correctly.");
        }
    }

    private Set<Product> getProducts(int categoryID) throws DataLayerException {
        LOGGER.log("INFO", "System retrieving products from relational database...");
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
            LOGGER.log("ERROR", "System encountered an SQL exception. Error readout: \r\n" + e.toString());
            throw new DataLayerException("An error was encountered when accessing the database. Make sure the database is formatted correctly.");
        }
    }
}
