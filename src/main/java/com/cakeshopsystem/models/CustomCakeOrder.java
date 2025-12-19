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

    public CustomCakeOrder(){}

    public CustomCakeOrder(double additionPrice, int bookingId, int customCakeId, int customCakeOrderId, String customMessage, int orderId, LocalDate pickupDate) {
        this.additionPrice = additionPrice;
        this.bookingId = bookingId;
        this.customCakeId = customCakeId;
        this.customCakeOrderId = customCakeOrderId;
        this.customMessage = customMessage;
        this.orderId = orderId;
        this.pickupDate = pickupDate;
    }

    public double getAdditionPrice() {
        return additionPrice;
    }

    public void setAdditionPrice(double additionPrice) {
        this.additionPrice = additionPrice;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getCustomCakeId() {
        return customCakeId;
    }

    public void setCustomCakeId(int customCakeId) {
        this.customCakeId = customCakeId;
    }

    public int getCustomCakeOrderId() {
        return customCakeOrderId;
    }

    public void setCustomCakeOrderId(int customCakeOrderId) {
        this.customCakeOrderId = customCakeOrderId;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    @Override
    public String toString() {
        return "CustomCakeOrder{" +
                "additionPrice=" + additionPrice +
                ", customCakeOrderId=" + customCakeOrderId +
                ", orderId=" + orderId +
                ", customCakeId=" + customCakeId +
                ", bookingId=" + bookingId +
                ", pickupDate=" + pickupDate +
                ", customMessage='" + customMessage + '\'' +
                '}';
    }
}
