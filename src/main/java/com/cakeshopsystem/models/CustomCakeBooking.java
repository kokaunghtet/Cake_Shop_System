package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.constants.CakeShape;

import java.time.LocalDate;

public class CustomCakeBooking {

    private int customCakeBookingId;
    private int orderId;
    private int cakeId;
    private Integer bookingId;
    private LocalDate pickupDate;

    private int flavourId;
    private Integer toppingId;
    private int sizeId;
    private CakeShape shape;

    private String customMessage;

    public CustomCakeBooking() {}

    public CustomCakeBooking(int customCakeBookingId,
                             int orderId,
                             int cakeId,
                             Integer bookingId,
                             LocalDate pickupDate,
                             int flavourId,
                             Integer toppingId,
                             int sizeId,
                             CakeShape shape,
                             String customMessage) {
        this.customCakeBookingId = customCakeBookingId;
        this.orderId = orderId;
        this.cakeId = cakeId;
        this.bookingId = bookingId;
        this.pickupDate = pickupDate;
        this.flavourId = flavourId;
        this.toppingId = toppingId;
        this.sizeId = sizeId;
        this.shape = shape;
        this.customMessage = customMessage;
    }

    public int getCustomCakeBookingId() {
        return customCakeBookingId;
    }

    public void setCustomCakeBookingId(int customCakeBookingId) {
        this.customCakeBookingId = customCakeBookingId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getCakeId() {
        return cakeId;
    }

    public void setCakeId(int cakeId) {
        this.cakeId = cakeId;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDate getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(LocalDate pickupDate) {
        this.pickupDate = pickupDate;
    }

    public int getFlavourId() {
        return flavourId;
    }

    public void setFlavourId(int flavourId) {
        this.flavourId = flavourId;
    }

    public Integer getToppingId() {
        return toppingId;
    }

    public void setToppingId(Integer toppingId) {
        this.toppingId = toppingId;
    }

    public int getSizeId() {
        return sizeId;
    }

    public void setSizeId(int sizeId) {
        this.sizeId = sizeId;
    }

    public CakeShape getShape() {
        return shape;
    }

    public void setShape(CakeShape shape) {
        this.shape = shape;
    }

    public String getCustomMessage() {
        return customMessage;
    }

    public void setCustomMessage(String customMessage) {
        this.customMessage = customMessage;
    }
}
