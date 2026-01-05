package com.cakeshopsystem.utils.constants;

public enum Cake_Shape {
    CIRCLE("Circle"),
    SQUARE("Square"),
    HEART("Heart");

    private final String displayName;

    Cake_Shape(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
