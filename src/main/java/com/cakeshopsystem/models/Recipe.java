package com.cakeshopsystem.models;

public class Recipe {

    private int recipeId;
    private int cakeId;
    private int ingredientId;
    private double requiredQuantity;

    public Recipe() {
    }

    public Recipe(int recipeId, int cakeId, int ingredientId, double requiredQuantity) {
        this.recipeId = recipeId;
        this.cakeId = cakeId;
        this.ingredientId = ingredientId;
        this.requiredQuantity = requiredQuantity;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
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
        return "Recipe{" +
                "recipeId=" + recipeId +
                ", cakeId=" + cakeId +
                ", ingredientId=" + ingredientId +
                ", requiredQuantity=" + requiredQuantity +
                '}';
    }
}
