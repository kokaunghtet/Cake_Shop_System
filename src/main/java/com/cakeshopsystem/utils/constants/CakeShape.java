package com.cakeshopsystem.utils.constants;

public enum CakeShape {
    CIRCLE("Circle"),
    SQUARE("Square"),
    HEART("Heart");

    private final String displayName;

    CakeShape(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
