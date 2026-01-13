package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Order;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class OrderDAO {

    // =====================================
    // ========== READ OPERATIONS ===========
    // =====================================

    public static ObservableList<Order> getAllOrder() {
        ObservableList<Order> orders = FXCollections.observableArrayList();
        String query = "SELECT * FROM orders ORDER BY order_id DESC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                orders.add(mapOrder(rs));
            }
        } catch (SQLException err) {
            System.err.println("Error fetching orders: " + err.getLocalizedMessage());
        }

        return orders;
    }

    public static Order getOrderById(int orderId) {
        String query = "SELECT * FROM orders WHERE order_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapOrder(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching order by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    // -------------------- CREATE -------------------
    public static boolean insertOrder(Order order) {
        String sql = """
            INSERT INTO orders (order_date, subtotal, discount_amount, grand_total, member_id, user_id, payment_id)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setTimestamp(1, Timestamp.valueOf(order.getOrderDate()));
            stmt.setDouble(2, order.getSubTotal());
            stmt.setDouble(3, order.getDiscount());
            stmt.setDouble(4, order.getGrandTotal());

            // member_id nullable
            if (order.getMemberId() != null) stmt.setInt(5, order.getMemberId());
            else stmt.setNull(5, Types.INTEGER);

            stmt.setInt(6, order.getUserId());
            stmt.setInt(7, order.getPaymentId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting orders failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) order.setOrderId(keys.getInt(1));
                else throw new SQLException("Inserting orders failed, no Id obtained");
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting order: " + err.getLocalizedMessage());
            return false;
        }
    }

    // -------------------- UPDATE -------------------
    public static boolean updateOrder(Order order) {
        String sql = """
            UPDATE orders
            SET subtotal = ?, discount_amount = ?, grand_total = ?, member_id = ?, user_id = ?, payment_id = ?
            WHERE order_id = ?
        """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setDouble(1, order.getSubTotal());
            stmt.setDouble(2, order.getDiscount());
            stmt.setDouble(3, order.getGrandTotal());

            if (order.getMemberId() != null) stmt.setInt(4, order.getMemberId());
            else stmt.setNull(4, Types.INTEGER);

            stmt.setInt(5, order.getUserId());
            stmt.setInt(6, order.getPaymentId());
            stmt.setInt(7, order.getOrderId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating order: " + err.getLocalizedMessage());
            return false;
        }
    }

    // Useful for membership linking step
    public static boolean updateOrderMemberId(int orderId, Integer memberId) {
        String sql = "UPDATE orders SET member_id = ? WHERE order_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            if (memberId != null) stmt.setInt(1, memberId);
            else stmt.setNull(1, Types.INTEGER);

            stmt.setInt(2, orderId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating order member_id: " + err.getLocalizedMessage());
            return false;
        }
    }

    // -------------------- DELETE -------------------
    public static boolean deleteOrder(int orderId) {
        String sql = "DELETE FROM orders WHERE order_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting order: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================

    private static Order mapOrder(ResultSet rs) throws SQLException {
        int orderId = rs.getInt("order_id");

        Timestamp od = rs.getTimestamp("order_date");
        LocalDateTime orderDate = (od == null) ? LocalDateTime.now() : od.toLocalDateTime();

        double subtotal = rs.getDouble("subtotal");
        double discount = rs.getDouble("discount_amount");
        double grandTotal = rs.getDouble("grand_total");

        Integer memberId = (Integer) rs.getObject("member_id"); // ✅ nullable

        int userId = rs.getInt("user_id");
        int paymentId = rs.getInt("payment_id");

        Timestamp ua = rs.getTimestamp("updated_at");
        LocalDateTime updatedAt = (ua == null) ? LocalDateTime.now() : ua.toLocalDateTime();

        return new Order(orderId, orderDate, subtotal, discount, grandTotal, memberId, userId, paymentId, updatedAt);
    }
}
