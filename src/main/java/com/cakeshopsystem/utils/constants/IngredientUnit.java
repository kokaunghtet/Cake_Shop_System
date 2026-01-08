package com.cakeshopsystem.utils.constants;

public enum IngredientUnit {
    GRAM("Gram"),
    KILOGRAM("Kilogram"),
    LITER("Liter"),
    TEASPOON("Teaspoon"),
    TABLESPOON("Tablespoon"),
    YOLK("Yolk"),
    WHITE("White");

    private final String displayName;

    IngredientUnit(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
