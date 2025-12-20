package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.constants.Booking_Status;

import java.time.LocalDate;

public class Booking {

    private int bookingId;
    private LocalDate bookingDate;
    private Booking_Status bookingStatus;

    public Booking(){}

    public Booking(int bookingId, LocalDate bookingDate, Booking_Status bookingStatus) {
        this.bookingId = bookingId;
        this.bookingDate = bookingDate;
        this.bookingStatus = bookingStatus;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Booking_Status getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(Booking_Status bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingId=" + bookingId +
                ", bookingDate=" + bookingDate +
                ", bookingStatus=" + bookingStatus +
                '}';
    }
}
