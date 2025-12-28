package com.cakeshopsystem.utils;

import org.mindrot.jbcrypt.BCrypt;

public final class PasswordHasher {

    private static final int COST = 12; // 10-12 typical

    private PasswordHasher() {
    }

    public static String hash(String rawPassword) {
        if (rawPassword == null) throw new IllegalArgumentException("rawPassword is null");
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(COST));
    }

    public static boolean verify(String rawPassword, String storedHash) {
        if (rawPassword == null || storedHash == null) return false;
        try {
            return BCrypt.checkpw(rawPassword, storedHash);
        } catch (IllegalArgumentException e) {
            // happens if storedHash is not a valid bcrypt hash
            return false;
        }
    }

    public static boolean looksLikeBcrypt(String value) {
        if (value == null) return false;
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
