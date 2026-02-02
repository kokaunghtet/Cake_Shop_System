package com.cakeshopsystem.utils.constants;

public enum OrderOption {
    REGULAR("REGULAR"),
    DISCOUNT("DISCOUNT"),
    HOT("HOT"),
    COLD("COLD");

    private final String displayName;

    OrderOption(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
