package com.cakeshopsystem.models;

import java.time.LocalDate;

public class Booking {

    private int bookingId;
    private LocalDate bookingDate;
    private String bookingStatus;

    public Booking(){}

    public Booking(LocalDate bookingDate, int bookingId, String bookingStatus) {
        this.bookingDate = bookingDate;
        this.bookingId = bookingId;
        this.bookingStatus = bookingStatus;
    }

    public LocalDate getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(LocalDate bookingDate) {
        this.bookingDate = bookingDate;
    }

    public int getBookingId() {
        return bookingId;
    }

    public void setBookingId(int bookingId) {
        this.bookingId = bookingId;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    @Override
    public String toString() {
        return "Booking{" +
                "bookingDate=" + bookingDate +
                ", bookingId=" + bookingId +
                ", bookingStatus='" + bookingStatus + '\'' +
                '}';
    }
}
