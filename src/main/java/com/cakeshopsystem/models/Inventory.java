package com.cakeshopsystem.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Inventory {

    private int inventoryId;
    private int productId;
    private int quantity = 0;
    private LocalDate expDate;
    private LocalDateTime updatedAt;

    public Inventory() {
    }

    public Inventory(int inventoryId, int productId, int quantity, LocalDate expDate, LocalDateTime updatedAt) {
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.quantity = quantity;
        this.expDate = expDate;
        this.updatedAt = updatedAt;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
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

    public LocalDate getExpDate() {
        return expDate;
    }

    public void setExpDate(LocalDate expDate) {
        this.expDate = expDate;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Inventory{" +
                "inventoryId=" + inventoryId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                ", expDate=" + expDate +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
