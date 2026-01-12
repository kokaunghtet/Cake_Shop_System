package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Drink;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class DrinkDAO {

    // =====================================
    // ========== READ OPERATIONS ===========
    // =====================================

    public static ObservableList<Drink> getAllDrinks() {
        ObservableList<Drink> drinks = FXCollections.observableArrayList();
        String query = "SELECT * FROM drinks";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                drinks.add(mapDrink(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching drinks: " + err.getLocalizedMessage());
        }

        return drinks;
    }

    public static Drink getDrinkById(int drinkId) {
        String query = "SELECT * FROM drinks WHERE drink_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, drinkId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapDrink(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching drink by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static ObservableList<Drink> getDrinksByProductId(int productId) {
        ObservableList<Drink> drinks = FXCollections.observableArrayList();
        String query = "SELECT * FROM drinks WHERE product_id = ? ORDER BY is_cold ASC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    drinks.add(mapDrink(rs));
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching drinks by product id: " + err.getLocalizedMessage());
        }

        return drinks;
    }

    public static Drink getDrinkByProductIdAndCold(int productId, boolean isCold) {
        String query = "SELECT * FROM drinks WHERE product_id = ? AND is_cold = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, productId);
            stmt.setBoolean(2, isCold);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapDrink(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching drink by product and is_cold: " + err.getLocalizedMessage());
        }

        return null;
    }

    /**
     * POS helper: compute effective selling price for a drink variant:
     * products.price + drinks.price_delta
     */
    public static Double getEffectivePriceByDrinkId(int drinkId) {
        String query = """
                SELECT (p.price + d.price_delta) AS effective_price
                FROM drinks d
                JOIN products p ON p.product_id = d.product_id
                WHERE d.drink_id = ?
                """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, drinkId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("effective_price");
            }

        } catch (SQLException err) {
            System.err.println("Error calculating effective drink price: " + err.getLocalizedMessage());
        }

        return null;
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    // -------------------- CREATE -------------------
    public static boolean insertDrink(Drink drink) {
        String sql = "INSERT INTO drinks (product_id, is_cold, price_delta) VALUES (?, ?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, drink.getProductId());
            stmt.setBoolean(2, drink.isCold());
            stmt.setDouble(3, drink.getPriceDelta() == null ? 0.0 : drink.getPriceDelta());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting drinks failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    drink.setDrinkId(keys.getInt(1));
                } else {
                    throw new SQLException("Inserting drinks failed, no ID obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting drink: " + err.getLocalizedMessage());
            return false;
        }
    }

    // -------------------- UPDATE -------------------
    public static boolean updateDrink(Drink drink) {
        String sql = "UPDATE drinks SET product_id = ?, is_cold = ?, price_delta = ? WHERE drink_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, drink.getProductId());
            stmt.setBoolean(2, drink.isCold());
            stmt.setDouble(3, drink.getPriceDelta() == null ? 0.0 : drink.getPriceDelta());
            stmt.setInt(4, drink.getDrinkId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating drink: " + err.getLocalizedMessage());
            return false;
        }
    }

    // -------------------- DELETE -------------------
    public static boolean deleteDrink(int drinkId) {
        String sql = "DELETE FROM drinks WHERE drink_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, drinkId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting drink: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================

    private static Drink mapDrink(ResultSet rs) throws SQLException {
        int drinkId = rs.getInt("drink_id");
        int productId = rs.getInt("product_id");
        boolean isCold = rs.getBoolean("is_cold");
        double priceDelta = rs.getDouble("price_delta"); // default 0 if NULL

        return new Drink(drinkId, productId, isCold, priceDelta);
    }
}
