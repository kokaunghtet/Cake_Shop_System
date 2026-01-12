package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Size;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class SizeDAO {

    // =====================================
    // ========== READ OPERATIONS ===========
    // =====================================

    public static ObservableList<Size> getAllSizes() {
        ObservableList<Size> sizes = FXCollections.observableArrayList();
        String query = "SELECT * FROM sizes ORDER BY size_inches ASC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                sizes.add(mapSize(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching sizes: " + err.getLocalizedMessage());
        }

        return sizes;
    }

    public static Size getSizeById(int sizeId) {
        String query = "SELECT * FROM sizes WHERE size_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, sizeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapSize(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching size by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static Size getSizeByInches(int sizeInches) {
        String query = "SELECT * FROM sizes WHERE size_inches = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, sizeInches);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapSize(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching size by inches: " + err.getLocalizedMessage());
        }

        return null;
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    public static boolean insertSize(Size size) {
        String sql = "INSERT INTO sizes (size_inches, price) VALUES (?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, size.getSizeInches());
            stmt.setDouble(2, size.getPrice());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting size failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    size.setSizeId(keys.getInt(1));
                } else {
                    throw new SQLException("Inserting size failed, no ID obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting size: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean updateSize(Size size) {
        String sql = "UPDATE sizes SET size_inches = ?, price = ? WHERE size_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, size.getSizeInches());
            stmt.setDouble(2, size.getPrice());
            stmt.setInt(3, size.getSizeId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating size: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean deleteSize(int sizeId) {
        String sql = "DELETE FROM sizes WHERE size_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, sizeId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting size: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================

    private static Size mapSize(ResultSet rs) throws SQLException {
        int sizeId = rs.getInt("size_id");
        int sizeInches = rs.getInt("size_inches");
        double price = rs.getDouble("price");
        return new Size(sizeId, sizeInches, price);
    }
}