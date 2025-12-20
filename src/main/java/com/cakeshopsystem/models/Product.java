package com.cakeshopsystem.models;

public class Product {

    private int productId;
    private String productName;
    private Category category;
    private double sellPrice;
    private boolean isActive = true;
    private String imgPath = null;

    public Product(){}

    public Product(int productId, String productName, Category category, double sellPrice, boolean isActive, String imgPath) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
        this.sellPrice = sellPrice;
        this.isActive = isActive;
        this.imgPath = imgPath;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", productName='" + productName + '\'' +
                ", category=" + category +
                ", sellPrice=" + sellPrice +
                ", isActive=" + isActive +
                ", imgPath='" + imgPath + '\'' +
                '}';
    }
}
