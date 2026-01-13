package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Booking;
import com.cakeshopsystem.utils.constants.BookingStatus;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;

public class BookingDAO {

    // =====================================
    // ========== READ OPERATIONS ==========
    // =====================================

    public static ObservableList<Booking> getAllBookings() {
        ObservableList<Booking> bookings = FXCollections.observableArrayList();
        String sql = "SELECT * FROM bookings ORDER BY booking_date DESC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                bookings.add(mapBooking(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching bookings: " + err.getLocalizedMessage());
        }

        return bookings;
    }
    public static Booking getBookingById(int bookingId) {
        String sql = "SELECT * FROM bookings WHERE booking_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapBooking(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching booking by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static ObservableList<Booking> getBookingsByDate(LocalDate bookingDate) {
        ObservableList<Booking> bookings = FXCollections.observableArrayList();
        String sql = "SELECT * FROM bookings WHERE booking_date = ? ORDER BY booking_id DESC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(bookingDate));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) bookings.add(mapBooking(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching bookings by date: " + err.getLocalizedMessage());
        }

        return bookings;
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    // ----------------- Insert -----------------
    public static boolean insertBooking(Booking booking) {
        String sql = "INSERT INTO bookings (booking_date, booking_status) VALUES (?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setDate(1, Date.valueOf(booking.getBookingDate()));
            stmt.setString(2, toDbStatus(booking.getBookingStatus()));

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting booking failed, no rows affected.");

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    booking.setBookingId(generatedKeys.getInt(1));
                } else {
                    throw new SQLException("Inserting booking failed, no id obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting booking: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ----------------- Update -----------------
    public static boolean updateBooking(Booking booking) {
        String sql = "UPDATE bookings SET booking_date = ?, booking_status = ? WHERE booking_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(booking.getBookingDate()));
            stmt.setString(2, toDbStatus(booking.getBookingStatus()));
            stmt.setInt(3, booking.getBookingId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating booking: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ----------------- Delete -----------------
    public static boolean deleteBooking(int bookingId) {
        String sql = "DELETE FROM bookings WHERE booking_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting booking: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================
    private static Booking mapBooking(ResultSet rs) throws SQLException {
        int bookingId = rs.getInt("booking_id");

        Date d = rs.getDate("booking_date");
        LocalDate bookingDate = (d == null) ? null : d.toLocalDate();

        String statusStr = rs.getString("booking_status");
        BookingStatus status = fromDbStatus(statusStr);

        return new Booking(bookingId, bookingDate, status);
    }

    private static BookingStatus fromDbStatus(String dbValue) {
        if (dbValue == null) return BookingStatus.PENDING;

        // DB: "Pending" / "Confirmed" / "Cancelled"
        // Enum: PENDING / CONFIRMED / CANCELLED
        String normalized = dbValue.trim().toUpperCase();

        // handle case: "Pending" -> "PENDING"
        if ("PENDING".equals(normalized)) return BookingStatus.PENDING;
        if ("CONFIRMED".equals(normalized)) return BookingStatus.CONFIRMED;

        // DB uses "Cancelled" (double-l), enum likely CANCELLED
        if ("CANCELLED".equals(normalized) || "CANCELED".equals(normalized)) return BookingStatus.CANCELLED;

        // fallback
        try {
            return BookingStatus.valueOf(normalized);
        } catch (Exception ignored) {
            return BookingStatus.PENDING;
        }
    }

    private static String toDbStatus(BookingStatus status) {
        if (status == null) return "Pending";

        return switch (status) {
            case PENDING -> "Pending";
            case CONFIRMED -> "Confirmed";
            case CANCELLED -> "Cancelled";
        };
    }
}
