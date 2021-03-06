package org.rit.swen440.control;

import org.rit.swen440.dataLayer.*;
import org.rit.swen440.dataLayer.sqlite.DataSQLiteClient;
import org.rit.swen440.dataLayer.sqlite.LogSQLiteClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controls access to data, on start-up scans directories and builds internal
 * representation of categories and items within each category.  Isolates the
 * categories and products from information on the underlying file system.
 */
public class Controller {
  private Set<Category> categories = new HashSet<>();

  public  enum PRODUCT_FIELD {
    NAME,
    DESCRIPTION,
    COST,
    INVENTORY
  }

  public Controller(String fileName) throws DataLayerException {
    loadCategories(fileName);
  }

  /**
   * Load the Category information
   *
   * @param database the database to collect the catagory info from
   */
  private void loadCategories(String database) throws DataLayerException {
    DataSQLiteClient conn = new DataSQLiteClient(database);
    this.categories = conn.getCategories();
    conn.close();
  }

  /**
   * Get a list of all category names
   *
   * @return list of categories
   */
  public List<String> getCategories() {

    return categories.stream()
        .map(Category::getName)
        .collect(Collectors.toList());
  }

  /**
   * Get the description of the named category
   * @param category name
   * @return description
   */
  public String getCategoryDescription(String category) {
    Optional<Category> match = categories.stream().filter(c -> c.getName().equalsIgnoreCase(category)).findFirst();
    return match.map(Category::getDescription).orElse(null);
  }

  /**
   * Return a list of Products based on the provided category.
   *
   * @param categoryName Name of Category to use
   * @return List of Products in the category
   */
  public List<String> getProducts(String categoryName) {
    Optional<Category> category = findCategory(categoryName);

   return category.map(c -> c.getProducts().stream()
              .map(Product::getTitle)
              .collect(Collectors.toList()))
           .orElse(null);
  }


  public String getProductInformation(String category, String product, PRODUCT_FIELD field) {
   Optional<Product> selectedProduct = getProduct(category, product);
   switch (field) {
     case NAME:
       return selectedProduct.map(Product::getTitle).orElse(null);

     case DESCRIPTION:
       return selectedProduct.map(Product::getDescription).orElse(null);

     case COST:
       return selectedProduct.map(p -> String.format("%.2f", p.getCost())).orElse(null);

     case INVENTORY:
       return selectedProduct.map(p -> String.valueOf(p.getItemCount())).orElse(null);
   }

   return null;
  }

  /**
   * Get the category that matches the provided category name
   *
   * @param name
   * @return Category, if present
   */
  public Optional<Category> findCategory(String name) {
    return categories.stream()
        .filter(c -> c.getName().equalsIgnoreCase(name))
        .findFirst();
  }

  /**
   * Loop through all our categories and write any product records that
   * have been updated.
   */
  public void writeCategories() {
    for (Category category: categories) {
      writeProducts(category.getProducts());
    }
  }

  /* -----------------------------------
   *
   * Private Methods
   */

  /**
   * Get the category object for this directory
   *
   * @param path directory
   * @return Category object, if .cat file exists
   */
  private Optional<Category> getCategory(Path path) {
    DirectoryStream.Filter<Path> catFilter = new DirectoryStream.Filter<Path>() {
      @Override
      public boolean accept(Path path) throws IOException {
        return path.toString().toLowerCase().endsWith("cat");
      }
    };

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, catFilter)) {
      for (Path file : stream) {
        // read the file
        BufferedReader reader = Files.newBufferedReader(file, Charset.forName("US-ASCII"));
        Category category = new Category();

        category.setName(reader.readLine());
        category.setDescription(reader.readLine());
        category.setProducts(loadProducts(path));

        return Optional.of(category);
      }
    } catch (IOException | DirectoryIteratorException e) {
      System.err.println(e);
    }

    return Optional.empty();
  }

  private Optional<Product> getProduct(String category, String product) {
    return findCategory(category).map(c -> c.findProduct(product)).orElse(null);
  }

  public boolean tryOrder(String category, String product, int count) {
    Optional<Product> pOpt = getProduct(category, product);
    if (pOpt.isPresent()) {
      Product p = getProduct(category, product).get();
      if (p != null) {
        return p.order(count);
      }
    } else {
      Logger.OSLogger.log("ERROR", "Tried to order " + Integer.toString(count) + " of " + product + " and failed.");
    }
    return false;
  }

  /**
   * Parse a subdirectory and create a product object for each product within it
   *
   * @param path the subdirectory we're working in
   * @return a set of products
   */
  private Set<Product> loadProducts(Path path) {
    DirectoryStream.Filter<Path> productFilter = new DirectoryStream.Filter<Path>() {
      @Override
      public boolean accept(Path path) throws IOException {
        return !Files.isDirectory(path) && !path.toString().toLowerCase().endsWith("cat");
      }
    };

    Set<Product> products = new HashSet<>();

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path, productFilter)) {
      for (Path productFile : stream) {
        // Read the product file
        try (BufferedReader reader = Files.newBufferedReader(productFile, Charset.forName("US-ASCII"))){
          Product product = new Product();
          product.setSkuCode(Integer.valueOf(reader.readLine()));
          product.setItemCount(Integer.valueOf(reader.readLine()));
          product.setThreshold(Integer.valueOf(reader.readLine()));
          product.setReorderAmount(Integer.valueOf(reader.readLine()));
          product.setTitle(reader.readLine());
          product.setDescription(reader.readLine());
          product.setCost(new BigDecimal(reader.readLine()));

          product.setPath(productFile);

          products.add(product);
        } catch (Exception e) {
          // Failed to read a product.  Log the error and continue
          System.err.println("Failed to read file: " + path.toString());
        }
      }
    } catch (IOException | DirectoryIteratorException e) {
      System.err.println(e);
    }

    return products;
  }

  /**
   * Loop through the set of products and write out any updated products
   *
   * @param products set of products
   */
  private void writeProducts(Set<Product> products) {
    for (Product product : products) {
      if (product.isUpdated()) {
        updateProduct(product);
      }
    }
  }

  /**
   * Write an updated product
   *
   * @param product the product
   */
  private void updateProduct(Product product) {
    try (BufferedWriter writer = Files.newBufferedWriter(product.getPath(), Charset.forName("US-ASCII"))){
      writer.write(String.valueOf(product.getSkuCode()));
      writer.newLine();
      writer.write(String.valueOf(product.getItemCount()));
      writer.newLine();
      writer.write(String.valueOf(product.getThreshold()));
      writer.newLine();
      writer.write(String.valueOf(product.getReorderAmount()));
      writer.newLine();
      writer.write(product.getTitle());
      writer.newLine();
      writer.write(product.getDescription());
      writer.newLine();
      writer.write(product.getCost().toString());
    } catch(IOException e) {
      System.err.println("Failed to write product file for:" + product.getTitle());
    }
  }

  public List<String> getLogs(){
    List<LogItem> logItems = null;
    try {
      LogSQLiteClient client = new LogSQLiteClient("logs.db");
      logItems = client.getLogs(0, Long.MAX_VALUE);
    } catch (Exception e) {}

    List<String> logItemStrings = new ArrayList<>();
    for(LogItem item : logItems){
      logItemStrings.add(item.severity + " | " + item.date.toString() + ":\n" + "  " + item.message);
    }
    return logItemStrings;
  }
}
