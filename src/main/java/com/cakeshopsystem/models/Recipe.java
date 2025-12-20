package com.cakeshopsystem.models;

public class Recipe {

    private int recipeId;
    private Cake cake;
    private Ingredient ingredient;
    private int requiredQuantity;

    public Recipe(){}

    public Recipe(int recipeId, Cake cake, Ingredient ingredient, int requiredQuantity) {
        this.recipeId = recipeId;
        this.cake = cake;
        this.ingredient = ingredient;
        this.requiredQuantity = requiredQuantity;
    }

    public int getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(int recipeId) {
        this.recipeId = recipeId;
    }

    public Cake getCake() {
        return cake;
    }

    public void setCake(Cake cake) {
        this.cake = cake;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public void setIngredient(Ingredient ingredient) {
        this.ingredient = ingredient;
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
                "recipeId=" + recipeId +
                ", cake=" + cake +
                ", ingredient=" + ingredient +
                ", requiredQuantity=" + requiredQuantity +
                '}';
    }
}
