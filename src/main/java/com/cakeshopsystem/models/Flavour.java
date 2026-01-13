package com.cakeshopsystem.models;

public class Flavour {
    private int flavourId;
    private String flavourName;
    private double price;

    public Flavour() {
    }

    public Flavour(int flavourId, String flavourName, double price) {
        this.flavourId = flavourId;
        this.flavourName = flavourName;
        this.price = price;
    }

    public int getFlavourId() {
        return flavourId;
    }

    public void setFlavourId(int flavourId) {
        this.flavourId = flavourId;
    }

    public String getFlavourName() {
        return flavourName;
    }

    public void setFlavourName(String flavourName) {
        this.flavourName = flavourName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Flavour{" +
                "flavourId=" + flavourId +
                ", flavourName='" + flavourName + '\'' +
                ", price=" + price +
                '}';
    }
}
