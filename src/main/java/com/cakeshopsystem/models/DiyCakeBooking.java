package com.cakeshopsystem.models;

import java.time.LocalTime;

public class DiyCakeBooking {

    private int diyCakeBookingId;
    private int bookingId;
    private int customerId;
    private int prebakedCakeId;
    private int orderId;
    private LocalTime startTime;
    private LocalTime endTime;
    private double serviceCharges;

    public DiyCakeBooking(){}

    public DiyCakeBooking(int bookingId, int customerId, int diyCakeBookingId, LocalTime endTime, int orderId, int prebakedCakeId, double serviceCharges, LocalTime startTime) {
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.diyCakeBookingId = diyCakeBookingId;
        this.endTime = endTime;
        this.orderId = orderId;
        this.prebakedCakeId = prebakedCakeId;
        this.serviceCharges = serviceCharges;
        this.startTime = startTime;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public int getDiyCakeBookingId() {
        return diyCakeBookingId;
    }

    public void setDiyCakeBookingId(int diyCakeBookingId) {
        this.diyCakeBookingId = diyCakeBookingId;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getPrebakedCakeId() {
        return prebakedCakeId;
    }

    public void setPrebakedCakeId(int prebakedCakeId) {
        this.prebakedCakeId = prebakedCakeId;
    }

    public double getServiceCharges() {
        return serviceCharges;
    }

    public void setServiceCharges(double serviceCharges) {
        this.serviceCharges = serviceCharges;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    @Override
    public String toString() {
        return "DiyCakeBooking{" +
                "bookingId=" + bookingId +
                ", diyCakeBookingId=" + diyCakeBookingId +
                ", customerId=" + customerId +
                ", prebakedCakeId=" + prebakedCakeId +
                ", orderId=" + orderId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", serviceCharges=" + serviceCharges +
                '}';
    }
}
