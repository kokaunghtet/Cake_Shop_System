package com.cakeshopsystem.models;

public class Product {

    private int productId;
    private String productName;
    private int categoryId;
    private double sellPrice;
    private boolean isActive;
    private String imgPath;

    public Product(){}

    public Product(int categoryId, String imgPath, boolean isActive, int productId, String productName, double sellPrice) {
        this.categoryId = categoryId;
        this.imgPath = imgPath;
        this.isActive = isActive;
        this.productId = productId;
        this.productName = productName;
        this.sellPrice = sellPrice;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
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

    public double getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(double sellPrice) {
        this.sellPrice = sellPrice;
    }

    @Override
    public String toString() {
        return "Product{" +
                "categoryId=" + categoryId +
                ", productId=" + productId +
                ", productName='" + productName + '\'' +
                ", sellPrice=" + sellPrice +
                ", isActive=" + isActive +
                ", imgPath='" + imgPath + '\'' +
                '}';
    }
}
