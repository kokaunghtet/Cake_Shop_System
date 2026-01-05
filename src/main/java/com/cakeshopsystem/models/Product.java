package com.cakeshopsystem.models;

public class Product {

    private int productId;
    private String productName;
    private Category category;
    private double basePrice;
    private boolean isActive = true;
    private boolean trackInventory = true;
    private int shelfLifeDays;
    private String imgPath = null;

    public Product(){}

    public Product(int productId, String productName, Category category, double basePrice, boolean isActive, boolean trackInventory, int shelfLifeDays, String imgPath) {
        this.productId = productId;
        this.productName = productName;
        this.category = category;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
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

    public int getShelfLifeDays() {
        return shelfLifeDays;
    }

    public void setShelfLifeDays(int shelfLifeDays) {
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
                ", category=" + category +
                ", basePrice=" + basePrice +
                ", isActive=" + isActive +
                ", shelfLifeDays=" + shelfLifeDays +
                ", imgPath='" + imgPath + '\'' +
                '}';
    }
}
