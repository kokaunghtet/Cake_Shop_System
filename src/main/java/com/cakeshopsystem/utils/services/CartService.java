package com.cakeshopsystem.utils.services;

import com.cakeshopsystem.models.CartItem;
import com.cakeshopsystem.models.Product;
import javafx.beans.Observable;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyIntegerWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Objects;

public class CartService {

    private static final CartService INSTANCE = new CartService();

    private final ObservableList<CartItem> items =
            FXCollections.observableArrayList(item -> new Observable[]{item.quantityProperty()});

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

    // improved distinct count (so special items don’t collide)
    private void recomputeDistinct() {
        HashSet<String> keys = new HashSet<>();
        for (CartItem it : items) keys.add(distinctKey(it));
        distinctProductCount.set(keys.size());
    }

    private String distinctKey(CartItem it) {
        String opt = Objects.toString(it.getOption(), "");
        String type = it.isCustomCake() ? "CUSTOM" : (it.isDiyBooking() ? "DIY" : "NORMAL");
        return it.getProductId() + "|" + opt + "|" + type;
    }

    public CartItem findItem(int productId, String option) {
        for (CartItem it : items) {
            if (it.getProductId() == productId && Objects.equals(it.getOption(), option)) {
                return it;
            }
        }
        return null;
    }

    public int getQuantity(int productId, String option) {
        CartItem it = findItem(productId, option);
        return (it == null) ? 0 : it.getQuantity();
    }

    public void addProduct(Product p) {
        addItem(p, null, p.getPrice(), 1);
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
        if (newQty <= 0) items.remove(it);
        else it.setQuantity(newQty);
    }

    public void remove(CartItem it) {
        if (it != null) items.remove(it);
    }

    public void clear() {
        items.clear();
    }

    // =================================================
    // Custom cake
    // =================================================
    public CartItem findCustomCakeItem() {
        for (CartItem it : items) {
            if (it.isCustomCake()) return it;
        }
        return null;
    }

    public void addOrReplaceCustomCake(
            Product customCakeProduct,
            String displayName,
            double unitPrice,
            int cakeId,
            LocalDate pickupDate,
            String message,
            int flavourId,
            Integer toppingId,
            int sizeId,
            com.cakeshopsystem.utils.constants.CakeShape shape
    ) {
        if (customCakeProduct == null) return;

        CartItem old = findCustomCakeItem();
        if (old != null) items.remove(old);

        CartItem it = new CartItem(customCakeProduct.getProductId(), displayName, null, unitPrice, 1);
        it.setCustomBooking(cakeId, pickupDate, message, flavourId, toppingId, sizeId, shape);

        items.add(it);
    }

    // =================================================
    // DIY booking (NEW)
    // =================================================
    public CartItem findDiyBookingItem() {
        for (CartItem it : items) {
            if (it.isDiyBooking()) return it;
        }
        return null;
    }

    public void addOrReplaceDiyBooking(
            Product diySessionProduct,   // <-- IMPORTANT: "DIY Session" product
            String displayName,
            double unitPrice,
            int cakeId,
            int memberId,
            LocalDate sessionDate,
            LocalTime sessionStart
    ) {
        if (diySessionProduct == null) return;

        CartItem old = findDiyBookingItem();
        if (old != null) items.remove(old);

        CartItem it = new CartItem(diySessionProduct.getProductId(), displayName, null, unitPrice, 1);
        it.setDiyBooking(cakeId, memberId, sessionDate, sessionStart);

        items.add(it);
    }
}