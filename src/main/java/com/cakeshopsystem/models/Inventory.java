package com.cakeshopsystem.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Inventory {

    private int inventoryId;
    private Product product;
    private int quantity = 0;
    private LocalDate expDate;
    private LocalDateTime lastUpdated;

    public Inventory(){}

    public Inventory(int inventoryId, Product product, int quantity, LocalDate expDate, LocalDateTime lastUpdated) {
        this.inventoryId = inventoryId;
        this.product = product;
        this.quantity = quantity;
        this.expDate = expDate;
        this.lastUpdated = lastUpdated;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public LocalDate getExpDate() {
        return expDate;
    }

    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "inventoryId=" + inventoryId +
                ", product=" + product +
                ", quantity=" + quantity +
                ", expDate=" + expDate +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
