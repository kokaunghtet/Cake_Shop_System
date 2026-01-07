package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.constants.Drinks_Size;

public class Drink {

    private int drinkId;
    private Drinks_Size size;
    private boolean isCold;
    private int productId;
    private Double sellPrice;

    public Drink(){}

    public Drink(int drinkId, Drinks_Size size, boolean isCold, int productId, Double sellPrice) {
        this.drinkId = drinkId;
        this.size = size;
        this.isCold = isCold;
        this.productId = productId;
        this.sellPrice = sellPrice;
    }

    public int getDrinkId() {
        return drinkId;
    }

    public void setDrinkId(int drinkId) {
        this.drinkId = drinkId;
    }

    public Drinks_Size getSize() {
        return size;
    }

    public void setSize(Drinks_Size size) {
        this.size = size;
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

    public Double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(Double sellPrice) {
        this.sellPrice = sellPrice;
    }

    @Override
    public String toString() {
        return "Drink{" +
                "drinkId=" + drinkId +
                ", size=" + size +
                ", isCold=" + isCold +
                ", productId=" + productId +
                ", sellPrice=" + sellPrice +
                '}';
    }
}
