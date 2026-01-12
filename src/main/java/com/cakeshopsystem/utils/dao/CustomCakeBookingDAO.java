package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.CustomCakeBooking;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CustomCakeBookingDAO {

    // ===================== READ =====================

    public static ObservableList<CustomCakeBooking> getAllCustomCakeBooking() {
        ObservableList<CustomCakeBooking> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM custom_cake_bookings";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapCustomCakeBooking(rs));
            }
        } catch (SQLException err) {
            System.err.println("Error fetching custom cake bookings: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static CustomCakeBooking getCustomCakeBookingById(int customCakeBookingId) {
        String sql = "SELECT * FROM custom_cake_bookings WHERE custom_cake_booking_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, customCakeBookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapCustomCakeBooking(rs);
            }
        } catch (SQLException err) {
            System.err.println("Error fetching custom cake booking by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    // because you have UNIQUE(order_id)
    public static CustomCakeBooking getCustomCakeBookingByOrderId(int orderId) {
        String sql = "SELECT * FROM custom_cake_bookings WHERE order_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapCustomCakeBooking(rs);
            }
        } catch (SQLException err) {
            System.err.println("Error fetching custom cake booking by order id: " + err.getLocalizedMessage());
        }

        return null;
    }

    // ===================== CREATE =====================

    public static boolean insertCustomCakeBooking(CustomCakeBooking booking) {
        String sql = """
            INSERT INTO custom_cake_bookings (order_id, cake_id, booking_id, pickup_date, custom_message)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, booking.getOrderId());
            stmt.setInt(2, booking.getCustomCakeId());

            if (booking.getBookingId() == null) stmt.setNull(3, Types.INTEGER);
            else stmt.setInt(3, booking.getBookingId());

            if (booking.getPickupDate() == null) stmt.setNull(4, Types.DATE);
            else stmt.setDate(4, Date.valueOf(booking.getPickupDate()));

            stmt.setString(5, booking.getCustomMessage());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting custom_cake_bookings failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    booking.setCustomCakeOrderId(id); // your model method name
                } else {
                    throw new SQLException("Inserting custom_cake_bookings failed, no ID obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting custom cake booking: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ===================== UPDATE =====================

    public static boolean updateCustomCakeBooking(CustomCakeBooking booking) {
        String sql = """
            UPDATE custom_cake_bookings
            SET order_id = ?, cake_id = ?, booking_id = ?, pickup_date = ?, custom_message = ?
            WHERE custom_cake_booking_id = ?
            """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, booking.getOrderId());
            stmt.setInt(2, booking.getCustomCakeId());

            if (booking.getBookingId() == null) stmt.setNull(3, Types.INTEGER);
            else stmt.setInt(3, booking.getBookingId());

            if (booking.getPickupDate() == null) stmt.setNull(4, Types.DATE);
            else stmt.setDate(4, Date.valueOf(booking.getPickupDate()));

            stmt.setString(5, booking.getCustomMessage());
            stmt.setInt(6, booking.getCustomCakeOrderId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating custom cake booking: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ===================== DELETE =====================

    public static boolean deleteCustomCakeBooking(int customCakeBookingId) {
        String sql = "DELETE FROM custom_cake_bookings WHERE custom_cake_booking_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, customCakeBookingId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting custom cake booking: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ===================== HELPER =====================

    private static CustomCakeBooking mapCustomCakeBooking(ResultSet rs) throws SQLException {
        int id = rs.getInt("custom_cake_booking_id");
        int orderId = rs.getInt("order_id");
        int cakeId = rs.getInt("cake_id");

        Integer bookingId = (Integer) rs.getObject("booking_id"); // handles NULL safely

        Date pickup = rs.getDate("pickup_date");
        java.time.LocalDate pickupDate = (pickup == null) ? null : pickup.toLocalDate();

        String message = rs.getString("custom_message");

        return new CustomCakeBooking(id, orderId, cakeId, bookingId, pickupDate, message);
    }
}
