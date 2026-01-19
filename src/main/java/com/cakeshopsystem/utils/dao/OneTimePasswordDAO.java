package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.OTP;
import com.cakeshopsystem.utils.OTPCodeHasher;
import com.cakeshopsystem.utils.OTPResult;
import com.cakeshopsystem.utils.databaseconnection.DB;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;

public class OneTimePasswordDAO {

    public static OTPResult insertOTP(int userId, OTP otp) {

        String checkExistingSql =
                "SELECT 1 FROM otps " +
                        "WHERE user_id = ? AND used_at IS NULL AND expires_at > CURRENT_TIMESTAMP " +
                        "LIMIT 1";

        String insertSql =
                "INSERT INTO otps (user_id, otp_code, expires_at) VALUES (?, ?, ?)";

        try (Connection con = DB.connect()) {

            try (PreparedStatement checkStmt = con.prepareStatement(checkExistingSql)) {
                checkStmt.setInt(1, userId);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        return new OTPResult(false, "User already has an active OTP");
                    }
                }
            }

            try (PreparedStatement insertStmt = con.prepareStatement(insertSql)) {
                insertStmt.setInt(1, userId);
                // store salt:hash in otp_code
                insertStmt.setString(2, OTPCodeHasher.generateSecureOtpCode(otp.getCode()));
                insertStmt.setTimestamp(3, Timestamp.valueOf(otp.getExpireAt()));

                int rows = insertStmt.executeUpdate();
                return new OTPResult(rows > 0, rows > 0 ? "OTP created successfully" : "Failed to create OTP");
            }

        } catch (SQLException e) {
            System.err.println("Database error during OTP creation for user " + userId + ": " + e.getMessage());
            return new OTPResult(false, "System error: Could not create OTP");
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            System.err.println("OTP hashing error for user " + userId + ": " + e.getMessage());
            return new OTPResult(false, "System error: Could not create OTP");
        }
    }

    public static void deleteExpiredOTPs() {
        String sql = "DELETE FROM otps WHERE expires_at <= CURRENT_TIMESTAMP OR used_at IS NOT NULL";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException err) {
            System.err.println("Error deleting expired OTPs: " + err.getMessage());
        }
    }

    public static boolean isOtpValid(int userId, String code) {
        deleteExpiredOTPs();

        if (code == null) return false;
        String input = code.trim();
        if (input.length() != 6) return false;

        String sql =
                "SELECT otp_id, otp_code " +
                        "FROM otps " +
                        "WHERE user_id = ? AND used_at IS NULL AND expires_at > CURRENT_TIMESTAMP " +
                        "ORDER BY otp_id DESC " +
                        "LIMIT 1";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int otpId = rs.getInt("otp_id");
                    String storedSaltAndHash = rs.getString("otp_code");

                    // verify FIRST
                    boolean ok = OTPCodeHasher.verifyOtpCode(input, storedSaltAndHash);

                    // mark used ONLY if valid
                    return ok && markOtpAsUsed(otpId);
                }
            }

            return false;

        } catch (SQLException err) {
            System.err.println("Error validating OTP for user " + userId + ": " + err.getMessage());
            return false;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean markOtpAsUsed(int otpId) {
        final String sql = "UPDATE otps SET used_at = CURRENT_TIMESTAMP WHERE otp_id = ? AND used_at IS NULL";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, otpId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error marking OTP as used: " + err.getMessage());
            return false;
        }
    }
}
