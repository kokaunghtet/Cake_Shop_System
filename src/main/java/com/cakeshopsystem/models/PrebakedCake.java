package com.cakeshopsystem.models;

public class PrebakedCake {

    private int prebakedCakeId;
    private int cakeId;
    private boolean isDiy = false;

    public PrebakedCake(){}

    public PrebakedCake(int prebakedCakeId, int cakeId, boolean isDiy) {
        this.prebakedCakeId = prebakedCakeId;
        this.cakeId = cakeId;
        this.isDiy = isDiy;
    }

    public int getPrebakedCakeId() {
        return prebakedCakeId;
    }

    public void setPrebakedCakeId(int prebakedCakeId) {
        this.prebakedCakeId = prebakedCakeId;
    }

    public int getCakeId() {
        return cakeId;
    }

    public void setCakeId(int cakeId) {
        this.cakeId = cakeId;
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
                ", cakeId=" + cakeId +
                ", isDiy=" + isDiy +
                '}';
    }
}
