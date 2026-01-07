package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.constants.Cake_Shape;

public class Cake {

    private int cakeId;
    private int productId;
    private String flavour;
    private String filling;
    private int cakeTypeId;
    private int sizeInches;
    private Cake_Shape shape;
    private String decoration;

    public Cake(){}

    public Cake(int cakeId, int productId, String flavour, String filling, int cakeTypeId, int sizeInches, Cake_Shape shape, String decoration) {
        this.cakeId = cakeId;
        this.productId = productId;
        this.flavour = flavour;
        this.filling = filling;
        this.cakeTypeId = cakeTypeId;
        this.sizeInches = sizeInches;
        this.shape = shape;
        this.decoration = decoration;
    }

    public int getCakeId() {
        return cakeId;
    }

    public void setCakeId(int cakeId) {
        this.cakeId = cakeId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getFlavour() {
        return flavour;
    }

    public void setFlavour(String flavour) {
        this.flavour = flavour;
    }

    public String getFilling() {
        return filling;
    }

    public void setFilling(String filling) {
        this.filling = filling;
    }

    public int getCakeTypeId() {
        return cakeTypeId;
    }

    public void setCakeTypeId(int cakeTypeId) {
        this.cakeTypeId = cakeTypeId;
    }

    public int getSizeInches() {
        return sizeInches;
    }

    public void setSizeInches(int sizeInches) {
        this.sizeInches = sizeInches;
    }

    public Cake_Shape getShape() {
        return shape;
    }

    public void setShape(Cake_Shape shape) {
        this.shape = shape;
    }

    public String getDecoration() {
        return decoration;
    }

    public void setDecoration(String decoration) {
        this.decoration = decoration;
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
                ", shape=" + shape +
                ", decoration='" + decoration + '\'' +
                '}';
    }
}
