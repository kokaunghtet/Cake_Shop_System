package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.constants.Drinks_Size;

public class Drink {

    private int drinkId;
    private Drinks_Size size;
    private boolean isCold;
    private Product product;
    private Double sellPrice;

    public Drink(){}

    public Drink(int drinkId, Drinks_Size size, boolean isCold, Product product, Double sellPrice) {
        this.drinkId = drinkId;
        this.size = size;
        this.isCold = isCold;
        this.product = product;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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
                ", product=" + product +
                ", sellPrice=" + sellPrice +
                '}';
    }
}
