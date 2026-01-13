package com.cakeshopsystem.models;

public class CakeRecipe {
    private int cakeRecipeId;
    private int cakeId;
    private int ingredientId;
    private double requiredQuantity;

    public CakeRecipe() {
    }

    public CakeRecipe(int cakeRecipeId, int cakeId, int ingredientId, double requiredQuantity) {
        this.cakeRecipeId = cakeRecipeId;
        this.cakeId = cakeId;
        this.ingredientId = ingredientId;
        this.requiredQuantity = requiredQuantity;
    }

    public int getCakeRecipeId() {
        return cakeRecipeId;
    }

    public void setCakeRecipeId(int cakeRecipeId) {
        this.cakeRecipeId = cakeRecipeId;
    }

    public int getCakeId() {
        return cakeId;
    }

    public void setCakeId(int cakeId) {
        this.cakeId = cakeId;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public double getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(double requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    @Override
    public String toString() {
        return "CakeRecipe{" +
                "cakeRecipeId=" + cakeRecipeId +
                ", cakeId=" + cakeId +
                ", ingredientId=" + ingredientId +
                ", requiredQuantity=" + requiredQuantity +
                '}';
    }
}


