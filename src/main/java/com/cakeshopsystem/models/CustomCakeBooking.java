package com.cakeshopsystem.models;

import java.time.LocalDate;

public class CustomCakeBooking {

    private int customCakeBookingId;
    private int orderId;
    private int cakeId;
    private int bookingId;
    private LocalDate pickupDate;
    private String customMessage;

    public CustomCakeBooking() {
    }

    public CustomCakeBooking(int customCakeBookingId, int orderId, int cakeId, int bookingId, LocalDate pickupDate, String customMessage) {
        this.customCakeBookingId = customCakeBookingId;
        this.orderId = orderId;
        this.cakeId = cakeId;
        this.bookingId = bookingId;
        this.pickupDate = pickupDate;
        this.customMessage = customMessage;
    }

    public int getCustomCakeOrderId() {
        return customCakeBookingId;
    }

    public void setCustomCakeOrderId(int customCakeBookingId) {
        this.customCakeBookingId = customCakeBookingId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCustomCakeId() {
        return cakeId;
    }

    public void setCustomCakeId(int cakeId) {
        this.cakeId = cakeId;
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

    @Override
    public String toString() {
        return "CustomCakeOrder{" +
                "customCakeBookingId=" + customCakeBookingId +
                ", orderId=" + orderId +
                ", cakeId=" + cakeId +
                ", bookingId=" + bookingId +
                ", pickupDate=" + pickupDate +
                ", customMessage='" + customMessage +
                '}';
    }
}
