package com.cakeshopsystem.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public class OTPCodeHasher {
    private static final int SALT_LENGTH = 16;
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;

    public static String hashOtpCode(String otpCode, byte[] salt)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        PBEKeySpec spec = new PBEKeySpec(otpCode.toCharArray(), salt, ITERATIONS, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] hashed = factory.generateSecret(spec).getEncoded();
        return Base64.getEncoder().encodeToString(hashed);
    }

    public static byte[] generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    // returns "base64Salt:base64Hash"
    public static String generateSecureOtpCode(String otpCode)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] salt = generateSalt();
        String hashed = hashOtpCode(otpCode, salt);
        String encodedSalt = Base64.getEncoder().encodeToString(salt);
        return encodedSalt + ":" + hashed;
    }

    public static boolean verifyOtpCode(String inputOtpCode, String storedSaltAndHash)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        if (storedSaltAndHash == null) return false;

        String[] parts = storedSaltAndHash.split(":");
        if (parts.length != 2) return false;

        byte[] salt = Base64.getDecoder().decode(parts[0]);
        String expectedHash = parts[1];

        String inputHash = hashOtpCode(inputOtpCode, salt);
        return expectedHash.equals(inputHash);
    }
}
