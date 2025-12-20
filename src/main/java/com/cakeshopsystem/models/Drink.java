package com.cakeshopsystem.models;

public class Drink {

    private int drinkId;
    private int size;
    private boolean isCold;
    private int productId;

    public Drink(){}

    public Drink(int drinkId, boolean isCold, int productId, int size) {
        this.drinkId = drinkId;
        this.isCold = isCold;
        this.productId = productId;
        this.size = size;
    }

    public int getDrinkId() {
        return drinkId;
    }

    public void setDrinkId(int drinkId) {
        this.drinkId = drinkId;
    }

    public boolean isCold() {
        return isCold;
    }

    public void setCold(boolean cold) {
        isCold = cold;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return "Drink{" +
                "drinkId=" + drinkId +
                ", size=" + size +
                ", isCold=" + isCold +
                ", productId=" + productId +
                '}';
    }
}
