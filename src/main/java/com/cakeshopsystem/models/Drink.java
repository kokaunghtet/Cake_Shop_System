package com.cakeshopsystem.models;

public class Drink {

    private int drinkId;
    private int productId;
    private boolean isCold;
    private Double priceDelta;

    public Drink(){}

    public Drink(int drinkId, int productId, boolean isCold, Double priceDelta) {
        this.drinkId = drinkId;
        this.productId = productId;
        this.isCold = isCold;
        this.priceDelta = priceDelta;
    }

    public int getDrinkId() {
        return drinkId;
    }

    public void setDrinkId(int drinkId) {
        this.drinkId = drinkId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public boolean isCold() {
        return isCold;
    }

    public void setCold(boolean cold) {
        isCold = cold;
    }

    public Double getPriceDelta() {
        return priceDelta;
    }

    public void setPriceDelta(Double priceDelta) {
        this.priceDelta = priceDelta;
    }

    @Override
    public String toString() {
        return "Drink{" +
                "drinkId=" + drinkId +
                ", productId=" + productId +
                ", isCold=" + isCold +
                ", priceDelta=" + priceDelta +
                '}';
    }
}
