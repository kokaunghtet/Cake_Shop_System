package com.cakeshopsystem.models;

public class Accessory {

    private int accessoryId;
    private int productId;

    public Accessory(){}

    public Accessory(int accessoryId, int cakeId) {
        this.accessoryId = accessoryId;
        this.productId = cakeId;
    }

    public int getAccessoryId() {
        return accessoryId;
    }

    public void setAccessoryId(int accessoryId) {
        this.accessoryId = accessoryId;
    }

    public int getCakeId() {
        return productId;
    }

    public void setCakeId(int productId) {
        this.productId = productId;
    }

    @Override
    public String toString() {
        return "Accessory{" +
                "accessoryId=" + accessoryId +
                ", cakeId=" + productId +
                '}';
    }
}
