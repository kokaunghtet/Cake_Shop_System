package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class InventoryDAO {

    // =====================================
    // ========== READ OPERATIONS ==========
    // =====================================

    public static ObservableList<Inventory> getAllInventory() {
        ObservableList<Inventory> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM inventory ORDER BY inventory_id DESC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapInventory(rs));
            }
        } catch (SQLException err) {
            System.err.println("Error fetching inventory: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static Inventory getInventoryById(int inventoryId) {
        String query = "SELECT * FROM inventory WHERE inventory_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, inventoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapInventory(rs);
            }
        } catch (SQLException err) {
            System.err.println("Error fetching inventory by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static ObservableList<Inventory> getInventoryByProductId(int productId) {
        ObservableList<Inventory> list = FXCollections.observableArrayList();
        String query = """
                    SELECT * FROM inventory
                    WHERE product_id = ?
                    ORDER BY exp_date IS NULL, exp_date ASC, inventory_id ASC
                """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInventory(rs));
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching inventory by product_id: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static int getTotalAvailableQuantityByProductId(int productId) {
        String query = """
        SELECT COALESCE(SUM(quantity), 0) AS total_qty
        FROM inventory
        WHERE product_id = ?
          AND (exp_date IS NULL OR exp_date >= CURRENT_DATE)
    """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("total_qty");
            }

        } catch (SQLException err) {
            System.err.println("Error summing inventory quantity: " + err.getLocalizedMessage());
            err.printStackTrace();
        }

        return 0;
    }


    public static Inventory getInventoryByProductIdAndExpDate(int productId, LocalDate expDate) {
        String query = """
                    SELECT * FROM inventory
                    WHERE product_id = ? AND exp_date <=> ?
                    LIMIT 1
                """;
        // NOTE: <=> is MySQL NULL-safe equality (works even if exp_date is NULL)

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, productId);

            if (expDate != null) stmt.setDate(2, Date.valueOf(expDate));
            else stmt.setNull(2, Types.DATE);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapInventory(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching inventory by product_id & exp_date: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static ObservableList<Inventory> getInventoryDiscountCandidates() {
        ObservableList<Inventory> list = FXCollections.observableArrayList();

        String sql = """
        SELECT * FROM inventory
        WHERE quantity > 0
          AND exp_date IN (?, ?)
        ORDER BY exp_date ASC, inventory_id ASC
    """;

        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(today));
            stmt.setDate(2, Date.valueOf(tomorrow));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInventory(rs));
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching inventory discount candidates: " + err.getLocalizedMessage());
        }

        return list;
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    public static boolean insertInventory(Inventory inv) {
        String sql = "INSERT INTO inventory (product_id, quantity, exp_date) VALUES (?, ?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, inv.getProductId());
            stmt.setInt(2, inv.getQuantity());

            if (inv.getExpDate() != null) stmt.setDate(3, Date.valueOf(inv.getExpDate()));
            else stmt.setNull(3, Types.DATE);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting inventory failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) inv.setInventoryId(keys.getInt(1));
            }

            // DB sets updated_at automatically; set something reasonable in model
            inv.setUpdatedAt(LocalDateTime.now());

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting inventory: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean updateInventory(Inventory inv) {
        String sql = "UPDATE inventory SET product_id = ?, quantity = ?, exp_date = ? WHERE inventory_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, inv.getProductId());
            stmt.setInt(2, inv.getQuantity());

            if (inv.getExpDate() != null) stmt.setDate(3, Date.valueOf(inv.getExpDate()));
            else stmt.setNull(3, Types.DATE);

            stmt.setInt(4, inv.getInventoryId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating inventory: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean deleteInventory(int inventoryId) {
        String sql = "DELETE FROM inventory WHERE inventory_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, inventoryId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting inventory: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================

    private static Inventory mapInventory(ResultSet rs) throws SQLException {
        int inventoryId = rs.getInt("inventory_id");
        int productId = rs.getInt("product_id");
        int quantity = rs.getInt("quantity");

        Date exp = rs.getDate("exp_date");
        LocalDate expDate = (exp != null) ? exp.toLocalDate() : null;

        Timestamp ts = rs.getTimestamp("updated_at");
        LocalDateTime updatedAt = (ts != null) ? ts.toLocalDateTime() : null;

        return new Inventory(inventoryId, productId, quantity, expDate, updatedAt);
    }
}
