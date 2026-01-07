package com.cakeshopsystem.models;

public class Product {

    private int productId;
    private String productName;
    private int categoryId;
    private double basePrice;
    private boolean isActive = true;
    private boolean trackInventory = true;
    private Integer shelfLifeDays;
    private String imgPath = null;

    public Product() {
    }

    public Product(int productId, String productName, int categoryId, double basePrice, boolean isActive, boolean trackInventory, Integer shelfLifeDays, String imgPath) {
        this.productId = productId;
        this.productName = productName;
        this.categoryId = categoryId;
        this.basePrice = basePrice;
        this.isActive = isActive;
        this.trackInventory = trackInventory;
        this.shelfLifeDays = shelfLifeDays;
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

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isTrackInventory() {
        return trackInventory;
    }

    public void setTrackInventory(boolean trackInventory) {
        this.trackInventory = trackInventory;
    }

    public Integer getShelfLifeDays() {
        return shelfLifeDays;
    }

    public void setShelfLifeDays(Integer shelfLifeDays) {
        this.shelfLifeDays = shelfLifeDays;
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
                ", categoryId=" + categoryId +
                ", basePrice=" + basePrice +
                ", isActive=" + isActive +
                ", trackInventory=" + trackInventory +
                ", shelfLifeDays=" + shelfLifeDays +
                ", imgPath='" + imgPath + '\'' +
                '}';
    }
}
