package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.DiyCakeBooking;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DiyCakeBookingDAO {

    // ---------------------
    // READ
    // ---------------------
    public static ObservableList<DiyCakeBooking> getAllDiyCakeBookings() {
        ObservableList<DiyCakeBooking> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM diy_cake_bookings ORDER BY session_date DESC, session_start DESC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) list.add(map(rs));

        } catch (SQLException err) {
            System.err.println("Error fetching diy cake bookings: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static DiyCakeBooking getDiyCakeBookingById(int id) {
        String sql = "SELECT * FROM diy_cake_bookings WHERE diy_cake_booking_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return map(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching diy booking by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static DiyCakeBooking getDiyCakeBookingByOrderId(int orderId) {
        String sql = "SELECT * FROM diy_cake_bookings WHERE order_id = ? LIMIT 1";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return map(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching diy booking by order id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static ObservableList<DiyCakeBooking> getDiyCakeBookingsByBookingId(int bookingId) {
        ObservableList<DiyCakeBooking> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM diy_cake_bookings WHERE booking_id = ? ORDER BY session_date DESC, session_start DESC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, bookingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching diy bookings by booking id: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static ObservableList<DiyCakeBooking> getDiyCakeBookingsByDate(LocalDate date) {
        ObservableList<DiyCakeBooking> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM diy_cake_bookings WHERE session_date = ? ORDER BY session_start ASC";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(date));
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching diy bookings by date: " + err.getLocalizedMessage());
        }

        return list;
    }

    // ---------------------
    // INSERT (transaction-safe)
    // NOTE: Option B: quota reserve/release must be done in SAME transaction (BookingDAO.reserveDiySlot / releaseDiySlot)
    // ---------------------
    public static boolean insertDiyCakeBooking(Connection con, DiyCakeBooking booking) throws SQLException {
        if (con == null) throw new SQLException("Connection is required");
        if (booking == null) throw new SQLException("booking is required");

        if (booking.getSessionDate() == null) throw new SQLException("session_date is required");
        if (booking.getSessionStart() == null) throw new SQLException("session_start is required");
        if (!isAllowedSessionStart(booking.getSessionStart())) {
            throw new SQLException("Invalid session_start (allowed: 09:00, 12:00, 15:00)");
        }

        String sql = """
            INSERT INTO diy_cake_bookings
            (booking_id, member_id, cake_id, order_id, session_date, session_start)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, booking.getBookingId());
            stmt.setInt(2, booking.getMemberId());
            stmt.setInt(3, booking.getCakeId());
            stmt.setInt(4, booking.getOrderId());
            stmt.setDate(5, Date.valueOf(booking.getSessionDate()));
            stmt.setTime(6, Time.valueOf(booking.getSessionStart()));

            int affected = stmt.executeUpdate();
            if (affected != 1) return false;

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) booking.setDiyCakeBookingId(keys.getInt(1));
            }
            return true;
        }
    }

    // ---------------------
    // Availability (Option B = quota table)
    // ---------------------
    public static Set<LocalTime> getTakenSessionStarts(LocalDate date) {
        Set<LocalTime> set = new HashSet<>();
        if (date == null) return set;

        String sql = """
            SELECT session_start
            FROM diy_session_quota
            WHERE session_date = ?
              AND booked_count > 0
            """;

        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Time t = rs.getTime(1);
                    if (t != null) set.add(t.toLocalTime());
                }
            }

        } catch (SQLException e) {
            System.err.println("Error fetching taken DIY slots: " + e.getLocalizedMessage());
        }

        return set;
    }

    public static boolean isSlotAvailable(LocalDate date, LocalTime start) {
        if (date == null || start == null) return false;
        if (!isAllowedSessionStart(start)) return false;

        String sql = """
            SELECT booked_count
            FROM diy_session_quota
            WHERE session_date = ? AND session_start = ?
            """;

        try (Connection con = DB.connect();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDate(1, Date.valueOf(date));
            ps.setTime(2, Time.valueOf(start));

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return true;     // no quota row => free
                return rs.getInt(1) == 0;        // 0 => free, 1 => taken
            }

        } catch (SQLException e) {
            System.err.println("Error checking DIY slot: " + e.getLocalizedMessage());
            return false;
        }
    }

    public static List<LocalTime> getAvailableSessions(LocalDate sessionDate) {
        List<LocalTime> all = List.of(LocalTime.of(9, 0), LocalTime.of(12, 0), LocalTime.of(15, 0));
        if (sessionDate == null) return all;

        Set<LocalTime> taken = getTakenSessionStarts(sessionDate);
        java.util.ArrayList<LocalTime> free = new java.util.ArrayList<>();
        for (LocalTime t : all) if (!taken.contains(t)) free.add(t);
        return free;
    }

    private static boolean isAllowedSessionStart(LocalTime t) {
        return t != null && (
                t.equals(LocalTime.of(9, 0)) ||
                        t.equals(LocalTime.of(12, 0)) ||
                        t.equals(LocalTime.of(15, 0))
        );
    }

    // ---------------------
    // Mapper
    // ---------------------
    private static DiyCakeBooking map(ResultSet rs) throws SQLException {
        int id = rs.getInt("diy_cake_booking_id");
        int bookingId = rs.getInt("booking_id");
        int memberId = rs.getInt("member_id");
        int cakeId = rs.getInt("cake_id");
        int orderId = rs.getInt("order_id");

        Date d = rs.getDate("session_date");
        LocalDate sessionDate = (d == null) ? null : d.toLocalDate();

        Time t = rs.getTime("session_start");
        LocalTime sessionStart = (t == null) ? null : t.toLocalTime();

        return new DiyCakeBooking(id, bookingId, memberId, cakeId, orderId, sessionDate, sessionStart);
    }
}
