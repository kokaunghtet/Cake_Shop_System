package com.cakeshopsystem.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class DiyCakeBooking {

    private int diyCakeBookingId;
    private int bookingId;
    private int memberId;
    private int cakeId;
    private int orderId;

    private LocalDate sessionDate;
    private LocalTime sessionStart;

    public DiyCakeBooking() {
    }

    public DiyCakeBooking(int diyCakeBookingId, int bookingId, int memberId, int cakeId, int orderId,
                          LocalDate sessionDate, LocalTime sessionStart) {
        this.diyCakeBookingId = diyCakeBookingId;
        this.bookingId = bookingId;
        this.memberId = memberId;
        this.cakeId = cakeId;
        this.orderId = orderId;
        this.sessionDate = sessionDate;
        this.sessionStart = sessionStart;
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

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getCakeId() {
        return cakeId;
    }

    public void setCakeId(int cakeId) {
        this.cakeId = cakeId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public LocalDate getSessionDate() {
        return sessionDate;
    }

    public void setSessionDate(LocalDate sessionDate) {
        this.sessionDate = sessionDate;
    }

    public LocalTime getSessionStart() {
        return sessionStart;
    }

    public void setSessionStart(LocalTime sessionStart) {
        this.sessionStart = sessionStart;
    }

    // convenience: your duration is fixed 2 hours
    public LocalTime getSessionEnd() {
        return (sessionStart == null) ? null : sessionStart.plusHours(2);
    }
}
