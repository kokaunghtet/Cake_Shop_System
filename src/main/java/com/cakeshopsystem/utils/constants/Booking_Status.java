package com.cakeshopsystem.utils.constants;

public enum Booking_Status {
    PENDING("Pending"),
    COMFIRMED("Confirmed"),
    CANCELLED("Cancelled");

    private final String displayName;

    Booking_Status(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
