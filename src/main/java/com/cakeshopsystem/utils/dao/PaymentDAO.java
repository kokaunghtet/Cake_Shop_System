package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Payment;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class PaymentDAO {

    // ==========================
    // -------- READ ------------
    // ==========================

    public static ObservableList<Payment> getAllPayments() {
        ObservableList<Payment> payments = FXCollections.observableArrayList();
        String sql = "select * from payments order by payment_id";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                payments.add(mapPayment(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching payments: " + err.getLocalizedMessage());
        }

        return payments;
    }

    public static ObservableList<Payment> getActivePayments() {
        ObservableList<Payment> payments = FXCollections.observableArrayList();
        String sql = "select * from payments where is_active = true order by payment_id";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                payments.add(mapPayment(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching active payments: " + err.getLocalizedMessage());
        }

        return payments;
    }

    public static Payment getPaymentById(int paymentId) {
        String sql = "select * from payments where payment_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, paymentId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapPayment(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching payment by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    // ==========================
    // -------- CREATE ----------
    // ==========================

    public static boolean insertPayment(Payment payment) {
        String sql = "insert into payments (payment_name, is_active) values (?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, payment.getPaymentName());
            stmt.setBoolean(2, payment.isActive());

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("Inserting payment failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    payment.setPaymentId(keys.getInt(1));
                } else {
                    throw new SQLException("Inserting payment failed, no id obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting payment: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ==========================
    // -------- UPDATE ----------
    // ==========================

    public static boolean updatePayment(Payment payment) {
        String sql = "update payments set payment_name = ?, is_active = ? where payment_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, payment.getPaymentName());
            stmt.setBoolean(2, payment.isActive());
            stmt.setInt(3, payment.getPaymentId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating payment: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean setPaymentActive(int paymentId, boolean active) {
        String sql = "update payments set is_active = ? where payment_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setBoolean(1, active);
            stmt.setInt(2, paymentId);

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating payment active status: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ==========================
    // -------- DELETE ----------
    // ==========================

    public static boolean deletePayment(int paymentId) {
        // note: this may fail if orders reference payments (fk restrict) — in that case, use setPaymentActive(false)
        String sql = "delete from payments where payment_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, paymentId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting payment: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ==========================
    // -------- HELPER ----------
    // ==========================

    private static Payment mapPayment(ResultSet rs) throws SQLException {
        int id = rs.getInt("payment_id");
        String name = rs.getString("payment_name");
        boolean isActive = rs.getBoolean("is_active");
        return new Payment(id, name, isActive);
    }
}
