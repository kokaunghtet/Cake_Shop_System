package com.cakeshopsystem.utils.constants;

public enum Drinks_Size {
    SMALL("Small"),
    MEDIUM("Medium"),
    LARGE("Large");

    private final String displayName;

    Drinks_Size(String displayName){
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
