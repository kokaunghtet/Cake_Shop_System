package com.cakeshopsystem.models;

public class Accessory {

    private int accessoryId;
    private Product product;

    public Accessory(){}

    public Accessory(int accessoryId, Product product) {
        this.accessoryId = accessoryId;
        this.product = product;
    }

    public int getAccessoryId() {
        return accessoryId;
    }

    public void setAccessoryId(int accessoryId) {
        this.accessoryId = accessoryId;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "Accessory{" +
                "accessoryId=" + accessoryId +
                ", product=" + product +
                '}';
    }
}
