package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.OrderItem;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class OrderItemDAO {

    // =====================================
    // ========== READ OPERATIONS ===========
    // =====================================

    public static ObservableList<OrderItem> getAllOrderItem() {
        ObservableList<OrderItem> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM order_items ORDER BY order_item_id DESC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapOrderItem(rs));
            }
        } catch (SQLException err) {
            System.err.println("Error fetching order items: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static ObservableList<OrderItem> getOrderItemByOrderId(int orderId) {
        ObservableList<OrderItem> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM order_items WHERE order_id = ? ORDER BY order_item_id ASC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapOrderItem(rs));
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching order items by order_id: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static OrderItem getOrderItemById(int orderItemId) {
        String query = "SELECT * FROM order_items WHERE order_item_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, orderItemId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapOrderItem(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching order item by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    // -------------------- CREATE -------------------
    public static boolean insertOrderItem(OrderItem item) {
        String sql = """
            INSERT INTO order_items (order_id, product_id, drink_id, quantity, unit_price)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, item.getOrderId());

            // one target rule: either product_id or drink_id
            if (item.getProductId() != null) stmt.setInt(2, item.getProductId());
            else stmt.setNull(2, Types.INTEGER);

            if (item.getDrinkId() != null) stmt.setInt(3, item.getDrinkId());
            else stmt.setNull(3, Types.INTEGER);

            stmt.setInt(4, item.getQuantity());
            stmt.setDouble(5, item.getUnitPrice());

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("Inserting order_item failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) item.setOrderItemId(keys.getInt(1));
            }

            // line_total is generated in DB, so refresh it
            OrderItem refreshed = getOrderItemById(item.getOrderItemId());
            if (refreshed != null) item.setLineTotal(refreshed.getLineTotal());

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting order item: " + err.getLocalizedMessage());
            return false;
        }
    }

    // -------------------- UPDATE -------------------
    public static boolean updateOrderItem(OrderItem item) {
        // We generally don't allow changing order_id after creation.
        String sql = """
            UPDATE order_items
            SET product_id = ?, drink_id = ?, quantity = ?, unit_price = ?
            WHERE order_item_id = ?
        """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            if (item.getProductId() != null) stmt.setInt(1, item.getProductId());
            else stmt.setNull(1, Types.INTEGER);

            if (item.getDrinkId() != null) stmt.setInt(2, item.getDrinkId());
            else stmt.setNull(2, Types.INTEGER);

            stmt.setInt(3, item.getQuantity());
            stmt.setDouble(4, item.getUnitPrice());
            stmt.setInt(5, item.getOrderItemId());

            boolean ok = stmt.executeUpdate() > 0;

            if (ok) {
                OrderItem refreshed = getOrderItemById(item.getOrderItemId());
                if (refreshed != null) item.setLineTotal(refreshed.getLineTotal());
            }

            return ok;

        } catch (SQLException err) {
            System.err.println("Error updating order item: " + err.getLocalizedMessage());
            return false;
        }
    }

    // -------------------- DELETE -------------------
    public static boolean deleteOrderItem(int orderItemId) {
        String sql = "DELETE FROM order_items WHERE order_item_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, orderItemId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting order item: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean deleteOrderItemByOrderId(int orderId) {
        String sql = "DELETE FROM order_items WHERE order_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            return stmt.executeUpdate() >= 0;

        } catch (SQLException err) {
            System.err.println("Error deleting order items by order_id: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================

    private static OrderItem mapOrderItem(ResultSet rs) throws SQLException {
        int orderItemId = rs.getInt("order_item_id");
        int orderId = rs.getInt("order_id");

        Integer productId = (Integer) rs.getObject("product_id");
        Integer drinkId = (Integer) rs.getObject("drink_id");

        int quantity = rs.getInt("quantity");
        double unitPrice = rs.getDouble("unit_price");
        double lineTotal = rs.getDouble("line_total"); // generated column

        return new OrderItem(orderItemId, orderId, productId, drinkId, quantity, unitPrice, lineTotal);
    }
}
