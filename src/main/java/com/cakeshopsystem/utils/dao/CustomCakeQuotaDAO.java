package com.cakeshopsystem.utils.dao;

import java.sql.*;
import java.time.LocalDate;

public class CustomCakeQuotaDAO {

    public static void reserveSlot(Connection con, LocalDate pickupDate, int limitPerDay) throws SQLException {
        if (pickupDate == null) throw new SQLException("Pickup date is required.");

        String ensureRow = """
            INSERT INTO custom_cake_daily_quota (pickup_date, booked_count)
            VALUES (?, 0)
            ON DUPLICATE KEY UPDATE booked_count = booked_count
            """;
        try (PreparedStatement ps = con.prepareStatement(ensureRow)) {
            ps.setDate(1, Date.valueOf(pickupDate));
            ps.executeUpdate();
        }

        int current;
        String lock = "SELECT booked_count FROM custom_cake_daily_quota WHERE pickup_date = ? FOR UPDATE";
        try (PreparedStatement ps = con.prepareStatement(lock)) {
            ps.setDate(1, Date.valueOf(pickupDate));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                current = rs.getInt(1);
            }
        }

        if (current + 1 > limitPerDay) {
            throw new SQLException("Custom cake limit reached for pickup date: " + pickupDate);
        }

        String inc = "UPDATE custom_cake_daily_quota SET booked_count = booked_count + 1 WHERE pickup_date = ?";
        try (PreparedStatement ps = con.prepareStatement(inc)) {
            ps.setDate(1, Date.valueOf(pickupDate));
            ps.executeUpdate();
        }
    }

    public static void releaseSlot(Connection con, LocalDate pickupDate) throws SQLException {
        if (pickupDate == null) return;

        String ensureRow = """
            INSERT INTO custom_cake_daily_quota (pickup_date, booked_count)
            VALUES (?, 0)
            ON DUPLICATE KEY UPDATE booked_count = booked_count
            """;
        try (PreparedStatement ps = con.prepareStatement(ensureRow)) {
            ps.setDate(1, Date.valueOf(pickupDate));
            ps.executeUpdate();
        }

        String dec = """
            UPDATE custom_cake_daily_quota
            SET booked_count = GREATEST(booked_count - 1, 0)
            WHERE pickup_date = ?
            """;
        try (PreparedStatement ps = con.prepareStatement(dec)) {
            ps.setDate(1, Date.valueOf(pickupDate));
            ps.executeUpdate();
        }
    }
}
