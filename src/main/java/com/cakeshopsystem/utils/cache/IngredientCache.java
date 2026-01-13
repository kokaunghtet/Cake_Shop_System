package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Ingredient;
import com.cakeshopsystem.utils.dao.IngredientDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class IngredientCache {

    private static final ObservableList<Ingredient> ingredientsList = FXCollections.observableArrayList();
    private static final Map<Integer, Ingredient> ingredientsMap = new HashMap<>();
    private static final Map<String, Integer> ingredientNameToIdMap = new HashMap<>();

    private IngredientCache() {}

    // ===================== get =====================

    public static ObservableList<Ingredient> getIngredientsList() {
        if (ingredientsList.isEmpty()) refreshIngredients();
        return ingredientsList;
    }

    public static Map<Integer, Ingredient> getIngredientsMap() {
        if (ingredientsMap.isEmpty()) refreshIngredients();
        return ingredientsMap;
    }

    public static Ingredient getIngredientById(int ingredientId) {
        if (ingredientsMap.isEmpty()) refreshIngredients();
        return ingredientsMap.get(ingredientId);
    }

    public static int getIngredientIdByName(String ingredientName) {
        if (ingredientName == null || ingredientName.isBlank()) return -1;
        if (ingredientNameToIdMap.isEmpty()) refreshIngredients();
        return ingredientNameToIdMap.getOrDefault(normalize(ingredientName), -1);
    }

    // ===================== refresh =====================

    public static void refreshIngredients() {
        ingredientsList.clear();
        ingredientsMap.clear();
        ingredientNameToIdMap.clear();

        ObservableList<Ingredient> fresh = IngredientDAO.getAllIngredients();
        for (Ingredient ingredient : fresh) {
            cacheIngredient(ingredient);
        }
    }

    // ===================== crud wrappers =====================

    public static boolean addIngredient(Ingredient ingredient) {
        if (ingredient == null) return false;

        boolean ok = IngredientDAO.insertIngredient(ingredient);
        if (ok) cacheIngredient(ingredient);

        return ok;
    }

    public static boolean updateIngredient(Ingredient ingredient) {
        if (ingredient == null) return false;

        boolean ok = IngredientDAO.updateIngredient(ingredient);
        if (ok) cacheIngredient(ingredient);

        return ok;
    }

    public static boolean deleteIngredient(int ingredientId) {
        boolean ok = IngredientDAO.deleteIngredient(ingredientId);
        if (ok) {
            Ingredient removed = ingredientsMap.remove(ingredientId);

            ingredientsList.removeIf(i -> i.getIngredientId() == ingredientId);

            if (removed != null) {
                ingredientNameToIdMap.remove(normalize(removed.getIngredientName()));
            } else {
                rebuildNameMapFromList();
            }
        }
        return ok;
    }

    // ===================== helpers =====================

    private static void cacheIngredient(Ingredient ingredient) {
        if (ingredient == null) return;

        int id = ingredient.getIngredientId();

        int idx = findIndexById(id);
        if (idx >= 0) ingredientsList.set(idx, ingredient);
        else ingredientsList.add(ingredient);

        ingredientsMap.put(id, ingredient);
        ingredientNameToIdMap.put(normalize(ingredient.getIngredientName()), id);
    }

    private static int findIndexById(int ingredientId) {
        for (int i = 0; i < ingredientsList.size(); i++) {
            if (ingredientsList.get(i).getIngredientId() == ingredientId) return i;
        }
        return -1;
    }

    private static String normalize(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }

    private static void rebuildNameMapFromList() {
        ingredientNameToIdMap.clear();
        for (Ingredient ing : ingredientsList) {
            ingredientNameToIdMap.put(normalize(ing.getIngredientName()), ing.getIngredientId());
        }
    }
}
