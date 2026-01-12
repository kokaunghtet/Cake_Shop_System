package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class InventoryCache {

    private static final ObservableList<Inventory> inventoryList = FXCollections.observableArrayList();
    private static final Map<Integer, Inventory> inventoryMap = new HashMap<>();

    private InventoryCache() {}

    // ===================== Getters =====================

    public static ObservableList<Inventory> getInventoryList() {
        if (inventoryList.isEmpty()) refreshInventory();
        return inventoryList;
    }

    public static Map<Integer, Inventory> getInventoryMap() {
        if (inventoryMap.isEmpty()) refreshInventory();
        return inventoryMap;
    }

    public static Inventory getInventoryById(int inventoryId) {
        if (inventoryMap.isEmpty()) refreshInventory();
        return inventoryMap.get(inventoryId);
    }

    // ⚠️ Better fetched from DAO (can be large)
    public static ObservableList<Inventory> getInventoryByProductId(int productId) {
        return InventoryDAO.getInventoryByProductId(productId);
    }

    // ===================== Refresh =====================

    public static void refreshInventory() {
        inventoryList.clear();
        inventoryMap.clear();

        for (Inventory inv : InventoryDAO.getAllInventory()) {
            cacheInventory(inv);
        }
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addInventory(Inventory inv) {
        if (inv == null) return false;

        boolean ok = InventoryDAO.insertInventory(inv);
        if (ok) cacheInventory(inv);

        return ok;
    }

    public static boolean updateInventory(Inventory inv) {
        if (inv == null) return false;

        boolean ok = InventoryDAO.updateInventory(inv);
        if (ok) cacheInventory(inv);

        return ok;
    }

    public static boolean deleteInventory(int inventoryId) {
        boolean ok = InventoryDAO.deleteInventory(inventoryId);
        if (ok) {
            inventoryMap.remove(inventoryId);
            inventoryList.removeIf(i -> i.getInventoryId() == inventoryId);
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cacheInventory(Inventory inv) {
        if (inv == null) return;

        int id = inv.getInventoryId();

        int idx = findIndexById(id);
        if (idx >= 0) inventoryList.set(idx, inv);
        else inventoryList.add(inv);

        inventoryMap.put(id, inv);
    }

    private static int findIndexById(int inventoryId) {
        for (int i = 0; i < inventoryList.size(); i++) {
            if (inventoryList.get(i).getInventoryId() == inventoryId) return i;
        }
        return -1;
    }
}
