package com.cakeshopsystem.models;

public class Payment {

    private int paymentId;
    private String paymentName;
    private boolean isActive = true;

    public Payment(){}

    public Payment(int paymentId, String paymentName, boolean isActive) {
        this.paymentId = paymentId;
        this.paymentName = paymentName;
        this.isActive = isActive;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(int paymentId) {
        this.paymentId = paymentId;
    }

    public String getPaymentName() {
        return paymentName;
    }

    public void setPaymentName(String paymentName) {
        this.paymentName = paymentName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public String toString() {
        return "Payment{" +
                "paymentId=" + paymentId +
                ", paymentName='" + paymentName + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
