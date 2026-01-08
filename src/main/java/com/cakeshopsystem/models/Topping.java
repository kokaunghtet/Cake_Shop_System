package com.cakeshopsystem.models;

public class Topping {
    private int toppingId;
    private String toppingName;
    private double price;

    public Topping() {
    }

    public Topping(int toppingId, String toppingName, double price) {
        this.toppingId = toppingId;
        this.toppingName = toppingName;
        this.price = price;
    }

    public int getToppingId() {
        return toppingId;
    }

    public void setToppingId(int toppingId) {
        this.toppingId = toppingId;
    }

    public String getToppingName() {
        return toppingName;
    }

    public void setToppingName(String toppingName) {
        this.toppingName = toppingName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Topping{" +
                "toppingId=" + toppingId +
                ", toppingName='" + toppingName + '\'' +
                ", price=" + price +
                '}';
    }
}
