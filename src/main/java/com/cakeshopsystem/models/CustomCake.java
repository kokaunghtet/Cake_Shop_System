package com.cakeshopsystem.models;

public class CustomCake {

    private int customCakeId;
    private int cakeId;

    public CustomCake(){}

    public CustomCake(int cakeId, int customCakeId) {
        this.cakeId = cakeId;
        this.customCakeId = customCakeId;
    }

    public int getCakeId() {
        return cakeId;
    }

    public void setCakeId(int cakeId) {
        this.cakeId = cakeId;
    }

    public int getCustomCakeId() {
        return customCakeId;
    }

    public void setCustomCakeId(int customCakeId) {
        this.customCakeId = customCakeId;
    }

    @Override
    public String toString() {
        return "CustomCake{" +
                "cakeId=" + cakeId +
                ", customCakeId=" + customCakeId +
                '}';
    }
}
