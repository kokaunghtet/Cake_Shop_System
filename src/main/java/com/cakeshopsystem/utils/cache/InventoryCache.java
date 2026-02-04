package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.*;

public class InventoryCache {

    private static final ObservableList<Inventory> all = FXCollections.observableArrayList();

    private static final Map<Integer, Integer> totalQtyByProduct = new HashMap<>();
    private static final Map<Integer, Integer> regularQtyByProduct = new HashMap<>();

    private static final ObservableList<Inventory> discountCandidates = FXCollections.observableArrayList();

    private static boolean loaded = false;

    private InventoryCache() {}

    private static void ensureLoaded() {
        if (!loaded) refresh();
    }

    public static void refresh() {
        all.clear();
        totalQtyByProduct.clear();
        regularQtyByProduct.clear();
        discountCandidates.clear();

        LocalDate today = LocalDate.now();

        for (Inventory inv : InventoryDAO.getAllInventory()) {
            all.add(inv);

            int qty = inv.getQuantity();
            if (qty <= 0) continue;

            LocalDate exp = inv.getExpDate();
            boolean notExpired = (exp == null) || !exp.isBefore(today);

            boolean isDiscountCandidate =
                    exp != null
                            && exp.equals(today)
                            && inv.getCreatedAt() != null
                            && inv.getCreatedAt().toLocalDate().isBefore(today);

            if (isDiscountCandidate) {
                discountCandidates.add(inv);
            }

            if (notExpired) {
                int pid = inv.getProductId();
                totalQtyByProduct.merge(pid, qty, Integer::sum);

                if (!isDiscountCandidate) {
                    regularQtyByProduct.merge(pid, qty, Integer::sum);
                }
            }
        }

        discountCandidates.sort(Comparator
                .comparing(Inventory::getExpDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparingInt(Inventory::getInventoryId));

        loaded = true;
    }

    public static int getTotalQtyByProductId(int productId) {
        ensureLoaded();
        return totalQtyByProduct.getOrDefault(productId, 0);
    }

    public static int getRegularQtyByProductId(int productId) {
        ensureLoaded();
        return regularQtyByProduct.getOrDefault(productId, 0);
    }

    public static ObservableList<Inventory> getDiscountCandidates() {
        ensureLoaded();
        return discountCandidates;
    }

    public static ObservableList<Inventory> getInventoryByProductId(int productId) {
        return InventoryDAO.getInventoryByProductId(productId);
    }
}
