package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.constants.BookingStatus;

import java.time.LocalDate;

public class Booking {

    private int bookingId;
    private LocalDate bookingDate;
    private BookingStatus bookingStatus = BookingStatus.PENDING;

    public Booking(){}

    public Booking(int bookingId, LocalDate bookingDate, BookingStatus bookingStatus) {
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

    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(BookingStatus bookingStatus) {
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
