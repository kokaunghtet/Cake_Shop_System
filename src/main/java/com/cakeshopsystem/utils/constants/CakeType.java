package com.cakeshopsystem.utils.constants;

public enum CakeType {
    PREBAKED("Prebaked"),
    CUSTOM("Custom");

    private final String displayName;

    CakeType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
