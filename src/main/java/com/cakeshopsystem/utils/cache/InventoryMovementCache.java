package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.InventoryMovement;
import com.cakeshopsystem.utils.dao.InventoryMovementDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class InventoryMovementCache {

    // ===================== Getters =====================

    private static final ObservableList<InventoryMovement> inventoryMovementList = FXCollections.observableArrayList();
    private static final Map<Integer, InventoryMovement> inventoryMovementMap = new HashMap<>();

    private InventoryMovementCache() {}

    public static ObservableList<InventoryMovement> getInventoryMovementList() {
        if (inventoryMovementList.isEmpty()) refreshInventoryMovement();
        return inventoryMovementList;
    }

    public static Map<Integer, InventoryMovement> getInventoryMovementMap() {
        if (inventoryMovementMap.isEmpty()) refreshInventoryMovement();
        return inventoryMovementMap;
    }

    public static InventoryMovement getInventoryMovementById(int inventoryMovementId) {
        if (inventoryMovementMap.isEmpty()) refreshInventoryMovement();
        return inventoryMovementMap.get(inventoryMovementId);
    }

    // Better to fetch by inventoryId/orderItemId from DAO because movements can be huge
    public static ObservableList<InventoryMovement> getInventoryMovementByInventoryId(int inventoryId) {
        return InventoryMovementDAO.getInventoryMovementByInventoryId(inventoryId);
    }

    public static ObservableList<InventoryMovement> getInventoryMovementByOrderItemId(int orderItemId) {
        return InventoryMovementDAO.getInventoryMovementByOrderItemId(orderItemId);
    }

    // ===================== Refresh =====================

    public static void refreshInventoryMovement() {
        inventoryMovementList.clear();
        inventoryMovementMap.clear();

        for (InventoryMovement mv : InventoryMovementDAO.getAllInventoryMovement()) {
            cacheInventoryMovement(mv);
        }
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addInventoryMovement(InventoryMovement mv) {
        if (mv == null) return false;

        boolean ok = InventoryMovementDAO.insertInventoryMovement(mv);
        if (ok) cacheInventoryMovement(mv);

        return ok;
    }

    public static boolean updateInventoryMovement(InventoryMovement mv) {
        if (mv == null) return false;

        boolean ok = InventoryMovementDAO.updateInventoryMovement(mv);
        if (ok) cacheInventoryMovement(mv);

        return ok;
    }

    public static boolean deleteInventoryMovement(int inventoryMovementId) {
        boolean ok = InventoryMovementDAO.deleteInventoryMovement(inventoryMovementId);
        if (ok) {
            inventoryMovementMap.remove(inventoryMovementId);
            inventoryMovementList.removeIf(m -> m.getInventoryMovementId() == inventoryMovementId);
        }
        return ok;
    }

    private static void cacheInventoryMovement(InventoryMovement mv) {
        if (mv == null) return;

        int id = mv.getInventoryMovementId();

        int idx = findIndexById(id);
        if (idx >= 0) inventoryMovementList.set(idx, mv);
        else inventoryMovementList.add(mv);

        inventoryMovementMap.put(id, mv);
    }

    private static int findIndexById(int id) {
        for (int i = 0; i < inventoryMovementList.size(); i++) {
            if (inventoryMovementList.get(i).getInventoryMovementId() == id) return i;
        }
        return -1;
    }
}
