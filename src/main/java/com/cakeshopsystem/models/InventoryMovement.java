package com.cakeshopsystem.models;

import java.time.LocalDateTime;

public class InventoryMovement {
    private int inventoryMovementId;
    private int inventoryId;
    private InventoryMovement movementType;
    private int quantityChange;
    private Integer orderItemId;
    private Integer userId;
    private LocalDateTime moveAt = LocalDateTime.now();

    public InventoryMovement() {
    }

    public InventoryMovement(int inventoryMovementId, int inventoryId, InventoryMovement movementType, int quantityChange, Integer orderItemId, Integer userId, LocalDateTime moveAt) {
        this.inventoryMovementId = inventoryMovementId;
        this.inventoryId = inventoryId;
        this.movementType = movementType;
        this.quantityChange = quantityChange;
        this.orderItemId = orderItemId;
        this.userId = userId;
        this.moveAt = moveAt;
    }

    public int getInventoryMovementId() {
        return inventoryMovementId;
    }

    public void setInventoryMovementId(int inventoryMovementId) {
        this.inventoryMovementId = inventoryMovementId;
    }

    public int getInventoryId() {
        return inventoryId;
    }

    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }

    public InventoryMovement getMovementType() {
        return movementType;
    }

    public void setMovementType(InventoryMovement movementType) {
        this.movementType = movementType;
    }

    public int getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(int quantityChange) {
        this.quantityChange = quantityChange;
    }

    public Integer getOrderItemId() {
        return orderItemId;
    }

    public void setOrderItemId(Integer orderItemId) {
        this.orderItemId = orderItemId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public LocalDateTime getMoveAt() {
        return moveAt;
    }

    public void setMoveAt(LocalDateTime moveAt) {
        this.moveAt = moveAt;
    }

    @Override
    public String toString() {
        return "InventoryMovement{" +
                "inventoryMovementId=" + inventoryMovementId +
                ", inventoryId=" + inventoryId +
                ", movementType=" + movementType +
                ", quantityChange=" + quantityChange +
                ", orderItemId=" + orderItemId +
                ", userId=" + userId +
                ", moveAt=" + moveAt +
                '}';
    }
}
