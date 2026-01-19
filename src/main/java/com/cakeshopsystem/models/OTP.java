package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.otp.OTPHelper;

import java.time.LocalDateTime;

public class OTP {
    private String code;
    private int userId;
    private LocalDateTime expireAt;

    public OTP(int userId) {
        this.code = OTPHelper.generateOTP();
        this.userId = userId;
        this.expireAt = LocalDateTime.now().plusMinutes(5);
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }
}
