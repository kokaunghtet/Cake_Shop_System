package com.cakeshopsystem.models;

public class CustomCake {

    private int customCakeId;
    private Cake cake;

    public CustomCake(){}

    public CustomCake(int customCakeId, Cake cake) {
        this.customCakeId = customCakeId;
        this.cake = cake;
    }

    public int getCustomCakeId() {
        return customCakeId;
    }

    public void setCustomCakeId(int customCakeId) {
        this.customCakeId = customCakeId;
    }

    public Cake getCake() {
        return cake;
    }

    public void setCake(Cake cake) {
        this.cake = cake;
    }

    @Override
    public String toString() {
        return "CustomCake{" +
                "customCakeId=" + customCakeId +
                ", cake=" + cake +
                '}';
    }
}
