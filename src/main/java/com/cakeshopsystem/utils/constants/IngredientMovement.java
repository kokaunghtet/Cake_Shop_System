package com.cakeshopsystem.utils.constants;

public enum IngredientMovement {
    ADD("Add"),
    SALE("Sale"),
    WASTE("Waste");

    private final String displayName;

    IngredientMovement(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
