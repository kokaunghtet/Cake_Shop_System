package com.cakeshopsystem.utils.validators;

import java.util.regex.Pattern;

public class Validator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$");

    // =========================
    // Normalization Helper
    // =========================
    private static String normalize(String s) {
        return s == null ? "" : s.trim();
    }

    // =========================
    // Username
    // =========================
    public static String validateUsername(String username) {
        username = normalize(username);

        if (username.isEmpty()) return "Username cannot be empty";

        if (Character.isDigit(username.charAt(0))) {
            return "Username must not start with a number";
        }

        if (username.matches(".*\\d.*")) {
            return "Username must not contain numbers";
        }

        if (!username.matches("^[A-Za-z ]+$")) {
            return "Username can contain only letters and spaces";
        }

        return null;
    }

    // =========================
    // Email
    // =========================
    public static String validateEmail(String email) {
        email = normalize(email);
        if (email.isEmpty()) {
            return "Email cannot be empty";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Invalid email format";
        }
        return null;
    }

    // =========================
    // Password (Login)
    // =========================
    public static String validateLoginPassword(String password) {
        password = normalize(password);
        if (password.isEmpty()) {
            return "Password cannot be empty";
        }
        return null;
    }

    // =========================
    // Password (Create/Change)
    // =========================
    public static String validatePassword(String password) {
        password = normalize(password);

        if (password.isEmpty()) {
            return "Password cannot be empty";
        }
        if (password.length() < 8) {
            return "Password must be at least 8 characters";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain at least one uppercase letter";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Password must contain at least one lowercase letter";
        }
        if (!password.matches(".*[0-9].*")) {
            return "Password must contain at least one number";
        }
        if (!password.matches(".*[@$!%*?&^#].*")) {
            return "Password must contain at least one special character";
        }

        return null;
    }

    // =========================
    // Confirm Password
    // =========================
    public static String matchPassword(String password, String confirmPassword) {
        password = password == null ? "" : password;
        confirmPassword = confirmPassword == null ? "" : confirmPassword;

        if (!password.equals(confirmPassword)) { // case-sensitive
            return "Passwords do not match";
        }
        return null;
    }
}
