package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Topping;
import com.cakeshopsystem.utils.dao.ToppingDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class ToppingCache {

    private static final ObservableList<Topping> toppingsList = FXCollections.observableArrayList();
    private static final Map<Integer, Topping> toppingsMap = new HashMap<>();
    private static final Map<String, Integer> toppingNameToIdMap = new HashMap<>();

    private ToppingCache() {}

    // ===================== Cache Refresh =====================

    public static void refreshTopping() {
        toppingsList.clear();
        toppingsMap.clear();
        toppingNameToIdMap.clear();

        for (Topping topping : ToppingDAO.getAllToppings()) {
            cacheTopping(topping);
        }
    }

    // ===================== Getters =====================

    public static ObservableList<Topping> getToppingsList() {
        if (toppingsList.isEmpty()) refreshTopping();
        return toppingsList;
    }

    public static Map<Integer, Topping> getToppingsMap() {
        if (toppingsMap.isEmpty()) refreshTopping();
        return toppingsMap;
    }

    public static Topping getToppingById(int toppingId) {
        if (toppingsMap.isEmpty()) refreshTopping();
        return toppingsMap.get(toppingId);
    }

    public static int getToppingIdByName(String toppingName) {
        if (toppingNameToIdMap.isEmpty()) refreshTopping();
        return toppingNameToIdMap.getOrDefault(normalize(toppingName), -1);
    }

    public static String getToppingNameById(int toppingId) {
        Topping topping = getToppingById(toppingId);
        return topping == null ? null : topping.getToppingName();
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addTopping(Topping topping) {
        if (topping == null) return false;

        boolean ok = ToppingDAO.insertTopping(topping);
        if (ok) cacheTopping(topping);

        return ok;
    }

    public static boolean updateTopping(Topping topping) {
        if (topping == null) return false;

        boolean ok = ToppingDAO.updateTopping(topping);
        if (ok) {
            int idx = findIndexById(topping.getToppingId());
            if (idx >= 0) toppingsList.set(idx, topping);
            else toppingsList.add(topping);

            toppingsMap.put(topping.getToppingId(), topping);
            toppingNameToIdMap.put(normalize(topping.getToppingName()), topping.getToppingId());
        }

        return ok;
    }

    public static boolean deleteTopping(int toppingId) {
        boolean ok = ToppingDAO.deleteTopping(toppingId);
        if (ok) {
            Topping removed = toppingsMap.remove(toppingId);
            toppingsList.removeIf(t -> t.getToppingId() == toppingId);

            if (removed != null) {
                toppingNameToIdMap.remove(normalize(removed.getToppingName()));
            } else {
                rebuildNameMapFromList();
            }
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cacheTopping(Topping topping) {
        if (topping == null) return;

        int id = topping.getToppingId();

        int idx = findIndexById(id);
        if (idx >= 0) toppingsList.set(idx, topping);
        else toppingsList.add(topping);

        toppingsMap.put(id, topping);
        toppingNameToIdMap.put(normalize(topping.getToppingName()), id);
    }

    private static int findIndexById(int toppingId) {
        for (int i = 0; i < toppingsList.size(); i++) {
            if (toppingsList.get(i).getToppingId() == toppingId) return i;
        }
        return -1;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static void rebuildNameMapFromList() {
        toppingNameToIdMap.clear();
        for (Topping topping : toppingsList) {
            toppingNameToIdMap.put(normalize(topping.getToppingName()), topping.getToppingId());
        }
    }
}
