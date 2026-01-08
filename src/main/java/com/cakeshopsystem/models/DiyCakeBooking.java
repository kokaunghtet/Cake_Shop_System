package com.cakeshopsystem.models;

import java.time.LocalDateTime;

public class DiyCakeBooking {

    private int diyCakeBookingId;
    private int bookingId;
    private int memberId;
    private int cakeId;
    private int orderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public DiyCakeBooking() {
    }

    public DiyCakeBooking(int diyCakeBookingId, int bookingId, int memberId, int cakeId, int orderId, LocalDateTime startTime, LocalDateTime endTime) {
        this.diyCakeBookingId = diyCakeBookingId;
        this.bookingId = bookingId;
        this.memberId = memberId;
        this.cakeId = cakeId;
        this.orderId = orderId;
        this.startTime = startTime;
        this.endTime = endTime;
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
        return memberId;
    }

    public void setCustomerId(int memberId) {
        this.memberId = memberId;
    }

    public int getPrebakedCakeId() {
        return cakeId;
    }

    public void setPrebakedCakeId(int cakeId) {
        this.cakeId = cakeId;
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

    @Override
    public String toString() {
        return "DiyCakeBooking{" +
                "diyCakeBookingId=" + diyCakeBookingId +
                ", bookingId=" + bookingId +
                ", memberId=" + memberId +
                ", cakeId=" + cakeId +
                ", orderId=" + orderId +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
