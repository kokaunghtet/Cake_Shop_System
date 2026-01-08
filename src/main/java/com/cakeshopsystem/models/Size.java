package com.cakeshopsystem.models;

public class Size {
    private int sizeId;
    private String sizeInches;
    private double price;

    public Size() {
    }

    public Size(int sizeId, String sizeInches, double price) {
        this.sizeId = sizeId;
        this.sizeInches = sizeInches;
        this.price = price;
    }

    public int getSizeId() {
        return sizeId;
    }

    public void setSizeId(int sizeId) {
        this.sizeId = sizeId;
    }

    public String getSizeInches() {
        return sizeInches;
    }

    public void setSizeInches(String sizeInches) {
        this.sizeInches = sizeInches;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Size{" +
                "sizeId=" + sizeId +
                ", sizeInches='" + sizeInches + '\'' +
                ", price=" + price +
                '}';
    }
}
