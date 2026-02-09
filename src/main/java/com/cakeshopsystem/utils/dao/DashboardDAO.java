package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.utils.databaseconnection.DB;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DashboardDAO {

    // ---------- Small DTOs ----------
    public record IdName(int id, String name) {}

    public record NameValue(String name, long value) {}

    public record BucketValue(String bucket, BigDecimal value) {}

    public record BucketCount(String bucket, long count) {}

    // ---------- Filters for cbProductFilter ----------
    public static List<IdName> fetchCategories() throws SQLException {
        String sql = "SELECT category_id, category_name FROM categories ORDER BY category_name";
        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<IdName> out = new ArrayList<>();
            while (rs.next()) out.add(new IdName(rs.getInt("category_id"), rs.getString("category_name")));
            return out;
        }
    }

    public static List<IdName> fetchActiveProducts() throws SQLException {
        String sql = "SELECT product_id, product_name FROM products WHERE is_active = 1 ORDER BY product_name";
        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<IdName> out = new ArrayList<>();
            while (rs.next()) out.add(new IdName(rs.getInt(1), rs.getString(2)));
            return out;
        }
    }

    // ---------- MODE A: All / Category => TOP products (Bar+Pie) ----------
    public static List<NameValue> fetchTopProductsByQty(
            LocalDateTime startInclusive,
            LocalDateTime endExclusive,
            Integer categoryIdOrNull,
            int limit
    ) throws SQLException {

        String sql =
                "SELECT p.product_name, COALESCE(SUM(oi.quantity),0) AS qty " +
                        "FROM order_items oi " +
                        "JOIN orders o ON o.order_id = oi.order_id " +
                        "LEFT JOIN drinks d ON d.drink_id = oi.drink_id " +
                        "JOIN products p ON p.product_id = COALESCE(oi.product_id, d.product_id) " +
                        "WHERE o.order_date >= ? AND o.order_date < ? " +
                        (categoryIdOrNull != null ? "AND p.category_id = ? " : "") +
                        "GROUP BY p.product_id, p.product_name " +
                        "ORDER BY qty DESC " +
                        "LIMIT ?";

        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int i = 1;
            ps.setTimestamp(i++, Timestamp.valueOf(startInclusive));
            ps.setTimestamp(i++, Timestamp.valueOf(endExclusive));
            if (categoryIdOrNull != null) ps.setInt(i++, categoryIdOrNull);
            ps.setInt(i, limit);

            try (ResultSet rs = ps.executeQuery()) {
                List<NameValue> out = new ArrayList<>();
                while (rs.next()) out.add(new NameValue(rs.getString(1), rs.getLong(2)));
                return out;
            }
        }
    }

    public static long fetchTotalQtyAllProducts(
            LocalDateTime startInclusive,
            LocalDateTime endExclusive,
            Integer categoryIdOrNull
    ) throws SQLException {

        String sql =
                "SELECT COALESCE(SUM(oi.quantity),0) AS total_qty " +
                        "FROM order_items oi " +
                        "JOIN orders o ON o.order_id = oi.order_id " +
                        "LEFT JOIN drinks d ON d.drink_id = oi.drink_id " +
                        "JOIN products p ON p.product_id = COALESCE(oi.product_id, d.product_id) " +
                        "WHERE o.order_date >= ? AND o.order_date < ? " +
                        (categoryIdOrNull != null ? "AND p.category_id = ? " : "");

        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int i = 1;
            ps.setTimestamp(i++, Timestamp.valueOf(startInclusive));
            ps.setTimestamp(i++, Timestamp.valueOf(endExclusive));
            if (categoryIdOrNull != null) ps.setInt(i, categoryIdOrNull);

            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    // Revenue trend (Line) for All / Category
    public static List<BucketValue> fetchRevenueTrend(
            LocalDateTime startInclusive,
            LocalDateTime endExclusive,
            String period,                 // Daily/Weekly/Monthly/Yearly
            Integer categoryIdOrNull
    ) throws SQLException {

        String bucketExpr = bucketExpr(period);

        String sql =
                "SELECT " + bucketExpr + " AS bucket, COALESCE(SUM(oi.line_total),0) AS revenue " +
                        "FROM order_items oi " +
                        "JOIN orders o ON o.order_id = oi.order_id " +
                        "LEFT JOIN drinks d ON d.drink_id = oi.drink_id " +
                        "JOIN products p ON p.product_id = COALESCE(oi.product_id, d.product_id) " +
                        "WHERE o.order_date >= ? AND o.order_date < ? " +
                        (categoryIdOrNull != null ? "AND p.category_id = ? " : "") +
                        "GROUP BY bucket " +
                        "ORDER BY bucket";

        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            int i = 1;
            ps.setTimestamp(i++, Timestamp.valueOf(startInclusive));
            ps.setTimestamp(i++, Timestamp.valueOf(endExclusive));
            if (categoryIdOrNull != null) ps.setInt(i, categoryIdOrNull);

            try (ResultSet rs = ps.executeQuery()) {
                List<BucketValue> out = new ArrayList<>();
                while (rs.next()) out.add(new BucketValue(rs.getString(1), rs.getBigDecimal(2)));
                return out;
            }
        }
    }

    // ---------- MODE B: Single Product => product trend (Bar+Line) + option breakdown (Pie) ----------
    public static List<BucketCount> fetchProductQtyTrend(
            LocalDateTime startInclusive,
            LocalDateTime endExclusive,
            String period,
            int productId
    ) throws SQLException {

        String bucketExpr = bucketExpr(period);

        String sql =
                "SELECT " + bucketExpr + " AS bucket, COALESCE(SUM(oi.quantity),0) AS qty " +
                        "FROM order_items oi " +
                        "JOIN orders o ON o.order_id = oi.order_id " +
                        "LEFT JOIN drinks d ON d.drink_id = oi.drink_id " +
                        "JOIN products p ON p.product_id = COALESCE(oi.product_id, d.product_id) " +
                        "WHERE o.order_date >= ? AND o.order_date < ? " +
                        "AND p.product_id = ? " +
                        "GROUP BY bucket " +
                        "ORDER BY bucket";

        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(startInclusive));
            ps.setTimestamp(2, Timestamp.valueOf(endExclusive));
            ps.setInt(3, productId);

            try (ResultSet rs = ps.executeQuery()) {
                List<BucketCount> out = new ArrayList<>();
                while (rs.next()) out.add(new BucketCount(rs.getString(1), rs.getLong(2)));
                return out;
            }
        }
    }

    public static List<BucketValue> fetchProductRevenueTrend(
            LocalDateTime startInclusive,
            LocalDateTime endExclusive,
            String period,
            int productId
    ) throws SQLException {

        String bucketExpr = bucketExpr(period);

        String sql =
                "SELECT " + bucketExpr + " AS bucket, COALESCE(SUM(oi.line_total),0) AS revenue " +
                        "FROM order_items oi " +
                        "JOIN orders o ON o.order_id = oi.order_id " +
                        "LEFT JOIN drinks d ON d.drink_id = oi.drink_id " +
                        "JOIN products p ON p.product_id = COALESCE(oi.product_id, d.product_id) " +
                        "WHERE o.order_date >= ? AND o.order_date < ? " +
                        "AND p.product_id = ? " +
                        "GROUP BY bucket " +
                        "ORDER BY bucket";

        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(startInclusive));
            ps.setTimestamp(2, Timestamp.valueOf(endExclusive));
            ps.setInt(3, productId);

            try (ResultSet rs = ps.executeQuery()) {
                List<BucketValue> out = new ArrayList<>();
                while (rs.next()) out.add(new BucketValue(rs.getString(1), rs.getBigDecimal(2)));
                return out;
            }
        }
    }

    public static List<NameValue> fetchProductOptionBreakdown(
            LocalDateTime startInclusive,
            LocalDateTime endExclusive,
            int productId
    ) throws SQLException {

        String sql =
                "SELECT oi.order_option, COALESCE(SUM(oi.quantity),0) AS qty " +
                        "FROM order_items oi " +
                        "JOIN orders o ON o.order_id = oi.order_id " +
                        "LEFT JOIN drinks d ON d.drink_id = oi.drink_id " +
                        "JOIN products p ON p.product_id = COALESCE(oi.product_id, d.product_id) " +
                        "WHERE o.order_date >= ? AND o.order_date < ? " +
                        "AND p.product_id = ? " +
                        "GROUP BY oi.order_option " +
                        "ORDER BY qty DESC";

        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(startInclusive));
            ps.setTimestamp(2, Timestamp.valueOf(endExclusive));
            ps.setInt(3, productId);

            try (ResultSet rs = ps.executeQuery()) {
                List<NameValue> out = new ArrayList<>();
                while (rs.next()) out.add(new NameValue(rs.getString(1), rs.getLong(2)));
                return out;
            }
        }
    }

    // ---------- Bucket expression per period ----------
    private static String bucketExpr(String period) {
        // Keep it sortable (ORDER BY bucket works)
        return switch (period) {
            case "Daily" -> "DATE_FORMAT(o.order_date, '%H:00')";       // 09:00, 10:00...
            case "Weekly" -> "DATE_FORMAT(o.order_date, '%Y-%m-%d')";   // 2026-02-08...
            case "Monthly" -> "DATE_FORMAT(o.order_date, '%Y-%m-%d')";  // per day
            case "Yearly" -> "DATE_FORMAT(o.order_date, '%Y-%m')";      // 2026-02...
            default -> "DATE_FORMAT(o.order_date, '%Y-%m-%d')";
        };
    }

    public static List<Integer> fetchTopProductIdsByQtyInCategory(
            LocalDateTime startInclusive,
            LocalDateTime endExclusive,
            int categoryId,
            int limit
    ) throws SQLException {

        String sql =
                "SELECT p.product_id, SUM(oi.quantity) AS qty " +
                        "FROM order_items oi " +
                        "JOIN orders o ON o.order_id = oi.order_id " +
                        "LEFT JOIN drinks d ON d.drink_id = oi.drink_id " +
                        "JOIN products p ON p.product_id = COALESCE(oi.product_id, d.product_id) " +
                        "WHERE o.order_date >= ? AND o.order_date < ? " +
                        "AND p.category_id = ? " +
                        "GROUP BY p.product_id " +
                        "ORDER BY qty DESC " +
                        "LIMIT ?";

        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(startInclusive));
            ps.setTimestamp(2, Timestamp.valueOf(endExclusive));
            ps.setInt(3, categoryId);
            ps.setInt(4, limit);

            List<Integer> ids = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt("product_id"));
                }
            }
            return ids;
        }
    }

    public static LocalDate fetchLatestOrderDate() throws SQLException {
        String sql = "SELECT MAX(order_date) AS max_date FROM orders";
        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                Timestamp ts = rs.getTimestamp("max_date");
                return ts == null ? null : ts.toLocalDateTime().toLocalDate();
            }
            return null;
        }
    }


    // inside DashboardDAO

    public record KpiData(long members, long orders, java.math.BigDecimal sales, long cancels) {}

    public static KpiData fetchKpis(
            java.time.LocalDateTime startInclusive,
            java.time.LocalDateTime endExclusive,
            Integer categoryIdOrNull
    ) throws java.sql.SQLException {

        long members = fetchNewMembersCount(startInclusive, endExclusive);
        long orders  = fetchOrdersCount(startInclusive, endExclusive, categoryIdOrNull);
        java.math.BigDecimal sales = fetchSalesTotal(startInclusive, endExclusive, categoryIdOrNull);

        // bookings.booking_date is DATE, so use LocalDate range
        long cancels = fetchCancelledBookingsCount(
                startInclusive.toLocalDate(),
                endExclusive.toLocalDate(),
                categoryIdOrNull
        );

        return new KpiData(members, orders, sales, cancels);
    }

    private static long fetchNewMembersCount(java.time.LocalDateTime start, java.time.LocalDateTime end) throws java.sql.SQLException {
        String sql = "SELECT COUNT(*) FROM members WHERE created_at >= ? AND created_at < ?";
        try (java.sql.Connection con = com.cakeshopsystem.utils.databaseconnection.DB.connect();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, java.sql.Timestamp.valueOf(start));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(end));
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private static long fetchOrdersCount(java.time.LocalDateTime start, java.time.LocalDateTime end, Integer categoryIdOrNull) throws java.sql.SQLException {

        if (categoryIdOrNull == null) {
            String sql = "SELECT COUNT(*) FROM orders WHERE order_date >= ? AND order_date < ?";
            try (java.sql.Connection con = com.cakeshopsystem.utils.databaseconnection.DB.connect();
                 java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setTimestamp(1, java.sql.Timestamp.valueOf(start));
                ps.setTimestamp(2, java.sql.Timestamp.valueOf(end));
                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        }

        String sql =
                "SELECT COUNT(DISTINCT o.order_id) " +
                        "FROM orders o " +
                        "JOIN order_items oi ON oi.order_id = o.order_id " +
                        "LEFT JOIN drinks d ON d.drink_id = oi.drink_id " +
                        "JOIN products p ON p.product_id = COALESCE(oi.product_id, d.product_id) " +
                        "WHERE o.order_date >= ? AND o.order_date < ? " +
                        "AND p.category_id = ?";

        try (java.sql.Connection con = com.cakeshopsystem.utils.databaseconnection.DB.connect();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, java.sql.Timestamp.valueOf(start));
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(end));
            ps.setInt(3, categoryIdOrNull);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }

    private static java.math.BigDecimal fetchSalesTotal(java.time.LocalDateTime start, java.time.LocalDateTime end, Integer categoryIdOrNull) throws java.sql.SQLException {
        String sql =
                "SELECT COALESCE(SUM(oi.line_total),0) " +
                        "FROM order_items oi " +
                        "JOIN orders o ON o.order_id = oi.order_id " +
                        "LEFT JOIN drinks d ON d.drink_id = oi.drink_id " +
                        "JOIN products p ON p.product_id = COALESCE(oi.product_id, d.product_id) " +
                        "WHERE o.order_date >= ? AND o.order_date < ? " +
                        (categoryIdOrNull != null ? "AND p.category_id = ? " : "");

        try (java.sql.Connection con = com.cakeshopsystem.utils.databaseconnection.DB.connect();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            int i = 1;
            ps.setTimestamp(i++, java.sql.Timestamp.valueOf(start));
            ps.setTimestamp(i++, java.sql.Timestamp.valueOf(end));
            if (categoryIdOrNull != null) ps.setInt(i, categoryIdOrNull);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getBigDecimal(1);
            }
        }
    }

    private static long fetchCancelledBookingsCount(
            java.time.LocalDate startDateInclusive,
            java.time.LocalDate endDateExclusive,
            Integer categoryIdOrNull
    ) throws java.sql.SQLException {

        // No category filter -> count all cancelled bookings in date range
        if (categoryIdOrNull == null) {
            String sql =
                    "SELECT COUNT(*) " +
                            "FROM bookings " +
                            "WHERE booking_status = 'Cancelled' " +
                            "AND booking_date >= ? AND booking_date < ?";

            try (java.sql.Connection con = com.cakeshopsystem.utils.databaseconnection.DB.connect();
                 java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setDate(1, java.sql.Date.valueOf(startDateInclusive));
                ps.setDate(2, java.sql.Date.valueOf(endDateExclusive));

                try (java.sql.ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    return rs.getLong(1);
                }
            }
        }

        // Category filter -> count cancelled bookings that are tied to cakes in that category
        // (For Drink/Baked Goods/Accessory, this will likely return 0, which is correct.)
        String sql =
                "SELECT COUNT(DISTINCT b.booking_id) " +
                        "FROM bookings b " +
                        "LEFT JOIN diy_cake_bookings diy ON diy.booking_id = b.booking_id " +
                        "LEFT JOIN custom_cake_bookings ccb ON ccb.booking_id = b.booking_id " +
                        "LEFT JOIN cakes c ON c.cake_id = COALESCE(diy.cake_id, ccb.cake_id) " +
                        "LEFT JOIN products p ON p.product_id = c.product_id " +
                        "WHERE b.booking_status = 'Cancelled' " +
                        "AND b.booking_date >= ? AND b.booking_date < ? " +
                        "AND p.category_id = ?";

        try (java.sql.Connection con = com.cakeshopsystem.utils.databaseconnection.DB.connect();
             java.sql.PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(startDateInclusive));
            ps.setDate(2, java.sql.Date.valueOf(endDateExclusive));
            ps.setInt(3, categoryIdOrNull);

            try (java.sql.ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        }
    }


}
