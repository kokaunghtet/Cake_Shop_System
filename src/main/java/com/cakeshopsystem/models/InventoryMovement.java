package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.constants.InventoryMovementType;

import java.time.LocalDateTime;

public class InventoryMovement {
    private int inventoryMovementId;
    private int inventoryId;
    private InventoryMovementType movementType;
    private int quantityChange;
    private Integer orderItemId;
    private Integer userId;
    private LocalDateTime movedAt = LocalDateTime.now();

    public InventoryMovement() {
    }

    public InventoryMovement(int inventoryMovementId,
                             int inventoryId,
                             InventoryMovementType movementType,
                             int quantityChange,
                             Integer orderItemId,
                             Integer userId,
                             LocalDateTime movedAt) {
        this.inventoryMovementId = inventoryMovementId;
        this.inventoryId = inventoryId;
        this.movementType = movementType;
        this.quantityChange = quantityChange;
        this.orderItemId = orderItemId;
        this.userId = userId;
        this.movedAt = movedAt;
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

    public InventoryMovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(InventoryMovementType movementType) {
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

    public LocalDateTime getMovedAt() {
        return movedAt;
    }

    public void setMovedAt(LocalDateTime movedAt) {
        this.movedAt = movedAt;
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
                ", movedAt=" + movedAt +
                '}';
    }
}
