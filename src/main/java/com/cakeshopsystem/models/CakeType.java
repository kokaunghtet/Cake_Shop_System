package com.cakeshopsystem.models;

public class CakeType {

    private int cakeTypeId;
    private String cakeTypeName;

    public CakeType(){}

    public CakeType(int cakeTypeId, String cakeTypeName) {
        this.cakeTypeId = cakeTypeId;
        this.cakeTypeName = cakeTypeName;
    }

    public int getCakeTypeId() {
        return cakeTypeId;
    }

    public void setCakeTypeId(int cakeTypeId) {
        this.cakeTypeId = cakeTypeId;
    }

    public String getCakeTypeName() {
        return cakeTypeName;
    }

    public void setCakeTypeName(String cakeTypeName) {
        this.cakeTypeName = cakeTypeName;
    }

    @Override
    public String toString() {
        return "CakeType{" +
                "cakeTypeId=" + cakeTypeId +
                ", cakeTypeName='" + cakeTypeName + '\'' +
                '}';
    }
}
