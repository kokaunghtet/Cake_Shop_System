package com.cakeshopsystem.models;

import com.cakeshopsystem.utils.constants.Ingredient_Unit;

public class Ingredient {

    private int ingredientId;
    private String ingredientName;
    private Ingredient_Unit unit;

    public Ingredient(){}

    public Ingredient(String ingredientName, int ingredientId, Ingredient_Unit unit) {
        this.ingredientName = ingredientName;
        this.ingredientId = ingredientId;
        this.unit = unit;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public Ingredient_Unit getUnit() {
        return unit;
    }

    public void setUnit(Ingredient_Unit unit) {
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "Ingredient{" +
                "ingredientName='" + ingredientName + '\'' +
                ", ingredientId=" + ingredientId +
                ", unit='" + unit + '\'' +
                '}';
    }
}
