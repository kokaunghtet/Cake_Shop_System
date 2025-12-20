package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.constants.Cake_Shape;

public class Cake {

    private int cakeId;
    private Product product;
    private String flavour;
    private String filling;
    private CakeType cakeType;
    private int sizeInches;
    private Cake_Shape shape;
    private String decoration;

    public Cake(){}

    public Cake(int cakeId, Product product, String flavour, String filling, CakeType cakeType, int sizeInches, Cake_Shape shape, String decoration) {
        this.cakeId = cakeId;
        this.product = product;
        this.flavour = flavour;
        this.filling = filling;
        this.cakeType = cakeType;
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

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
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

    public CakeType getCakeType() {
        return cakeType;
    }

    public void setCakeType(CakeType cakeType) {
        this.cakeType = cakeType;
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
                ", product=" + product +
                ", flavour='" + flavour + '\'' +
                ", filling='" + filling + '\'' +
                ", cakeType=" + cakeType +
                ", sizeInches=" + sizeInches +
                ", shape='" + shape + '\'' +
                ", decoration='" + decoration + '\'' +
                '}';
    }
}
