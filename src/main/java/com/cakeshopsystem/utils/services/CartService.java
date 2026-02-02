package com.cakeshopsystem.utils.services;

import com.cakeshopsystem.models.CartItem;
import com.cakeshopsystem.models.Product;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.util.Objects;

import java.util.HashSet;

public class CartService {
    private static final CartService INSTANCE = new CartService();

    private final ObservableList<CartItem> items =
            FXCollections.observableArrayList(item -> new Observable[]{ item.quantityProperty() });

    private final ReadOnlyIntegerWrapper totalQuantity = new ReadOnlyIntegerWrapper(0);
    private final ReadOnlyIntegerWrapper distinctProductCount = new ReadOnlyIntegerWrapper(0);

    private CartService() {
        items.addListener((ListChangeListener<CartItem>) c -> {
            recomputeTotal();
            recomputeDistinct();
        });
    }

    public static CartService getInstance() {
        return INSTANCE;
    }

    public ObservableList<CartItem> getItems() {
        return items;
    }

    public ReadOnlyIntegerProperty totalQuantityProperty() {
        return totalQuantity.getReadOnlyProperty();
    }

    public int getTotalQuantity() {
        return totalQuantity.get();
    }

    public ReadOnlyIntegerProperty distinctProductCountProperty() {
        return distinctProductCount.getReadOnlyProperty();
    }

    public int getDistinctProductCount() {
        return distinctProductCount.get();
    }

    private void recomputeTotal() {
        int sum = 0;
        for (CartItem it : items) sum += it.getQuantity();
        totalQuantity.set(sum);
    }

    private void recomputeDistinct() {
        HashSet<Integer> unique = new HashSet<>();
        for (CartItem it : items) unique.add(it.getProductId());
        distinctProductCount.set(unique.size());
    }


    public CartItem findItem(int productId, String option) {
        for (CartItem it : items) {
            if (it.getProductId() == productId && Objects.equals(it.getOption(), option)) {
                return it;
            }
        }
        return null;
    }

    public CartItem findItem(int productId) {
        return findItem(productId, null);
    }

    public int getQuantity(int productId, String option) {
        CartItem it = findItem(productId, option);
        return it == null ? 0 : it.getQuantity();
    }

    public void addProduct(Product p) {
        addItem(p, null, p.getPrice(), 1);
    }

    public void addProduct(Product p, int qty) {
        addItem(p, null, p.getPrice(), qty);
    }

    public void addItem(Product p, String option, double unitPrice, int qty) {
        if (p == null || qty <= 0) return;

        CartItem it = findItem(p.getProductId(), option);
        if (it == null) {
            items.add(new CartItem(p.getProductId(), p.getProductName(), option, unitPrice, qty));
        } else {
            it.setQuantity(it.getQuantity() + qty);
        }
    }

    public void changeQty(CartItem it, int delta) {
        if (it == null) return;

        int newQty = it.getQuantity() + delta;
        if (newQty <= 0) {
            items.remove(it);
        } else {
            it.setQuantity(newQty);
        }
    }

    public void remove(CartItem it) {
        if (it != null) items.remove(it);
    }

    public void clear() {
        items.clear();
    }
}
