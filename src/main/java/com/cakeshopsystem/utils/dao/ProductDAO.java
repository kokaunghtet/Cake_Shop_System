package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ProductDAO {

    // ==========================
    // -------- READ ------------
    // ==========================
    public static ObservableList<Product> getAllProducts() {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String sql = "SELECT * FROM products ORDER BY product_id";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapProduct(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching products: " + err.getLocalizedMessage());
        }

        return products;
    }

    public static Product getProductById(int productId) {
        String sql = "SELECT * FROM products WHERE product_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching product by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static ObservableList<Product> getProductsByCategoryId(int categoryId) {
        ObservableList<Product> products = FXCollections.observableArrayList();
        String sql = "SELECT * FROM products WHERE category_id = ? ORDER BY product_id";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    products.add(mapProduct(rs));
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching products by category id: " + err.getLocalizedMessage());
        }

        return products;
    }

    public static Product getProductByName(String productName) {
        String sql = "SELECT * FROM products WHERE product_name = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, productName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapProduct(rs);
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching product by name: " + err.getLocalizedMessage());
        }

        return null;
    }

    // ==========================
    // -------- WRITE -----------
    // ==========================
    public static boolean insertProduct(Product product) {
        String sql = """
                INSERT INTO products
                    (product_name, category_id, base_price, is_active, track_inventory, shelf_life_days, img_path)
                VALUES
                    (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, product.getProductName());
            stmt.setInt(2, product.getCategoryId());
            stmt.setDouble(3, product.getBasePrice());
            stmt.setBoolean(4, product.isActive());
            stmt.setBoolean(5, product.isTrackInventory());

            // nullable Integer
            if (product.getShelfLifeDays() == null) stmt.setNull(6, Types.INTEGER);
            else stmt.setInt(6, product.getShelfLifeDays());

            // nullable String
            if (product.getImgPath() == null) stmt.setNull(7, Types.VARCHAR);
            else stmt.setString(7, product.getImgPath());

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("Inserting product failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    product.setProductId(keys.getInt(1));
                } else {
                    throw new SQLException("Inserting product failed, no ID obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting product: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean updateProduct(Product product) {
        String sql = """
                UPDATE products
                SET product_name = ?,
                    category_id = ?,
                    base_price = ?,
                    is_active = ?,
                    track_inventory = ?,
                    shelf_life_days = ?,
                    img_path = ?
                WHERE product_id = ?
                """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, product.getProductName());
            stmt.setInt(2, product.getCategoryId());
            stmt.setDouble(3, product.getBasePrice());
            stmt.setBoolean(4, product.isActive());
            stmt.setBoolean(5, product.isTrackInventory());

            if (product.getShelfLifeDays() == null) stmt.setNull(6, Types.INTEGER);
            else stmt.setInt(6, product.getShelfLifeDays());

            if (product.getImgPath() == null) stmt.setNull(7, Types.VARCHAR);
            else stmt.setString(7, product.getImgPath());

            stmt.setInt(8, product.getProductId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating product: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, productId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            // This may fail if other tables reference this product (FK RESTRICT) — that's normal.
            System.err.println("Error deleting product: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ==========================
    // ------ ROW MAPPER --------
    // ==========================
    private static Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product();

        p.setProductId(rs.getInt("product_id"));
        p.setProductName(rs.getString("product_name"));
        p.setCategoryId(rs.getInt("category_id"));
        p.setBasePrice(rs.getDouble("base_price"));

        p.setActive(rs.getBoolean("is_active"));
        p.setTrackInventory(rs.getBoolean("track_inventory"));

        int shelf = rs.getInt("shelf_life_days");
        if (rs.wasNull()) p.setShelfLifeDays(null);
        else p.setShelfLifeDays(shelf);

        p.setImgPath(rs.getString("img_path")); // returns null if SQL NULL

        return p;
    }
}
