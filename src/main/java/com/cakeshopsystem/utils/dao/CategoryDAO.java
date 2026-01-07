package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Category;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CategoryDAO {

    // ==========================
    // -------- READ ------------
    // ==========================
    public static ObservableList<Category> getAllCategories() {
        ObservableList<Category> categories = FXCollections.observableArrayList();
        String sql = "SELECT * FROM categories ORDER BY category_id";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                categories.add(mapCategory(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching categories: " + err.getLocalizedMessage());
        }

        return categories;
    }

    public static Category getCategoryById(int categoryId) {
        String sql = "SELECT * FROM categories WHERE category_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapCategory(rs);
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching category by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static Category getCategoryByName(String categoryName) {
        String sql = "SELECT * FROM categories WHERE category_name = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, categoryName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapCategory(rs);
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching category by name: " + err.getLocalizedMessage());
        }

        return null;
    }

    // ==========================
    // -------- CREATE ----------
    // ==========================
    public static boolean insertCategory(Category category) {
        String sql = "INSERT INTO categories (category_name) VALUES (?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, category.getCategoryName());

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("Inserting category failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    category.setCategoryId(keys.getInt(1));
                } else {
                    throw new SQLException("Inserting category failed, no ID obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting category: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ==========================
    // -------- UPDATE ----------
    // ==========================
    public static boolean updateCategory(Category category) {
        String sql = "UPDATE categories SET category_name = ? WHERE category_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, category.getCategoryName());
            stmt.setInt(2, category.getCategoryId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating category: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ==========================
    // -------- DELETE ----------
    // ==========================
    public static boolean deleteCategory(int categoryId) {
        String sql = "DELETE FROM categories WHERE category_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, categoryId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            // This will often fail if products reference the category (FK RESTRICT) — that's normal.
            System.err.println("Error deleting category: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ==========================
    // ------ HELPER MAP --------
    // ==========================
    private static Category mapCategory(ResultSet rs) throws SQLException {
        int id = rs.getInt("category_id");
        String name = rs.getString("category_name");
        return new Category(id, name);
    }
}
