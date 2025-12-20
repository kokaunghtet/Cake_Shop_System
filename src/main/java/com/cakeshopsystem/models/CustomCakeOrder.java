package com.cakeshopsystem.models;

import java.time.LocalDate;

public class CustomCakeOrder {

    private int customCakeOrderId;
    private Order order;
    private CustomCake customCake;
    private Booking booking;
    private LocalDate pickupDate;
    private String customMessage;
    private double additionPrice;

    public CustomCakeOrder(){}

    public CustomCakeOrder(int customCakeOrderId, Order order, CustomCake customCake, Booking booking, LocalDate pickupDate, String customMessage, double additionPrice) {
        this.customCakeOrderId = customCakeOrderId;
        this.order = order;
        this.customCake = customCake;
        this.booking = booking;
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

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public CustomCake getCustomCake() {
        return customCake;
    }

    public void setCustomCake(CustomCake customCake) {
        this.customCake = customCake;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
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
                ", order=" + order +
                ", customCake=" + customCake +
                ", booking=" + booking +
                ", pickupDate=" + pickupDate +
                ", customMessage='" + customMessage + '\'' +
                ", additionPrice=" + additionPrice +
                '}';
    }
}
