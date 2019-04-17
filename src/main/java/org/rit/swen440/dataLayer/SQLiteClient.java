package org.rit.swen440.dataLayer;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class SQLiteClient {

    private final String DB_DIR = "data/";
    private final String SQLITE_PREPEND = "jdbc:sqlite:";

    private Connection conn;

    public SQLiteClient(String db) {
        try {
            this.conn = DriverManager.getConnection(SQLITE_PREPEND + DB_DIR + db);
            if (this.conn != null) {
                DatabaseMetaData meta = this.conn.getMetaData();
                System.out.println("The driver name is " + meta.getDriverName());
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            // TODO handle?
        }
    }

    public void close() {
        try {
            this.conn.close();
        } catch (SQLException e) {
            // TODO handle?
        }
    }

    public Set<Category> getCategories() {
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
            // TODO handle?
            System.out.println(e.getMessage());
        }
        return new HashSet<Category>();
    }

    private Set<Product> getProducts(int categoryID) {
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
            // TODO handle?
            System.out.println(e.getMessage());
        }
        return new HashSet<Product>();
    }

    public static void main(String[] args) {
        SQLiteClient conn = new SQLiteClient("test.db");
        System.out.println(conn.getCategories());
        conn.close();
    }
}
