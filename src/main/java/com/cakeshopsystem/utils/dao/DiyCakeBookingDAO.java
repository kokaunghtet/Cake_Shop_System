package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.DiyCakeBooking;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDateTime;

public class DiyCakeBookingDAO {

    // ===================== READ =====================

    public static ObservableList<DiyCakeBooking> getAllDiyCakeBooking() {
        ObservableList<DiyCakeBooking> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM diy_cake_bookings";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapDiyCakeBooking(rs));
            }
        } catch (SQLException err) {
            System.err.println("Error fetching diy cake bookings: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static DiyCakeBooking getDiyCakeBookingById(int diyCakeBookingId) {
        String sql = "SELECT * FROM diy_cake_bookings WHERE diy_cake_booking_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, diyCakeBookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapDiyCakeBooking(rs);
            }
        } catch (SQLException err) {
            System.err.println("Error fetching diy cake booking by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    // because you have UNIQUE(order_id)
    public static DiyCakeBooking getDiyCakeBookingByOrderId(int orderId) {
        String sql = "SELECT * FROM diy_cake_bookings WHERE order_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, orderId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapDiyCakeBooking(rs);
            }
        } catch (SQLException err) {
            System.err.println("Error fetching diy cake booking by order id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static ObservableList<DiyCakeBooking> getDiyCakeBookingByBookingId(int bookingId) {
        ObservableList<DiyCakeBooking> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM diy_cake_bookings WHERE booking_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(mapDiyCakeBooking(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching diy cake bookings by booking id: " + err.getLocalizedMessage());
        }

        return list;
    }

    // ===================== CREATE =====================

    public static boolean insertDiyCakeBooking(DiyCakeBooking booking) {
        if (booking == null) return false;
        if (booking.getStartTime() == null || booking.getEndTime() == null) return false;

        String sql = """
            INSERT INTO diy_cake_bookings (booking_id, member_id, cake_id, order_id, start_time, end_time)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, booking.getBookingId());
            stmt.setInt(2, booking.getMemberId());      // member_id
            stmt.setInt(3, booking.getCakeId());  // cake_id
            stmt.setInt(4, booking.getOrderId());

            stmt.setTimestamp(5, Timestamp.valueOf(booking.getStartTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(booking.getEndTime()));

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("Insert diy_cake_bookings failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    booking.setDiyCakeBookingId(keys.getInt(1));
                } else {
                    throw new SQLException("Insert diy_cake_bookings failed, no ID obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting diy cake booking: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ===================== UPDATE =====================

    public static boolean updateDiyCakeBooking(DiyCakeBooking booking) {
        if (booking == null) return false;
        if (booking.getStartTime() == null || booking.getEndTime() == null) return false;

        String sql = """
            UPDATE diy_cake_bookings
            SET booking_id = ?, member_id = ?, cake_id = ?, order_id = ?, start_time = ?, end_time = ?
            WHERE diy_cake_booking_id = ?
            """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, booking.getBookingId());
            stmt.setInt(2, booking.getMemberId());
            stmt.setInt(3, booking.getCakeId());
            stmt.setInt(4, booking.getOrderId());
            stmt.setTimestamp(5, Timestamp.valueOf(booking.getStartTime()));
            stmt.setTimestamp(6, Timestamp.valueOf(booking.getEndTime()));
            stmt.setInt(7, booking.getDiyCakeBookingId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating diy cake booking: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ===================== DELETE =====================

    public static boolean deleteDiyCakeBooking(int diyCakeBookingId) {
        String sql = "DELETE FROM diy_cake_bookings WHERE diy_cake_booking_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, diyCakeBookingId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting diy cake booking: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ===================== HELPER =====================

    private static DiyCakeBooking mapDiyCakeBooking(ResultSet rs) throws SQLException {
        int id = rs.getInt("diy_cake_booking_id");
        int bookingId = rs.getInt("booking_id");
        int memberId = rs.getInt("member_id");
        int cakeId = rs.getInt("cake_id");
        int orderId = rs.getInt("order_id");

        Timestamp st = rs.getTimestamp("start_time");
        Timestamp et = rs.getTimestamp("end_time");

        LocalDateTime start = (st == null) ? null : st.toLocalDateTime();
        LocalDateTime end = (et == null) ? null : et.toLocalDateTime();

        return new DiyCakeBooking(id, bookingId, memberId, cakeId, orderId, start, end);
    }
}
