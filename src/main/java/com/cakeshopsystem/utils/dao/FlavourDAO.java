package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Flavour;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class FlavourDAO {

    // =====================================
    // ========== READ OPERATIONS ===========
    // =====================================

    public static ObservableList<Flavour> getAllFlavours() {
        ObservableList<Flavour> flavours = FXCollections.observableArrayList();
        String query = "SELECT * FROM flavours";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                flavours.add(mapFlavour(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching flavours: " + err.getLocalizedMessage());
        }

        return flavours;
    }

    public static Flavour getFlavourById(int flavourId) {
        String query = "SELECT * FROM flavours WHERE flavour_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, flavourId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapFlavour(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching flavour by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static Flavour getFlavourByName(String flavourName) {
        String query = "SELECT * FROM flavours WHERE flavour_name = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, flavourName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapFlavour(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching flavour by name: " + err.getLocalizedMessage());
        }

        return null;
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    // ----------------- Insert Flavour -----------------
    public static boolean insertFlavour(Flavour flavour) {
        String sql = "INSERT INTO flavours (flavour_name, price) VALUES (?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, flavour.getFlavourName());
            stmt.setDouble(2, flavour.getPrice());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting flavours failed, no rows affected.");

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int flavourId = generatedKeys.getInt(1);
                    flavour.setFlavourId(flavourId);
                } else {
                    throw new SQLException("Inserting flavours failed, no Id obtained");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting flavour: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ----------------- Update Flavour -----------------
    public static boolean updateFlavour(Flavour flavour) {
        String sql = "UPDATE flavours SET flavour_name = ?, price = ? WHERE flavour_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, flavour.getFlavourName());
            stmt.setDouble(2, flavour.getPrice());
            stmt.setInt(3, flavour.getFlavourId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating flavour: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ----------------- Delete Flavour -----------------
    public static boolean deleteFlavour(int flavourId) {
        String sql = "DELETE FROM flavours WHERE flavour_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, flavourId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting flavour: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================

    private static Flavour mapFlavour(ResultSet rs) throws SQLException {
        int flavourId = rs.getInt("flavour_id");
        String flavourName = rs.getString("flavour_name");
        double price = rs.getDouble("price");
        return new Flavour(flavourId, flavourName, price);
    }
}
