package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.constants.CakeShape;
import com.cakeshopsystem.utils.constants.CakeType;

public class Cake {

    private int cakeId;
    private int productId;
    private int flavourId;
    private int toppingId;
    private int sizeId;
    private CakeType cakeType;
    private CakeShape shape;
    private boolean isDiyAllowed = false;

    public Cake() {
    }

    public Cake(int cakeId, int productId, int flavourId, int toppingId, int sizeId, CakeType cakeType, CakeShape shape, boolean isDiyAllowed) {
        this.cakeId = cakeId;
        this.productId = productId;
        this.flavourId = flavourId;
        this.toppingId = toppingId;
        this.sizeId = sizeId;
        this.cakeType = cakeType;
        this.shape = shape;
        this.isDiyAllowed = isDiyAllowed;
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

    public int getFlavourId() {
        return flavourId;
    }

    public void setFlavourId(int flavourId) {
        this.flavourId = flavourId;
    }

    public int getToppingId() {
        return toppingId;
    }

    public void setToppingId(int toppingId) {
        this.toppingId = toppingId;
    }

    public int getSizeId() {
        return sizeId;
    }

    public void setSizeId(int sizeId) {
        this.sizeId = sizeId;
    }

    public CakeType getCakeType() {
        return cakeType;
    }

    public void setCakeType(CakeType cakeType) {
        this.cakeType = cakeType;
    }

    public CakeShape getShape() {
        return shape;
    }

    public void setShape(CakeShape shape) {
        this.shape = shape;
    }

    public boolean isDiyAllowed() {
        return isDiyAllowed;
    }

    public void setDiyAllowed(boolean diyAllowed) {
        isDiyAllowed = diyAllowed;
    }
}
