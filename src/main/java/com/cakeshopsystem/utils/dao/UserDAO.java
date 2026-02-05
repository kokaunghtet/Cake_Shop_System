package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.StaffPerformanceRow;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.AuthResult;
import com.cakeshopsystem.utils.PasswordHasher;
import com.cakeshopsystem.utils.databaseconnection.DB;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDAO {
    // ========================
    // ==== AUTHENTICATION ====
    // ========================
    public static AuthResult authenticateUser(String username, String password) throws SQLException {
        final String SQL_SELECT_USER_BY_USERNAME =
                "SELECT user_id, role_id, password_hash, email, user_name, image_path, is_active " +
                        "FROM users WHERE user_name = ?";

        final String SQL_UPDATE_LAST_LOGIN =
                "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement selectStmt = con.prepareStatement(SQL_SELECT_USER_BY_USERNAME)) {

            selectStmt.setString(1, username);

            try (ResultSet rs = selectStmt.executeQuery()) {

                if (!rs.next())
                    return new AuthResult(false, "Username not found!");

                String passwordHash = rs.getString("password_hash");
                if (!PasswordHasher.verify(password, passwordHash)) {
                    return new AuthResult(false, "Password incorrect!");
                }

                if (!rs.getBoolean("is_active"))
                    return new AuthResult(false, "This account has been deactivated!");

                int userId = rs.getInt("user_id");
                String userName = rs.getString("user_name");
                int roleId = rs.getInt("role_id");
                String email = rs.getString("email");
                String imagePath = rs.getString("image_path");

                // Update last login
                try (PreparedStatement updateLastLoginStmt = con.prepareStatement(SQL_UPDATE_LAST_LOGIN)) {
                    updateLastLoginStmt.setInt(1, userId);
                    updateLastLoginStmt.executeUpdate();
                }

                User loggedUser = new User(userId, userName, email, roleId);
                loggedUser.setImagePath(imagePath);
                SessionManager.setUser(loggedUser);

                return new AuthResult(true, "Authentication Successful.");
            }

        } catch (SQLException err) {
            System.err.println("Authentication error: " + err.getMessage());
            return new AuthResult(false, "System error: Authentication failed.");
        }
    }

    public static AuthResult updatePassword(int userId, String password) {
        final String SQL_UPDATE_PASSWORD_HASH =
                "UPDATE users SET password_hash = ? WHERE user_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement updatePasswordStmt = con.prepareStatement(SQL_UPDATE_PASSWORD_HASH)) {

            final String passwordHash = PasswordHasher.hash(password);

            updatePasswordStmt.setString(1, passwordHash);
            updatePasswordStmt.setInt(2, userId);

            int affectedRows = updatePasswordStmt.executeUpdate();

            return affectedRows > 0
                    ? new AuthResult(true, "Password updated successfully.")
                    : new AuthResult(false, "No User found with the specified ID.");

        } catch (SQLException ex) {
            System.err.println("Update Password error: " + ex.getLocalizedMessage());
            return new AuthResult(false, "System error: Could not update password.");
        }
    }

    public static AuthResult checkPassword(int userId, String password) {
        final String SQL_SELECT_PASSWORD_HASH_BY_ID =
                "SELECT password_hash FROM users WHERE user_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement selectHashStmt = con.prepareStatement(SQL_SELECT_PASSWORD_HASH_BY_ID)) {

            selectHashStmt.setInt(1, userId);

            try (ResultSet rs = selectHashStmt.executeQuery()) {
                if (!rs.next()) {
                    return new AuthResult(false, "User not found.");
                }

                final String passwordHash = rs.getString("password_hash");

                if (!PasswordHasher.verify(password, passwordHash)) {
                    return new AuthResult(false, "Current password is incorrect.");
                }

                return new AuthResult(true, "Password matches.");
            }

        } catch (SQLException ex) {
            System.err.println("Check Password error: " + ex.getLocalizedMessage());
            return new AuthResult(false, "System error: Could not verify password.");
        }
    }

    public static int isEmailExists(String email) {
        final String SQL_SELECT_USER_ID_BY_EMAIL =
                "SELECT user_id FROM users WHERE email = ?";

        try (Connection con = DB.connect();
             PreparedStatement selectUserIdStmt = con.prepareStatement(SQL_SELECT_USER_ID_BY_EMAIL)) {

            selectUserIdStmt.setString(1, email);

            try (ResultSet rs = selectUserIdStmt.executeQuery()) {
                if (!rs.next()) {
                    return 0;
                }
                return rs.getInt("user_id");
            }

        } catch (SQLException ex) {
            System.err.println("Error checking email existence: " + ex.getMessage());
            return 0;
        }
    }


    // ==================================
    // ======== CRUD OPERATIONS =========
    // ==================================

    // --- INSERT ---
    public static AuthResult insertUser(User user) {
        final String SQL_CHECK_USER_FIELDS =
                "SELECT user_name, email FROM users WHERE user_name = ? OR email = ?";

        final String SQL_INSERT_USER =
                "INSERT INTO users (user_name, password_hash, role_id, email, image_path) VALUES (?, ?, ?, ?, ?)";

        try (Connection con = DB.connect()) {

            // Check if username or email already exists
            try (PreparedStatement check = con.prepareStatement(SQL_CHECK_USER_FIELDS)) {
                check.setString(1, user.getUserName());
                check.setString(2, user.getEmail());

                try (ResultSet rs = check.executeQuery()) {
                    boolean usernameTaken = false;
                    boolean emailTaken = false;

                    while (rs.next()) {
                        String existingUsername = rs.getString("user_name");
                        String existingEmail = rs.getString("email");

                        if (existingUsername != null && existingUsername.equals(user.getUserName())) {
                            usernameTaken = true;
                        }
                        if (existingEmail != null && existingEmail.equals(user.getEmail())) {
                            emailTaken = true;
                        }
                    }

                    if (usernameTaken) return new AuthResult(false, "Username is already taken.");
                    if (emailTaken) return new AuthResult(false, "Email is already registered.");
                }
            }

            final String passwordHash = PasswordHasher.hash(user.getPassword());

            try (PreparedStatement ps = con.prepareStatement(SQL_INSERT_USER, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getUserName());
                ps.setString(2, passwordHash);
                ps.setInt(3, user.getRole());
                ps.setString(4, user.getEmail());
                ps.setString(5, user.getImagePath());

                int rows = ps.executeUpdate();
                if (rows == 0) {
                    throw new SQLException("Inserting user failed, no rows affected.");
                }

                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        user.setUserId(keys.getInt(1));
                    }
                }

                return new AuthResult(true, "User registered successfully.");
            }

        } catch (SQLException err) {
            System.err.println("Insert User error: " + err.getMessage());
            return new AuthResult(false, "System error: Could not create user.");
        }
    }

    // --- UPDATE ---
    public static boolean updateUser(User user) {
        final String SQL_UPDATE_USER =
                "UPDATE users " +
                        "SET user_name = ?, email = ?, image_path = ?, is_active = ?, role_id = ? " +
                        "WHERE user_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement updateStmt = con.prepareStatement(SQL_UPDATE_USER)) {

            updateStmt.setString(1, user.getUserName());
            updateStmt.setString(2, user.getEmail());
            updateStmt.setString(3, user.getImagePath());
            updateStmt.setBoolean(4, user.isActive());
            updateStmt.setInt(5, user.getRole());
            updateStmt.setInt(6, user.getUserId());

            return updateStmt.executeUpdate() > 0;

        } catch (SQLException ex) {
            System.err.println("Update User error: " + ex.getLocalizedMessage());
            return false;
        }
    }

    // --- DELETE ---
    public static boolean deleteUser(int userId) {
        final String SQL_DELETE_USER = "DELETE FROM users WHERE user_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement deleteStmt = con.prepareStatement(SQL_DELETE_USER)) {

            deleteStmt.setInt(1, userId);
            return deleteStmt.executeUpdate() > 0;

        } catch (SQLException ex) {
            System.err.println("Delete User error: " + ex.getMessage());
            return false;
        }
    }

    // =======================
    // ====== User List ======
    // =======================
    public static ObservableList<User> getAllUsers(int limit, int offset) {
        ObservableList<User> users = FXCollections.observableArrayList();

        int safeLimit = Math.max(1, limit);
        int safeOffset = Math.max(0, offset);

        final String SQL_SELECT_USERS_PAGED =
                "SELECT user_id, user_name, role_id, email, image_path, is_active " +
                        "FROM users " +
                        "ORDER BY user_id " +
                        "LIMIT ? OFFSET ?";

        try (Connection con = DB.connect();
             PreparedStatement selectUsersStmt = con.prepareStatement(SQL_SELECT_USERS_PAGED)) {

            selectUsersStmt.setInt(1, safeLimit);
            selectUsersStmt.setInt(2, safeOffset);

            try (ResultSet rs = selectUsersStmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUser(rs));
                }
            }

        } catch (SQLException ex) {
            System.err.println("Error fetching paginated user: " + ex.getLocalizedMessage());
        }

        return users;
    }

    public static int getTotalUserCount() {
        final String SQL_COUNT_USERS = "SELECT COUNT(*) FROM users";

        try (Connection con = DB.connect();
             PreparedStatement countUsersStmt = con.prepareStatement(SQL_COUNT_USERS);
             ResultSet rs = countUsersStmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException ex) {
            System.err.println("Error getting total user count: " + ex.getLocalizedMessage());
            return -1;
        }
    }

    public static Map<Integer, Integer> getUserCountByRole() {
        Map<Integer, Integer> result = new HashMap<>();
        String sql = "SELECT role_id, COUNT(*) AS count FROM users GROUP BY role_id";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            int total = 0;

            while (rs.next()) {
                int role = rs.getInt("role_id");
                int count = rs.getInt("count");
                result.put(role, count);
                total += count;
            }

            result.put(0, total);

        } catch (SQLException e) {
            System.err.println("Error getting user count by role: " + e.getLocalizedMessage());
        }

        return result;
    }

    public static User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try (Connection con = DB.connect(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next())
                return extractUser(rs);
        } catch (SQLException err) {
            System.err.println("Error fetching user: " + err.getLocalizedMessage());
        }
        return null;
    }

    public static ObservableList<User> getUsersByRole(int... roleIds) {
        ObservableList<User> users = FXCollections.observableArrayList();
        if (roleIds.length == 0)
            return users;

        StringBuilder sql = new StringBuilder("SELECT * FROM users WHERE role_id IN (");
        sql.append("?,".repeat(roleIds.length));
        sql.setLength(sql.length() - 1);
        sql.append(")");

        try (Connection con = DB.connect(); PreparedStatement stmt = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < roleIds.length; i++)
                stmt.setInt(i + 1, roleIds[i]);
            ResultSet rs = stmt.executeQuery();
            while (rs.next())
                users.add(extractUser(rs));
        } catch (SQLException err) {
            System.err.println("Error fetching users by role: " + err.getLocalizedMessage());
        }

        return users;
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================
    private static User extractUser(ResultSet rs) throws SQLException {
        int userId = rs.getInt("user_id");
        String userName = rs.getString("user_name");
        int roleId = rs.getInt("role_id");
        String email = rs.getString("email");
        String imagePath = rs.getString("image_path");
        boolean isActive = rs.getBoolean("is_active");

        User user = new User(userId, userName, email, roleId);
        user.setImagePath(imagePath);
        user.setActive(isActive);
        return user;
    }


    public static List<User> getAllStaff() {
        return List.of();
    }

    public static StaffPerformanceRow getStaffPerformance(int userId, String period, LocalDate date) {
        return null;
    }
}



