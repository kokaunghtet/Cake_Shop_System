package com.cakeshopsystem.models;

public class Accessory {

    private int accessoryId;
    private int productId;

    public Accessory(){}

    public Accessory(int accessoryId, int productId) {
        this.accessoryId = accessoryId;
        this.productId = productId;
    }

    public int getAccessoryId() {
        return accessoryId;
    }

    public void setAccessoryId(int accessoryId) {
        this.accessoryId = accessoryId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "Accessory{" +
                "accessoryId=" + accessoryId +
                ", productId=" + productId +
                '}';
    }
}
