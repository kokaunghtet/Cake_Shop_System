package com.cakeshopsystem.utils.validators;

import java.util.regex.Pattern;

public class Validator {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w.-]+@[\\w.-]+\\.\\w{2,}$");

    public static String validateUsername(String username) {
        username = username.trim();
        if (username.isEmpty()) {
            return "Username cannot be empty";
        }
        return null;
    }

    public static String validateEmail(String email) {
        email = email.trim();
        if (email.isEmpty()) {
            return "Email cannot be empty";
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Invalid email format";
        }
        return null;
    }

    public static String validatePassword(String password) {
        password = password.trim();

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
}
