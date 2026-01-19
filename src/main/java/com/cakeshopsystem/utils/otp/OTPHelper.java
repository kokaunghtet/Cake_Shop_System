package com.cakeshopsystem.utils.otp;

import com.cakeshopsystem.utils.dao.OneTimePasswordDAO;

import java.security.SecureRandom;

public final class OTPHelper {

    private static final SecureRandom RNG = new SecureRandom();
    public static final int DEFAULT_LENGTH = 6;

    private OTPHelper() {}

    public static String generateOTP() {
        int n = RNG.nextInt(1_000_000);        // 0..999999
        return String.format("%0" + DEFAULT_LENGTH + "d", n);
    }

    public static boolean isValidFormat(String otp) {
        if (otp == null) return false;
        otp = otp.trim();
        if (otp.length() != DEFAULT_LENGTH) return false;

        for (int i = 0; i < otp.length(); i++) {
            if (!Character.isDigit(otp.charAt(i))) return false;
        }
        return true;
    }

    public static boolean verifyOTP(int userId, String otpVal) {
        if (!isValidFormat(otpVal)) return false;
        return OneTimePasswordDAO.isOtpValid(userId, otpVal.trim());
    }

    public static void main(String[] args) {
        System.out.println("OTP: " + generateOTP());
    }
}
