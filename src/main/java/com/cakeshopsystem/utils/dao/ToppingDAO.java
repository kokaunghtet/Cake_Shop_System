package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Topping;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class ToppingDAO {

    // =====================================
    // ========== READ OPERATIONS ===========
    // =====================================

    public static ObservableList<Topping> getAllToppings() {
        ObservableList<Topping> toppings = FXCollections.observableArrayList();
        String query = "SELECT * FROM toppings";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                toppings.add(mapTopping(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching toppings: " + err.getLocalizedMessage());
        }

        return toppings;
    }

    public static Topping getToppingById(int toppingId) {
        String query = "SELECT * FROM toppings WHERE topping_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, toppingId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapTopping(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching topping by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static Topping getToppingByName(String toppingName) {
        String query = "SELECT * FROM toppings WHERE topping_name = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, toppingName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapTopping(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching topping by name: " + err.getLocalizedMessage());
        }

        return null;
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    public static boolean insertTopping(Topping topping) {
        String sql = "INSERT INTO toppings (topping_name, price) VALUES (?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, topping.getToppingName());
            stmt.setDouble(2, topping.getPrice());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting toppings failed, no rows affected.");

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int toppingId = generatedKeys.getInt(1);
                    topping.setToppingId(toppingId);
                } else {
                    throw new SQLException("Inserting toppings failed, no ID obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting topping: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean updateTopping(Topping topping) {
        String sql = "UPDATE toppings SET topping_name = ?, price = ? WHERE topping_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, topping.getToppingName());
            stmt.setDouble(2, topping.getPrice());
            stmt.setInt(3, topping.getToppingId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating topping: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean deleteTopping(int toppingId) {
        String sql = "DELETE FROM toppings WHERE topping_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, toppingId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting topping: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================

    private static Topping mapTopping(ResultSet rs) throws SQLException {
        int toppingId = rs.getInt("topping_id");
        String toppingName = rs.getString("topping_name");
        double price = rs.getDouble("price");
        return new Topping(toppingId, toppingName, price);
    }
}
