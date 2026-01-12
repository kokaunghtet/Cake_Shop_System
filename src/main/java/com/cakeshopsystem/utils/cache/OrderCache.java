package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Order;
import com.cakeshopsystem.utils.dao.OrderDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class OrderCache {

    private static final ObservableList<Order> orderList = FXCollections.observableArrayList();
    private static final Map<Integer, Order> orderMap = new HashMap<>();

    private OrderCache() {}

    // ===================== Getters =====================

    public static ObservableList<Order> getOrderList() {
        if (orderList.isEmpty()) refreshOrder();
        return orderList;
    }

    public static Map<Integer, Order> getOrderMap() {
        if (orderMap.isEmpty()) refreshOrder();
        return orderMap;
    }

    public static Order getOrderById(int orderId) {
        if (orderMap.isEmpty()) refreshOrder();
        return orderMap.get(orderId);
    }

    // ===================== Refresh =====================

    public static void refreshOrder() {
        orderList.clear();
        orderMap.clear();

        for (Order order : OrderDAO.getAllOrder()) {
            cacheOrder(order);
        }
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addOrder(Order order) {
        if (order == null) return false;

        boolean ok = OrderDAO.insertOrder(order);
        if (ok) cacheOrder(order);

        return ok;
    }

    public static boolean updateOrder(Order order) {
        if (order == null) return false;

        boolean ok = OrderDAO.updateOrder(order);
        if (ok) cacheOrder(order);

        return ok;
    }

    public static boolean updateOrderMemberId(int orderId, Integer memberId) {
        boolean ok = OrderDAO.updateOrderMemberId(orderId, memberId);
        if (ok) {
            Order cached = orderMap.get(orderId);
            if (cached != null) cached.setMemberId(memberId);
        }
        return ok;
    }

    public static boolean deleteOrder(int orderId) {
        boolean ok = OrderDAO.deleteOrder(orderId);
        if (ok) {
            orderMap.remove(orderId);
            orderList.removeIf(o -> o.getOrderId() == orderId);
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cacheOrder(Order order) {
        if (order == null) return;

        int orderId = order.getOrderId();

        int idx = findIndexById(orderId);
        if (idx >= 0) orderList.set(idx, order);
        else orderList.add(order);

        orderMap.put(orderId, order);
    }

    private static int findIndexById(int orderId) {
        for (int i = 0; i < orderList.size(); i++) {
            if (orderList.get(i).getOrderId() == orderId) return i;
        }
        return -1;
    }
}
