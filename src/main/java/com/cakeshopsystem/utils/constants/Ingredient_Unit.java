package com.cakeshopsystem.utils.constants;

public enum Ingredient_Unit {
    GRAM("Gram"),
    KILOGRAM("Kilogram"),
    LITER("Liter"),
    TEASPOON("Teaspoon"),
    TABLESPOON("Tablespoon"),
    YOLK("Yolk"),
    WHITE("White");

    private final String displayName;

    Ingredient_Unit(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
