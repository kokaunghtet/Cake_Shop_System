package com.cakeshopsystem.models;

import java.time.LocalDate;

public class Inventory {

    private int inventoryId;
    private int productId;
    private int quantity;
    private LocalDate expDate;
    private LocalDate lastUpdated;

    public Inventory(){}

    public Inventory(LocalDate expDate, int inventoryId, LocalDate lastUpdated, int productId, int quantity) {
        this.expDate = expDate;
        this.inventoryId = inventoryId;
        this.lastUpdated = lastUpdated;
        this.productId = productId;
        this.quantity = quantity;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public LocalDate getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDate lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "expDate=" + expDate +
                ", inventoryId=" + inventoryId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
