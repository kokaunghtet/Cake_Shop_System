package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Order;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.time.LocalDate;
import java.sql.Date;
import java.util.List;


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



        public static LinkedHashMap<LocalDate, Double> getRevenueByDateRange(LocalDate start, LocalDate end, Integer categoryId) {
            LinkedHashMap<LocalDate, Double> map = new LinkedHashMap<>();

            String sqlAll =
                    "SELECT DATE(o.order_date) AS d, COALESCE(SUM(o.grand_total), 0) AS revenue " +
                            "FROM orders o " +
                            "WHERE DATE(o.order_date) BETWEEN ? AND ? " +
                            "GROUP BY DATE(o.order_date) " +
                            "ORDER BY d";

            String sqlByCategory =
                    "SELECT DATE(o.order_date) AS d, COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS revenue " +
                            "FROM orders o " +
                            "JOIN order_items oi ON oi.order_id = o.order_id " +
                            "LEFT JOIN products p_prod ON p_prod.product_id = oi.product_id " +
                            "LEFT JOIN drinks dr ON dr.drink_id = oi.drink_id " +
                            "LEFT JOIN products p_drink ON p_drink.product_id = dr.product_id " +
                            "WHERE DATE(o.order_date) BETWEEN ? AND ? " +
                            "  AND COALESCE(p_prod.category_id, p_drink.category_id) = ? " +
                            "GROUP BY DATE(o.order_date) " +
                            "ORDER BY d";

            String sql = (categoryId == null) ? sqlAll : sqlByCategory;

            try (Connection con = DB.connect();
                 PreparedStatement stmt = con.prepareStatement(sql)) {

                stmt.setDate(1, Date.valueOf(start));
                stmt.setDate(2, Date.valueOf(end));
                if (categoryId != null) stmt.setInt(3, categoryId);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Date d = rs.getDate("d");
                        double revenue = rs.getDouble("revenue");
                        if (d != null) map.put(d.toLocalDate(), revenue);
                    }
                }

            } catch (SQLException err) {
                System.err.println("Error fetching revenue by date range: " + err.getLocalizedMessage());
            }

            return map;
        }

        // ------------------ CARDS ------------------

        public static int getTotalMembers() {
            String sql = "SELECT COUNT(*) FROM members";

            try (Connection con = DB.connect();
                 PreparedStatement stmt = con.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                if (rs.next()) return rs.getInt(1);

            } catch (SQLException err) {
                System.err.println("Error fetching total members: " + err.getLocalizedMessage());
            }

            return 0;
        }

        public static int getTotalOrders(LocalDate start, LocalDate end) {
            String sql = "SELECT COUNT(*) FROM orders WHERE DATE(order_date) BETWEEN ? AND ?";

            try (Connection con = DB.connect();
                 PreparedStatement stmt = con.prepareStatement(sql)) {

                stmt.setDate(1, Date.valueOf(start));
                stmt.setDate(2, Date.valueOf(end));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }

            } catch (SQLException err) {
                System.err.println("Error fetching total orders: " + err.getLocalizedMessage());
            }

            return 0;
        }


         // Total sales (revenue) using orders.grand_total.

        public static double getTotalSales(LocalDate start, LocalDate end) {
            String sql = "SELECT COALESCE(SUM(grand_total), 0) FROM orders WHERE DATE(order_date) BETWEEN ? AND ?";

            try (Connection con = DB.connect();
                 PreparedStatement stmt = con.prepareStatement(sql)) {

                stmt.setDate(1, Date.valueOf(start));
                stmt.setDate(2, Date.valueOf(end));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getDouble(1);
                }

            } catch (SQLException err) {
                System.err.println("Error fetching total sales: " + err.getLocalizedMessage());
            }

            return 0.0;
        }


         // Total cancellations. Based on your seed SQL, cancellation status exists in bookings.booking_status.

        public static int getTotalCancelBookings(LocalDate start, LocalDate end) {
            String sql = "SELECT COUNT(*) FROM bookings WHERE booking_status = 'Cancelled' AND booking_date BETWEEN ? AND ?";

            try (Connection con = DB.connect();
                 PreparedStatement stmt = con.prepareStatement(sql)) {

                stmt.setDate(1, Date.valueOf(start));
                stmt.setDate(2, Date.valueOf(end));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }

            } catch (SQLException err) {
                System.err.println("Error fetching total cancelled bookings: " + err.getLocalizedMessage());
            }

            return 0;
        }

    // In OrderDAO
    /*public static int getTotalOrdersByUserId(int userId) {
        String sql = "SELECT COUNT(*) FROM orders WHERE user_id = ?";
        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException err) {
            System.err.println("Error fetching total orders for user: " + err.getLocalizedMessage());
        }
        return 0;*/

    /*public static int getTotalOrdersByUserId(
            int userId, java.time.LocalDate start, java.time.LocalDate end) {

        String sql = "SELECT COUNT(*) FROM orders " +
                "WHERE user_id = ? " +
                "AND DATE(order_date) BETWEEN ? AND ?";

        try (java.sql.Connection con = DB.connect();
             java.sql.PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, java.sql.Date.valueOf(start));
            stmt.setDate(3, java.sql.Date.valueOf(end));

            try (java.sql.ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (java.sql.SQLException err) {
            err.printStackTrace();
        }

        return 0;
    }*/ //original//

    public static ObservableList<Order> getOrdersByUserId(
            int userId, LocalDate start, LocalDate end) {

        ObservableList<Order> orders = FXCollections.observableArrayList();

        String sql = """
        SELECT * FROM orders
        WHERE user_id = ?
          AND DATE(order_date) BETWEEN ? AND ?
        ORDER BY order_date DESC
    """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setDate(2, Date.valueOf(start));
            stmt.setDate(3, Date.valueOf(end));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapOrder(rs));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching orders by user: " + e.getMessage());
        }

        return orders;
    }



    public static Object getCakeByProductId(Integer productId) {
        return null;
    }

    public static Object getProductById(Integer productId) {
        return null;
    }

    public static Object getDrinkById(Integer drinkId) {
        return null;
    }

    public static int getTotalOrdersByUserId(int userId, LocalDate localDate, LocalDate now) {
        return userId;
    }

    public static List<Order> getOrdersByUser(int userId) {
        return List.of();
    }

    /*public static int countOrdersByUser(int userId) {
        return userId;
    }

    public static Iterable<Object> getOrdersByUser(int userId) {
        return null;
    }*/
}


