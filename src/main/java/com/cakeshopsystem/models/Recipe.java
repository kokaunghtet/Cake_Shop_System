package com.cakeshopsystem.models;

public class Recipe {

    private int recipeId;
    private int cakeId;
    private int ingredientId;
    private int requiredQuantity;

    public Recipe(){}

    public Recipe(int cakeId, int ingredientId, int recipeId, int requiredQuantity) {
        this.cakeId = cakeId;
        this.ingredientId = ingredientId;
        this.recipeId = recipeId;
        this.requiredQuantity = requiredQuantity;
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

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public int getRequiredQuantity() {
        return requiredQuantity;
    }

    public void setRequiredQuantity(int requiredQuantity) {
        this.requiredQuantity = requiredQuantity;
    }

    @Override
    public String toString() {
        return "Recipe{" +
                "cakeId=" + cakeId +
                ", recipeId=" + recipeId +
                ", ingredientId=" + ingredientId +
                ", requiredQuantity=" + requiredQuantity +
                '}';
    }
}
