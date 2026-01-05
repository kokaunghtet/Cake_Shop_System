package com.cakeshopsystem.models;

public class PrebakedCake {

    private int prebakedCakeId;
    private Cake cake;
    private boolean isDiy = false;

    public PrebakedCake(){}

    public PrebakedCake(int prebakedCakeId, Cake cake, boolean isDiy) {
        this.prebakedCakeId = prebakedCakeId;
        this.cake = cake;
        this.isDiy = isDiy;
    }

    public int getPrebakedCakeId() {
        return prebakedCakeId;
    }

    public void setPrebakedCakeId(int prebakedCakeId) {
        this.prebakedCakeId = prebakedCakeId;
    }

    public Cake getCake() {
        return cake;
    }

    public void setCake(Cake cake) {
        this.cake = cake;
    }

    public boolean isDiy() {
        return isDiy;
    }

    public void setDiy(boolean diy) {
        isDiy = diy;
    }

    @Override
    public String toString() {
        return "PrebakedCake{" +
                "prebakedCakeId=" + prebakedCakeId +
                ", cake=" + cake +
                ", isDiy=" + isDiy +
                '}';
    }
}
