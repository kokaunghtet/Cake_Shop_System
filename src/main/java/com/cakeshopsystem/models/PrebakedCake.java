package com.cakeshopsystem.models;

public class PrebakedCake {

    private int prebakedCakeId;
    private int cakeId;
    private boolean isDiy;

    public PrebakedCake(){}

    public PrebakedCake(int cakeId, boolean isDiy, int prebakedCakeId) {
        this.cakeId = cakeId;
        this.isDiy = isDiy;
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

    public int getPrebakedCakeId() {
        return prebakedCakeId;
    }

    public void setPrebakedCakeId(int prebakedCakeId) {
        this.prebakedCakeId = prebakedCakeId;
    }

    @Override
    public String toString() {
        return "PrebakedCake{" +
                "cakeId=" + cakeId +
                ", prebakedCakeId=" + prebakedCakeId +
                ", isDiy=" + isDiy +
                '}';
    }
}
