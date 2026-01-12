package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Cake;
import com.cakeshopsystem.utils.constants.CakeType;
import com.cakeshopsystem.utils.dao.CakeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class CakeCache {

    private static final ObservableList<Cake> cakesList = FXCollections.observableArrayList();
    private static final Map<Integer, Cake> cakesMap = new HashMap<>();
    private static final Map<Integer, Integer> productIdToCakeIdMap = new HashMap<>();

    private CakeCache() {}

    // ===================== Getters =====================

    public static ObservableList<Cake> getCakesList() {
        if (cakesList.isEmpty()) refreshCake();
        return cakesList;
    }

    public static Map<Integer, Cake> getCakesMap() {
        if (cakesMap.isEmpty()) refreshCake();
        return cakesMap;
    }

    public static Cake getCakeById(int cakeId) {
        if (cakesMap.isEmpty()) refreshCake();
        return cakesMap.get(cakeId);
    }

    public static Cake getCakeByProductId(int productId) {
        if (productIdToCakeIdMap.isEmpty()) refreshCake();
        Integer cakeId = productIdToCakeIdMap.get(productId);
        return cakeId == null ? null : getCakeById(cakeId);
    }

    public static ObservableList<Cake> getCakesByType(CakeType cakeType) {
        ObservableList<Cake> result = FXCollections.observableArrayList();
        if (cakeType == null) return result;

        for (Cake cake : getCakesList()) {
            if (cakeType.equals(cake.getCakeType())) result.add(cake);
        }
        return result;
    }

    public static ObservableList<Cake> getDiyAllowedCakes() {
        ObservableList<Cake> result = FXCollections.observableArrayList();
        for (Cake cake : getCakesList()) {
            if (cake.isDiyAllowed()) result.add(cake);
        }
        return result;
    }

    // ===================== Refresh =====================

    public static void refreshCake() {
        cakesList.clear();
        cakesMap.clear();
        productIdToCakeIdMap.clear();

        for (Cake cake : CakeDAO.getAllCakes()) {
            cacheCake(cake);
        }
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addCake(Cake cake) {
        if (cake == null) return false;

        boolean ok = CakeDAO.insertCake(cake);
        if (ok) cacheCake(cake);

        return ok;
    }

    public static boolean updateCake(Cake cake) {
        if (cake == null) return false;

        Cake old = cakesMap.get(cake.getCakeId());

        boolean ok = CakeDAO.updateCake(cake);
        if (ok) {
            if (old != null) productIdToCakeIdMap.remove(old.getProductId());
            cacheCake(cake);
        }

        return ok;
    }

    public static boolean deleteCake(int cakeId) {
        boolean ok = CakeDAO.deleteCake(cakeId);
        if (ok) {
            Cake removed = cakesMap.remove(cakeId);
            cakesList.removeIf(c -> c.getCakeId() == cakeId);

            if (removed != null) {
                productIdToCakeIdMap.remove(removed.getProductId());
            } else {
                rebuildProductMapFromList();
            }
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cacheCake(Cake cake) {
        if (cake == null) return;

        int cakeId = cake.getCakeId();

        int idx = findIndexById(cakeId);
        if (idx >= 0) cakesList.set(idx, cake);
        else cakesList.add(cake);

        cakesMap.put(cakeId, cake);
        productIdToCakeIdMap.put(cake.getProductId(), cakeId);
    }

    private static int findIndexById(int cakeId) {
        for (int i = 0; i < cakesList.size(); i++) {
            if (cakesList.get(i).getCakeId() == cakeId) return i;
        }
        return -1;
    }

    private static void rebuildProductMapFromList() {
        productIdToCakeIdMap.clear();
        for (Cake cake : cakesList) {
            productIdToCakeIdMap.put(cake.getProductId(), cake.getCakeId());
        }
    }
}
