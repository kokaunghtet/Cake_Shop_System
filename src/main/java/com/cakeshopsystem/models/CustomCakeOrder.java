package com.cakeshopsystem.models;

import java.time.LocalDate;

public class CustomCakeOrder {

    private int customCakeOrderId;
    private int orderId;
    private int customCakeId;
    private int bookingId;
    private LocalDate pickupDate;
    private String customMessage;
    private double additionPrice;

    public CustomCakeOrder() {
    }

    public CustomCakeOrder(int customCakeOrderId, int orderId, int customCakeId, int bookingId, LocalDate pickupDate, String customMessage, double additionPrice) {
        this.customCakeOrderId = customCakeOrderId;
        this.orderId = orderId;
        this.customCakeId = customCakeId;
        this.bookingId = bookingId;
        this.pickupDate = pickupDate;
        this.customMessage = customMessage;
        this.additionPrice = additionPrice;
    }

    public int getCustomCakeOrderId() {
        return customCakeOrderId;
    }

    public void setCustomCakeOrderId(int customCakeOrderId) {
        this.customCakeOrderId = customCakeOrderId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomCakeId() {
        return customCakeId;
    }

    public void setCustomCakeId(int customCakeId) {
        this.customCakeId = customCakeId;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    public double getAdditionPrice() {
        return additionPrice;
    }

    public void setAdditionPrice(double additionPrice) {
        this.additionPrice = additionPrice;
    }

    @Override
    public String toString() {
        return "CustomCakeOrder{" +
                "customCakeOrderId=" + customCakeOrderId +
                ", orderId=" + orderId +
                ", customCakeId=" + customCakeId +
                ", bookingId=" + bookingId +
                ", pickupDate=" + pickupDate +
                ", customMessage='" + customMessage + '\'' +
                ", additionPrice=" + additionPrice +
                '}';
    }
}
