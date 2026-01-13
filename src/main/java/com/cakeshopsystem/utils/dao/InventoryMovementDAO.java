package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.InventoryMovement;
import com.cakeshopsystem.utils.constants.InventoryMovementType;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class InventoryMovementDAO {

    public static ObservableList<InventoryMovement> getAllInventoryMovement() {
        ObservableList<InventoryMovement> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM inventory_movements ORDER BY inventory_movement_id DESC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapInventoryMovement(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching inventory movements: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static ObservableList<InventoryMovement> getInventoryMovementByInventoryId(int inventoryId) {
        ObservableList<InventoryMovement> list = FXCollections.observableArrayList();
        String sql = """
            SELECT * FROM inventory_movements
            WHERE inventory_id = ?
            ORDER BY moved_at DESC
        """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, inventoryId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInventoryMovement(rs));
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching inventory movements by inventory_id: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static ObservableList<InventoryMovement> getInventoryMovementByOrderItemId(int orderItemId) {
        ObservableList<InventoryMovement> list = FXCollections.observableArrayList();
        String sql = """
            SELECT * FROM inventory_movements
            WHERE order_item_id = ?
            ORDER BY moved_at DESC
        """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, orderItemId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapInventoryMovement(rs));
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching inventory movements by order_item_id: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static InventoryMovement getInventoryMovementById(int inventoryMovementId) {
        String sql = "SELECT * FROM inventory_movements WHERE inventory_movement_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, inventoryMovementId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapInventoryMovement(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching inventory movement by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    // -------------------- CREATE -------------------
    public static boolean insertInventoryMovement(InventoryMovement mv) {
        String sql = """
            INSERT INTO inventory_movements
                (inventory_id, movement_type, qty_change, order_item_id, user_id, moved_at)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, mv.getInventoryId());
            stmt.setString(2, mv.getMovementType().name());
            stmt.setInt(3, mv.getQuantityChange());

            if (mv.getOrderItemId() != null) stmt.setInt(4, mv.getOrderItemId());
            else stmt.setNull(4, Types.INTEGER);

            if (mv.getUserId() != null) stmt.setInt(5, mv.getUserId());
            else stmt.setNull(5, Types.INTEGER);

            // optional: let DB default CURRENT_TIMESTAMP if null
            if (mv.getMovedAt() != null) stmt.setTimestamp(6, Timestamp.valueOf(mv.getMovedAt()));
            else stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("Inserting inventory_movement failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) mv.setInventoryMovementId(keys.getInt(1));
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting inventory movement: " + err.getLocalizedMessage());
            return false;
        }
    }

    // -------------------- UPDATE -------------------
    // RARE MOVE : Updating inventory movements should be uncommon.
    public static boolean updateInventoryMovement(InventoryMovement mv) {
        // Usually movements are audit logs; updating should be rare.
        String sql = """
            UPDATE inventory_movements
            SET inventory_id = ?, movement_type = ?, qty_change = ?, order_item_id = ?, user_id = ?, moved_at = ?
            WHERE inventory_movement_id = ?
        """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, mv.getInventoryId());
            stmt.setString(2, mv.getMovementType().name());
            stmt.setInt(3, mv.getQuantityChange());

            if (mv.getOrderItemId() != null) stmt.setInt(4, mv.getOrderItemId());
            else stmt.setNull(4, Types.INTEGER);

            if (mv.getUserId() != null) stmt.setInt(5, mv.getUserId());
            else stmt.setNull(5, Types.INTEGER);

            stmt.setTimestamp(6, Timestamp.valueOf(mv.getMovedAt()));
            stmt.setInt(7, mv.getInventoryMovementId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating inventory movement: " + err.getLocalizedMessage());
            return false;
        }
    }

    // -------------------- DELETE -------------------
    // RARE MOVE : Deleting inventory movements should be uncommon.
    public static boolean deleteInventoryMovement(int inventoryMovementId) {
        String sql = "DELETE FROM inventory_movements WHERE inventory_movement_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, inventoryMovementId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting inventory movement: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================

    private static InventoryMovement mapInventoryMovement(ResultSet rs) throws SQLException {
        int id = rs.getInt("inventory_movement_id");
        int inventoryId = rs.getInt("inventory_id");

        String typeStr = rs.getString("movement_type");
        InventoryMovementType type = InventoryMovementType.valueOf(typeStr);

        int qtyChange = rs.getInt("qty_change");

        Integer orderItemId = (Integer) rs.getObject("order_item_id");
        Integer userId = (Integer) rs.getObject("user_id");

        Timestamp ts = rs.getTimestamp("moved_at");
        LocalDateTime movedAt = (ts != null) ? ts.toLocalDateTime() : LocalDateTime.now();

        return new InventoryMovement(id, inventoryId, type, qtyChange, orderItemId, userId, movedAt);
    }
}
