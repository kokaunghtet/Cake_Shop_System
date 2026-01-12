package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.OrderItem;
import com.cakeshopsystem.utils.dao.OrderItemDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class OrderItemCache {

    // ===================== Getters =====================
    private static final ObservableList<OrderItem> orderItemList = FXCollections.observableArrayList();
    private static final Map<Integer, OrderItem> orderItemMap = new HashMap<>();

    private OrderItemCache() {}

    public static ObservableList<OrderItem> getOrderItemList() {
        if (orderItemList.isEmpty()) refreshOrderItem();
        return orderItemList;
    }

    public static Map<Integer, OrderItem> getOrderItemMap() {
        if (orderItemMap.isEmpty()) refreshOrderItem();
        return orderItemMap;
    }

    public static OrderItem getOrderItemById(int orderItemId) {
        if (orderItemMap.isEmpty()) refreshOrderItem();
        return orderItemMap.get(orderItemId);
    }

    public static ObservableList<OrderItem> getOrderItemByOrderId(int orderId) {
        // This one is better fetched from DAO because list could be huge
        return OrderItemDAO.getOrderItemByOrderId(orderId);
    }

    // ===================== Refresh =====================

    public static void refreshOrderItem() {
        orderItemList.clear();
        orderItemMap.clear();

        for (OrderItem item : OrderItemDAO.getAllOrderItem()) {
            cacheOrderItem(item);
        }
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addOrderItem(OrderItem item) {
        if (item == null) return false;

        boolean ok = OrderItemDAO.insertOrderItem(item);
        if (ok) cacheOrderItem(item);

        return ok;
    }

    public static boolean updateOrderItem(OrderItem item) {
        if (item == null) return false;

        boolean ok = OrderItemDAO.updateOrderItem(item);
        if (ok) cacheOrderItem(item);

        return ok;
    }

    public static boolean deleteOrderItem(int orderItemId) {
        boolean ok = OrderItemDAO.deleteOrderItem(orderItemId);
        if (ok) {
            orderItemMap.remove(orderItemId);
            orderItemList.removeIf(i -> i.getOrderItemId() == orderItemId);
        }
        return ok;
    }

    private static void cacheOrderItem(OrderItem item) {
        if (item == null) return;

        int id = item.getOrderItemId();

        int idx = findIndexById(id);
        if (idx >= 0) orderItemList.set(idx, item);
        else orderItemList.add(item);

        orderItemMap.put(id, item);
    }

    private static int findIndexById(int id) {
        for (int i = 0; i < orderItemList.size(); i++) {
            if (orderItemList.get(i).getOrderItemId() == id) return i;
        }
        return -1;
    }
}
