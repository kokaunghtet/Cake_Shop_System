package com.cakeshopsystem.utils.constants;

public enum InventoryMovementType {
    ADD("Add"),
    SALE("Sale"),
    WASTE("Waste");

    private final String displayName;

    InventoryMovementType(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
