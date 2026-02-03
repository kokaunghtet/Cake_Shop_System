package com.cakeshopsystem.models;

public class ProductSales {
    private final String name;
    private final int totalSold;

    public ProductSales(String name, int totalSold) {
        this.name = name;
        this.totalSold = totalSold;
    }

    public String getName() {
        return name;
    }

    public int getTotalSold() {
        return totalSold;
    }
}

