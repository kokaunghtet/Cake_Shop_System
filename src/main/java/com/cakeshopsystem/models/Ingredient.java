package com.cakeshopsystem.models;

public class Ingredient {

    private int ingredientId;
    private String ingerdientName;
    private String unit;

    public Ingredient(){}

    public Ingredient(String ingerdientName, int ingredientId, String unit) {
        this.ingerdientName = ingerdientName;
        this.ingredientId = ingredientId;
        this.unit = unit;
    }

    public String getIngerdientName() {
        return ingerdientName;
    }

    public void setIngerdientName(String ingerdientName) {
        this.ingerdientName = ingerdientName;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "ingerdientName='" + ingerdientName + '\'' +
                ", ingredientId=" + ingredientId +
                ", unit='" + unit + '\'' +
                '}';
    }
}
