package com.cakeshopsystem.utils.services;

import com.cakeshopsystem.models.CartItem;
import com.cakeshopsystem.models.ReceiptData;
import com.cakeshopsystem.utils.databaseconnection.DB;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderService {

    public static int placeOrder(int userId, int paymentId, List<CartItem> items) throws Exception {
        ReceiptData rd = placeOrderAndBuildReceipt(
                userId,
                paymentId,
                "",
                "",
                items
        );
        return rd.getOrderId();
    }

    public static ReceiptData placeOrderAndBuildReceipt(
            int userId,
            int paymentId,
            String cashierName,
            String paymentName,
            List<CartItem> items
    ) throws Exception {

        if (items == null || items.isEmpty()) throw new Exception("Cart is empty.");

        List<CartItem> receiptItems = deepCopyItems(items);

        try (Connection con = DB.connect()) {
            con.setAutoCommit(false);

            try {
                Totals totals = computeTotals(con, items);

                LocalDateTime orderDate = LocalDateTime.now();
                int orderId = insertOrder(con, orderDate, totals.subtotal, totals.discountAmount, totals.grandTotal, userId, paymentId);

                for (CartItem it : items) {
                    String opt = normalize(it.getOption());

                    if (isDrink(opt)) {
                        int drinkId = getDrinkId(con, it.getProductId(), "COLD".equals(opt));
                        insertDrinkOrderItem(con, orderId, drinkId, opt, it.getQuantity(), money2(BigDecimal.valueOf(it.getUnitPrice())));
                    } else {
                        String productOpt = ("DISCOUNT".equals(opt)) ? "DISCOUNT" : "REGULAR";

                        int orderItemId = insertProductOrderItem(
                                con,
                                orderId,
                                it.getProductId(),
                                productOpt,
                                it.getQuantity(),
                                money2(BigDecimal.valueOf(it.getUnitPrice()))
                        );

                        if (isTrackInventory(con, it.getProductId())) {
                            boolean discount = "DISCOUNT".equals(productOpt);
                            deductInventoryFEFO(con, it.getProductId(), it.getQuantity(), discount, orderItemId, userId);
                        }
                    }
                }

                con.commit();

                return new ReceiptData(
                        orderId,
                        orderDate,
                        cashierName == null ? "" : cashierName,
                        paymentName == null ? "" : paymentName,
                        receiptItems,
                        totals.subtotal,
                        totals.discountAmount,
                        totals.grandTotal
                );

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            }
        }
    }

    // =========================
    // Totals (BigDecimal)
    // =========================
    private static Totals computeTotals(Connection con, List<CartItem> items) throws SQLException {
        BigDecimal subtotal = BigDecimal.ZERO;        // normal total before discount
        BigDecimal discountAmount = BigDecimal.ZERO;  // saved
        BigDecimal grandTotal = BigDecimal.ZERO;      // customer pays

        for (CartItem it : items) {
            String opt = normalize(it.getOption());
            int qty = it.getQuantity();

            BigDecimal qtyBD = BigDecimal.valueOf(qty);
            BigDecimal chargedUnit = money2(BigDecimal.valueOf(it.getUnitPrice()));
            BigDecimal lineCharged = chargedUnit.multiply(qtyBD);

            grandTotal = grandTotal.add(lineCharged);

            if ("DISCOUNT".equals(opt)) {
                BigDecimal normalUnit = getProductBasePrice(con, it.getProductId());
                BigDecimal lineNormal = normalUnit.multiply(qtyBD);

                subtotal = subtotal.add(lineNormal);
                discountAmount = discountAmount.add(lineNormal.subtract(lineCharged));
            } else {
                subtotal = subtotal.add(lineCharged);
            }
        }

        subtotal = money2(subtotal);
        discountAmount = money2(discountAmount);
        grandTotal = money2(grandTotal);

        if (discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            discountAmount = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return new Totals(subtotal, discountAmount, grandTotal);
    }

    private static BigDecimal getProductBasePrice(Connection con, int productId) throws SQLException {
        String sql = "SELECT price FROM products WHERE product_id = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Missing product price for product_id=" + productId);
                return money2(rs.getBigDecimal(1));
            }
        }
    }

    private static BigDecimal money2(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    private static class Totals {
        final BigDecimal subtotal;
        final BigDecimal discountAmount;
        final BigDecimal grandTotal;

        Totals(BigDecimal subtotal, BigDecimal discountAmount, BigDecimal grandTotal) {
            this.subtotal = subtotal;
            this.discountAmount = discountAmount;
            this.grandTotal = grandTotal;
        }
    }

    // =========================
    // Inserts
    // =========================
    private static int insertOrder(Connection con,
                                   LocalDateTime orderDate,
                                   BigDecimal subtotal,
                                   BigDecimal discountAmount,
                                   BigDecimal grandTotal,
                                   int userId,
                                   int paymentId) throws SQLException {

        String sql = """
                INSERT INTO orders (order_date, subtotal, discount_amount, grand_total, member_id, user_id, payment_id)
                VALUES (?, ?, ?, ?, NULL, ?, ?)
                """;

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, Timestamp.valueOf(orderDate));
            ps.setBigDecimal(2, subtotal);
            ps.setBigDecimal(3, discountAmount);
            ps.setBigDecimal(4, grandTotal);
            ps.setInt(5, userId);
            ps.setInt(6, paymentId);

            int affected = ps.executeUpdate();
            if (affected != 1) throw new SQLException("Failed to create order.");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("Failed to get order_id.");
                return rs.getInt(1);
            }
        }
    }

    private static int insertProductOrderItem(Connection con, int orderId, int productId, String orderOption, int qty, BigDecimal unitPrice) throws SQLException {
        String sql = """
                INSERT INTO order_items (order_id, product_id, drink_id, order_option, quantity, unit_price)
                VALUES (?, ?, NULL, ?, ?, ?)
                """;

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, orderId);
            ps.setInt(2, productId);
            ps.setString(3, orderOption);
            ps.setInt(4, qty);
            ps.setBigDecimal(5, money2(unitPrice));

            int affected = ps.executeUpdate();
            if (affected != 1) throw new SQLException("Failed to create product order item.");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("Failed to get order_item_id.");
                return rs.getInt(1);
            }
        }
    }

    private static void insertDrinkOrderItem(Connection con, int orderId, int drinkId, String orderOption, int qty, BigDecimal unitPrice) throws SQLException {
        String sql = """
                INSERT INTO order_items (order_id, product_id, drink_id, order_option, quantity, unit_price)
                VALUES (?, NULL, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, drinkId);
            ps.setString(3, orderOption);
            ps.setInt(4, qty);
            ps.setBigDecimal(5, money2(unitPrice));

            int affected = ps.executeUpdate();
            if (affected != 1) throw new SQLException("Failed to create drink order item.");
        }
    }

    // =========================
    // Inventory (same as before)
    // =========================
    private static void deductInventoryFEFO(Connection con, int productId, int qtyNeeded, boolean discount, int orderItemId, int userId) throws SQLException {
        String selectSql = discount
                ? """
                SELECT inventory_id, quantity
                FROM inventory
                WHERE product_id = ?
                  AND quantity > 0
                  AND exp_date = CURRENT_DATE
                ORDER BY inventory_id ASC
                FOR UPDATE
                """
                : """
                SELECT inventory_id, quantity
                FROM inventory
                WHERE product_id = ?
                  AND quantity > 0
                  AND (exp_date IS NULL OR exp_date > CURRENT_DATE)
                ORDER BY exp_date IS NULL, exp_date ASC, inventory_id ASC
                FOR UPDATE
                """;

        int remaining = qtyNeeded;

        try (PreparedStatement ps = con.prepareStatement(selectSql)) {
            ps.setInt(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next() && remaining > 0) {
                    int inventoryId = rs.getInt("inventory_id");
                    int have = rs.getInt("quantity");
                    int take = Math.min(have, remaining);

                    updateInventoryQty(con, inventoryId, take);
                    insertMovement(con, inventoryId, -take, orderItemId, userId);

                    remaining -= take;
                }
            }
        }

        if (remaining > 0) throw new SQLException("Not enough stock for product_id=" + productId);
    }

    private static void updateInventoryQty(Connection con, int inventoryId, int takeQty) throws SQLException {
        String sql = "UPDATE inventory SET quantity = quantity - ? WHERE inventory_id = ? AND quantity >= ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, takeQty);
            ps.setInt(2, inventoryId);
            ps.setInt(3, takeQty);

            int affected = ps.executeUpdate();
            if (affected != 1) throw new SQLException("Failed to deduct inventory_id=" + inventoryId);
        }
    }

    private static void insertMovement(Connection con, int inventoryId, int qtyChange, int orderItemId, int userId) throws SQLException {
        String sql = """
                INSERT INTO inventory_movements (inventory_id, movement_type, qty_change, order_item_id, user_id)
                VALUES (?, 'Sale', ?, ?, ?)
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, inventoryId);
            ps.setInt(2, qtyChange);
            ps.setInt(3, orderItemId);
            ps.setInt(4, userId);
            ps.executeUpdate();
        }
    }

    private static boolean isTrackInventory(Connection con, int productId) throws SQLException {
        String sql = "SELECT track_inventory FROM products WHERE product_id = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return true;
                return rs.getBoolean(1);
            }
        }
    }

    private static int getDrinkId(Connection con, int productId, boolean isCold) throws SQLException {
        String sql = "SELECT drink_id FROM drinks WHERE product_id = ? AND is_cold = ? LIMIT 1";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            ps.setBoolean(2, isCold);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Missing drink row for product_id=" + productId);
                return rs.getInt(1);
            }
        }
    }

    private static boolean isDrink(String opt) {
        return "HOT".equals(opt) || "COLD".equals(opt);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    private static List<CartItem> deepCopyItems(List<CartItem> items) {
        List<CartItem> copy = new ArrayList<>(items.size());
        for (CartItem it : items) {
            copy.add(new CartItem(
                    it.getProductId(),
                    it.getName(),
                    it.getOption(),
                    it.getUnitPrice(),
                    it.getQuantity()
            ));
        }
        return copy;
    }
}
