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

    // =================================================
    // Public order APIs
    // =================================================
    public static int placeOrder(int userId, int paymentId, List<CartItem> items) throws Exception {
        ReceiptData rd = placeOrderAndBuildReceipt(userId, paymentId, "", "", items);
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
                CartItem custom = null;

                // =================================================
                // 1) Validate custom cake rules
                // =================================================
                for (CartItem it : items) {
                    if (!it.isCustomCake()) continue;

                    if (custom != null) throw new SQLException("Only one custom cake allowed per order.");
                    custom = it;

                    if (it.getCustomCakeId() == null) throw new SQLException("Custom cake is missing cake_id.");
                    if (it.getPickupDate() == null) throw new SQLException("Custom cake pickup date is missing.");
                    if (it.getQuantity() != 1) throw new SQLException("Custom cake quantity must be 1.");

                    if (it.getCustomFlavourId() == null) throw new SQLException("Custom cake is missing flavour_id.");
                    if (it.getCustomSizeId() == null) throw new SQLException("Custom cake is missing size_id.");
                    if (it.getCustomShape() == null) throw new SQLException("Custom cake is missing shape.");

                    String opt = normalize(it.getOption());
                    if ("DISCOUNT".equals(opt) || "HOT".equals(opt) || "COLD".equals(opt)) {
                        throw new SQLException("Invalid option for custom cake item.");
                    }
                }

                // =================================================
                // 2) Custom cake quota & booking creation
                // =================================================
                Integer bookingId = null;
                if (custom != null) {
                    reserveCustomCakeSlot(con, custom.getPickupDate());
                    bookingId = insertBooking(con, java.time.LocalDate.now());
                }

                // =================================================
                // 3) Totals computation & order creation
                // =================================================
                Totals totals = computeTotals(con, items);

                LocalDateTime orderDate = LocalDateTime.now();
                int orderId = insertOrder(
                        con,
                        orderDate,
                        totals.subtotal,
                        totals.discountAmount,
                        totals.grandTotal,
                        userId,
                        paymentId
                );

                // =================================================
                // 4) Order items, inventory deduction & custom booking
                // =================================================
                for (CartItem it : items) {
                    String opt = normalize(it.getOption());

                    if (isDrink(opt)) {
                        int drinkId = getDrinkId(con, it.getProductId(), "COLD".equals(opt));
                        insertDrinkOrderItem(
                                con,
                                orderId,
                                drinkId,
                                opt,
                                it.getQuantity(),
                                money2(BigDecimal.valueOf(it.getUnitPrice()))
                        );
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

                        if (it.isCustomCake()) {
                            if (bookingId == null) throw new SQLException("Missing bookingId for custom cake.");
                            insertCustomCakeBooking(con, orderId, bookingId, it);
                        }

                        if (isTrackInventory(con, it.getProductId())) {
                            boolean discount = "DISCOUNT".equals(productOpt);
                            deductInventoryFEFO(
                                    con,
                                    it.getProductId(),
                                    it.getQuantity(),
                                    discount,
                                    orderItemId,
                                    userId
                            );
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

    // =================================================
    // Totals calculation
    // =================================================
    private static Totals computeTotals(Connection con, List<CartItem> items) throws SQLException {
        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal discountAmount = BigDecimal.ZERO;
        BigDecimal grandTotal = BigDecimal.ZERO;

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

    // =================================================
    // Order & order item inserts
    // =================================================
    private static int insertOrder(
            Connection con,
            LocalDateTime orderDate,
            BigDecimal subtotal,
            BigDecimal discountAmount,
            BigDecimal grandTotal,
            int userId,
            int paymentId
    ) throws SQLException {

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

    private static int insertProductOrderItem(
            Connection con,
            int orderId,
            int productId,
            String orderOption,
            int qty,
            BigDecimal unitPrice
    ) throws SQLException {

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

    private static void insertDrinkOrderItem(
            Connection con,
            int orderId,
            int drinkId,
            String orderOption,
            int qty,
            BigDecimal unitPrice
    ) throws SQLException {

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

    // =================================================
    // Inventory handling (FEFO)
    // =================================================
    private static void deductInventoryFEFO(
            Connection con,
            int productId,
            int qtyNeeded,
            boolean discount,
            int orderItemId,
            int userId
    ) throws SQLException {

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

    // =================================================
    // Lookups & helpers
    // =================================================
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

    private static boolean isDrink(String opt) {
        return "HOT".equals(opt) || "COLD".equals(opt);
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toUpperCase();
    }

    private static BigDecimal money2(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        return v.setScale(2, RoundingMode.HALF_UP);
    }

    // =================================================
    // Custom cake helpers
    // =================================================
    private static void reserveCustomCakeSlot(Connection con, java.time.LocalDate pickupDate) throws SQLException {
        if (pickupDate == null) throw new SQLException("Pickup date is required.");

        String ensureRow = """
                INSERT INTO custom_cake_daily_quota (pickup_date, booked_count)
                VALUES (?, 0)
                ON DUPLICATE KEY UPDATE booked_count = booked_count
                """;
        try (PreparedStatement ps = con.prepareStatement(ensureRow)) {
            ps.setDate(1, java.sql.Date.valueOf(pickupDate));
            ps.executeUpdate();
        }

        int current;
        String lock = "SELECT booked_count FROM custom_cake_daily_quota WHERE pickup_date = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(lock)) {
            ps.setDate(1, java.sql.Date.valueOf(pickupDate));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                current = rs.getInt(1);
            }
        }

        if (current + 1 > 3) throw new SQLException("Custom cake limit reached for pickup date: " + pickupDate);

        String inc = "UPDATE custom_cake_daily_quota SET booked_count = booked_count + 1 WHERE pickup_date = ?";
        try (PreparedStatement ps = con.prepareStatement(inc)) {
            ps.setDate(1, java.sql.Date.valueOf(pickupDate));
            ps.executeUpdate();
        }
    }

    private static void insertCustomCakeBooking(Connection con, int orderId, int bookingId, CartItem it) throws SQLException {
        String sql = """
                INSERT INTO custom_cake_bookings
                (order_id, cake_id, booking_id, pickup_date, flavour_id, topping_id, size_id, shape, custom_message)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, it.getCustomCakeId());
            ps.setInt(3, bookingId);
            ps.setDate(4, java.sql.Date.valueOf(it.getPickupDate()));

            ps.setInt(5, it.getCustomFlavourId());

            Integer toppingId = it.getCustomToppingId();
            if (toppingId == null) ps.setNull(6, Types.INTEGER);
            else ps.setInt(6, toppingId);

            ps.setInt(7, it.getCustomSizeId());
            ps.setString(8, toDbShape(it.getCustomShape()));

            String msg = it.getCustomMessage();
            if (msg == null || msg.isBlank()) ps.setNull(9, Types.VARCHAR);
            else {
                msg = msg.trim();
                if (msg.length() > 100) msg = msg.substring(0, 100);
                ps.setString(9, msg);
            }

            ps.executeUpdate();
        }
    }

    private static int insertBooking(Connection con, java.time.LocalDate bookingDate) throws SQLException {
        if (bookingDate == null) throw new SQLException("bookingDate is required.");

        String sql = """
                INSERT INTO bookings (booking_date, booking_status)
                VALUES (?, 'Pending')
                """;

        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setDate(1, java.sql.Date.valueOf(bookingDate));

            int affected = ps.executeUpdate();
            if (affected != 1) throw new SQLException("Failed to create booking.");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("Failed to get booking_id.");
                return rs.getInt(1);
            }
        }
    }

    private static String toDbShape(com.cakeshopsystem.utils.constants.CakeShape shape) {
        if (shape == null) return null;
        String n = shape.name();
        return n.substring(0, 1).toUpperCase() + n.substring(1).toLowerCase();
    }

    // =================================================
    // Receipt helpers
    // =================================================
    private static List<CartItem> deepCopyItems(List<CartItem> items) {
        List<CartItem> copy = new ArrayList<>(items.size());
        for (CartItem it : items) {
            CartItem c = new CartItem(
                    it.getProductId(),
                    it.getName(),
                    it.getOption(),
                    it.getUnitPrice(),
                    it.getQuantity()
            );
            if (it.isCustomCake()) {
                c.setCustomBooking(
                        it.getCustomCakeId(),
                        it.getPickupDate(),
                        it.getCustomMessage(),
                        it.getCustomFlavourId(),
                        it.getCustomToppingId(),
                        it.getCustomSizeId(),
                        it.getCustomShape()
                );
            }
            copy.add(c);
        }
        return copy;
    }

    // =================================================
    // Internal value object
    // =================================================
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
}
