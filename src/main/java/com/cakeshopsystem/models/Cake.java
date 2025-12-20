package com.cakeshopsystem.models;

public class Cake {

    private int cakeId;
    private int productId;
    private String flavour;
    private String filling;
    private int cakeTypeId;
    private int sizeInches;
    private String shape;
    private String decoration;

    public Cake(){}

    public Cake(int cakeId, int cakeTypeId, String decoration, String filling, String flavour, int productId, String shape, int sizeInches) {
        this.cakeId = cakeId;
        this.cakeTypeId = cakeTypeId;
        this.decoration = decoration;
        this.filling = filling;
        this.flavour = flavour;
        this.productId = productId;
        this.shape = shape;
        this.sizeInches = sizeInches;
    }

    public int getCakeId() {
        return cakeId;
    }

    public void setCakeId(int cakeId) {
        this.cakeId = cakeId;
    }

    public int getCakeTypeId() {
        return cakeTypeId;
    }

    public void setCakeTypeId(int cakeTypeId) {
        this.cakeTypeId = cakeTypeId;
    }

    public String getDecoration() {
        return decoration;
    }

    public void setDecoration(String decoration) {
        this.decoration = decoration;
    }

    public String getFilling() {
        return filling;
    }

    public void setFilling(String filling) {
        this.filling = filling;
    }

    public String getFlavour() {
        return flavour;
    }

    public void setFlavour(String flavour) {
        this.flavour = flavour;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getShape() {
        return shape;
    }

    public void setShape(String shape) {
        this.shape = shape;
    }

    public int getSizeInches() {
        return sizeInches;
    }

    public void setSizeInches(int sizeInches) {
        this.sizeInches = sizeInches;
    }

    @Override
    public String toString() {
        return "Cake{" +
                "cakeId=" + cakeId +
                ", productId=" + productId +
                ", flavour='" + flavour + '\'' +
                ", filling='" + filling + '\'' +
                ", cakeTypeId=" + cakeTypeId +
                ", sizeInches=" + sizeInches +
                ", shape='" + shape + '\'' +
                ", decoration='" + decoration + '\'' +
                '}';
    }
}
