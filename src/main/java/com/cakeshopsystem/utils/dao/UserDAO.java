package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.AuthResult;
import com.cakeshopsystem.utils.PasswordHasher;
import com.cakeshopsystem.utils.databaseconnection.DB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {
    // --- Authentication ---
    public static AuthResult authenticateUser(String username, String password) throws SQLException {
        String query = "select user_id, role_id, password_hash, email, user_name, image_path, is_active from users where user_name = ?";
        try (Connection con = DB.connect(); PreparedStatement preparedStatement = con.prepareStatement(query)) {
            preparedStatement.setString(1, username);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (!resultSet.next())
                    return new AuthResult(false, "Username not found!");

                String hashedPassword = resultSet.getString("password_hash");
                if (!PasswordHasher.verify(password, hashedPassword)) {
                    return new AuthResult(false, "Password incorrect!");
                }

                if (!resultSet.getBoolean("is_active"))
                    return new AuthResult(false, "This account has been deactivated by Admin!");

                int userId = resultSet.getInt("user_id");
                String userName = resultSet.getString("user_name");
                int roleId = resultSet.getInt("role_id");
                String email = resultSet.getString("email");
                String image = resultSet.getString("image_path");

                // Update last login
                try (PreparedStatement updateStmt = con
                        .prepareStatement("UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE user_id = ?")) {
                    updateStmt.setInt(1, userId);
                    updateStmt.executeUpdate();
                }

                User loggedUser = new User(userId, userName, email, roleId);
                loggedUser.setImagePath(image);
                // Code line for setting loggedUser to SessionManager.

                return new AuthResult(true, "Authentication Successful.");
            }
        } catch (SQLException err) {
            System.err.println("Authentication error: " + err.getMessage());
            return new AuthResult(false, "System error: Authentication failed.");
        }
    }
}
