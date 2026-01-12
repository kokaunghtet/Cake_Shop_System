package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Drink;
import com.cakeshopsystem.utils.dao.DrinkDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class DrinkCache {

    private static final ObservableList<Drink> drinksList = FXCollections.observableArrayList();
    private static final Map<Integer, Drink> drinksMap = new HashMap<>();
    private static final Map<String, Integer> productColdKeyToDrinkIdMap = new HashMap<>();

    private DrinkCache() {}

    // ===================== Getters =====================

    public static ObservableList<Drink> getDrinksList() {
        if (drinksList.isEmpty()) refreshDrinks();
        return drinksList;
    }

    public static Map<Integer, Drink> getDrinksMap() {
        if (drinksMap.isEmpty()) refreshDrinks();
        return drinksMap;
    }

    public static Drink getDrinkById(int drinkId) {
        if (drinksMap.isEmpty()) refreshDrinks();
        return drinksMap.get(drinkId);
    }

    public static Drink getDrinkByProductIdAndCold(int productId, boolean isCold) {
        if (productColdKeyToDrinkIdMap.isEmpty()) refreshDrinks();
        Integer drinkId = productColdKeyToDrinkIdMap.get(key(productId, isCold));
        return drinkId == null ? null : getDrinkById(drinkId);
    }

    public static int getDrinkIdByProductIdAndCold(int productId, boolean isCold) {
        if (productColdKeyToDrinkIdMap.isEmpty()) refreshDrinks();
        return productColdKeyToDrinkIdMap.getOrDefault(key(productId, isCold), -1);
    }

    public static ObservableList<Drink> getVariantsByProductId(int productId) {
        if (drinksList.isEmpty()) refreshDrinks();
        ObservableList<Drink> list = FXCollections.observableArrayList();
        for (Drink d : drinksList) {
            if (d.getProductId() == productId) list.add(d);
        }
        return list;
    }

    // ===================== Refresh =====================

    public static void refreshDrinks() {
        drinksList.clear();
        drinksMap.clear();
        productColdKeyToDrinkIdMap.clear();

        for (Drink drink : DrinkDAO.getAllDrinks()) {
            cacheDrink(drink);
        }
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addDrink(Drink drink) {
        if (drink == null) return false;

        boolean ok = DrinkDAO.insertDrink(drink);
        if (ok) cacheDrink(drink);

        return ok;
    }

    public static boolean updateDrink(Drink drink) {
        if (drink == null) return false;

        // remove old key mapping if productId/isCold changed
        Drink old = drinksMap.get(drink.getDrinkId());

        boolean ok = DrinkDAO.updateDrink(drink);
        if (ok) {
            if (old != null) {
                productColdKeyToDrinkIdMap.remove(key(old.getProductId(), old.isCold()));
            }
            cacheDrink(drink);
        }
        return ok;
    }

    public static boolean deleteDrink(int drinkId) {
        boolean ok = DrinkDAO.deleteDrink(drinkId);
        if (ok) {
            Drink removed = drinksMap.remove(drinkId);
            drinksList.removeIf(d -> d.getDrinkId() == drinkId);

            if (removed != null) {
                productColdKeyToDrinkIdMap.remove(key(removed.getProductId(), removed.isCold()));
            }
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cacheDrink(Drink drink) {
        if (drink == null) return;

        int id = drink.getDrinkId();

        int idx = findIndexById(id);
        if (idx >= 0) drinksList.set(idx, drink);
        else drinksList.add(drink);

        drinksMap.put(id, drink);
        productColdKeyToDrinkIdMap.put(key(drink.getProductId(), drink.isCold()), id);
    }

    private static int findIndexById(int drinkId) {
        for (int i = 0; i < drinksList.size(); i++) {
            if (drinksList.get(i).getDrinkId() == drinkId) return i;
        }
        return -1;
    }

    private static String key(int productId, boolean isCold) {
        return productId + "|" + (isCold ? 1 : 0);
    }
}
