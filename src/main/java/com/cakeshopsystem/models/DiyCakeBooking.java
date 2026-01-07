package com.cakeshopsystem.models;

import java.time.LocalDateTime;

public class DiyCakeBooking {

    private int diyCakeBookingId;
    private int bookingId;
    private int customerId;
    private int prebakedCakeId;
    private int orderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private double serviceCharges;

    public DiyCakeBooking() {
    }

    public DiyCakeBooking(int diyCakeBookingId, int bookingId, int customerId, int prebakedCakeId, int orderId, LocalDateTime startTime, LocalDateTime endTime, double serviceCharges) {
        this.diyCakeBookingId = diyCakeBookingId;
        this.bookingId = bookingId;
        this.customerId = customerId;
        this.prebakedCakeId = prebakedCakeId;
        this.orderId = orderId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.serviceCharges = serviceCharges;
    }

    public int getDiyCakeBookingId() {
        return diyCakeBookingId;
    }

    public void setDiyCakeBookingId(int diyCakeBookingId) {
        this.diyCakeBookingId = diyCakeBookingId;
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

    public int getPrebakedCakeId() {
        return prebakedCakeId;
    }

    public void setPrebakedCakeId(int prebakedCakeId) {
        this.prebakedCakeId = prebakedCakeId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public double getServiceCharges() {
        return serviceCharges;
    }

    public void setServiceCharges(double serviceCharges) {
        this.serviceCharges = serviceCharges;
    }

    @Override
    public String toString() {
        return "DiyCakeBooking{" +
                "diyCakeBookingId=" + diyCakeBookingId +
                ", bookingId=" + bookingId +
                ", customerId=" + customerId +
                ", prebakedCakeId=" + prebakedCakeId +
                ", orderId=" + orderId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", serviceCharges=" + serviceCharges +
                '}';
    }
}
