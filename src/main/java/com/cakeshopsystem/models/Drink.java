package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.constants.Drinks_Size;

public class Drink {

    private int drinkId;
    private Drinks_Size size;
    private boolean isCold;
    private Product product;

    public Drink(){}

    public Drink(int drinkId, Drinks_Size size, boolean isCold, Product product) {
        this.drinkId = drinkId;
        this.size = size;
        this.isCold = isCold;
        this.product = product;
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

    @Override
    public String toString() {
        return "Drink{" +
                "drinkId=" + drinkId +
                ", size=" + size +
                ", isCold=" + isCold +
                ", product=" + product +
                '}';
    }
}
