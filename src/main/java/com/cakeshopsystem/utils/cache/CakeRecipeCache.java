package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.CakeRecipe;
import com.cakeshopsystem.utils.dao.CakeRecipeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class CakeRecipeCache {

    private static final ObservableList<CakeRecipe> cakeRecipeList = FXCollections.observableArrayList();
    private static final Map<Integer, CakeRecipe> cakeRecipeMap = new HashMap<>();
    private static final Map<String, Integer> cakeIngredientToIdMap = new HashMap<>();

    private CakeRecipeCache() {}

    // ===================== Getters =====================

    public static ObservableList<CakeRecipe> getCakeRecipeList() {
        if (cakeRecipeList.isEmpty()) refreshCakeRecipe();
        return cakeRecipeList;
    }

    public static Map<Integer, CakeRecipe> getCakeRecipeMap() {
        if (cakeRecipeMap.isEmpty()) refreshCakeRecipe();
        return cakeRecipeMap;
    }

    public static CakeRecipe getCakeRecipeById(int cakeRecipeId) {
        if (cakeRecipeMap.isEmpty()) refreshCakeRecipe();
        return cakeRecipeMap.get(cakeRecipeId);
    }

    public static int getCakeRecipeIdByCakeAndIngredient(int cakeId, int ingredientId) {
        if (cakeIngredientToIdMap.isEmpty()) refreshCakeRecipe();
        return cakeIngredientToIdMap.getOrDefault(key(cakeId, ingredientId), -1);
    }

    public static ObservableList<CakeRecipe> getCakeRecipeByCakeId(int cakeId) {
        if (cakeRecipeList.isEmpty()) refreshCakeRecipe();

        ObservableList<CakeRecipe> list = FXCollections.observableArrayList();
        for (CakeRecipe cr : cakeRecipeList) {
            if (cr.getCakeId() == cakeId) list.add(cr);
        }
        return list;
    }

    // ===================== Refresh =====================

    public static void refreshCakeRecipe() {
        cakeRecipeList.clear();
        cakeRecipeMap.clear();
        cakeIngredientToIdMap.clear();

        for (CakeRecipe cr : CakeRecipeDAO.getAllCakeRecipe()) {
            cacheCakeRecipe(cr);
        }
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addCakeRecipe(CakeRecipe cakeRecipe) {
        if (cakeRecipe == null) return false;

        boolean ok = CakeRecipeDAO.insertCakeRecipe(cakeRecipe);
        if (ok) cacheCakeRecipe(cakeRecipe);
        return ok;
    }

    public static boolean updateCakeRecipe(CakeRecipe cakeRecipe) {
        if (cakeRecipe == null) return false;

        boolean ok = CakeRecipeDAO.updateCakeRecipe(cakeRecipe);
        if (ok) cacheCakeRecipe(cakeRecipe);
        return ok;
    }

    public static boolean deleteCakeRecipe(int cakeRecipeId) {
        boolean ok = CakeRecipeDAO.deleteCakeRecipe(cakeRecipeId);
        if (ok) {
            CakeRecipe removed = cakeRecipeMap.remove(cakeRecipeId);
            cakeRecipeList.removeIf(cr -> cr.getCakeRecipeId() == cakeRecipeId);

            if (removed != null) {
                cakeIngredientToIdMap.remove(key(removed.getCakeId(), removed.getIngredientId()));
            } else {
                rebuildCakeIngredientMapFromList();
            }
        }
        return ok;
    }

    // (Optional helper) delete all recipe rows for one cake
    public static boolean deleteCakeRecipeByCakeId(int cakeId) {
        boolean ok = CakeRecipeDAO.deleteCakeRecipeByCakeId(cakeId);
        if (ok) {
            cakeRecipeList.removeIf(cr -> cr.getCakeId() == cakeId);
            cakeRecipeMap.entrySet().removeIf(e -> e.getValue().getCakeId() == cakeId);
            rebuildCakeIngredientMapFromList();
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cacheCakeRecipe(CakeRecipe cr) {
        if (cr == null) return;

        int id = cr.getCakeRecipeId();

        // list (replace if exists)
        int idx = findIndexById(id);
        if (idx >= 0) cakeRecipeList.set(idx, cr);
        else cakeRecipeList.add(cr);

        // maps
        cakeRecipeMap.put(id, cr);
        cakeIngredientToIdMap.put(key(cr.getCakeId(), cr.getIngredientId()), id);
    }

    private static int findIndexById(int cakeRecipeId) {
        for (int i = 0; i < cakeRecipeList.size(); i++) {
            if (cakeRecipeList.get(i).getCakeRecipeId() == cakeRecipeId) return i;
        }
        return -1;
    }

    private static String key(int cakeId, int ingredientId) {
        return cakeId + ":" + ingredientId;
    }

    private static void rebuildCakeIngredientMapFromList() {
        cakeIngredientToIdMap.clear();
        for (CakeRecipe cr : cakeRecipeList) {
            cakeIngredientToIdMap.put(key(cr.getCakeId(), cr.getIngredientId()), cr.getCakeRecipeId());
        }
    }
}
