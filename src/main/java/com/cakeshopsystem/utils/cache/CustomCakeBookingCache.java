package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.CustomCakeBooking;
import com.cakeshopsystem.utils.dao.CustomCakeBookingDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class CustomCakeBookingCache {

    private static final ObservableList<CustomCakeBooking> list = FXCollections.observableArrayList();
    private static final Map<Integer, CustomCakeBooking> byId = new HashMap<>();
    private static final Map<Integer, Integer> orderIdToBookingId = new HashMap<>();

    private CustomCakeBookingCache() {}

    // ===================== GETTERS =====================

    public static ObservableList<CustomCakeBooking> getList() {
        if (list.isEmpty()) refresh();
        return list;
    }

    public static CustomCakeBooking getById(int customCakeBookingId) {
        if (byId.isEmpty()) refresh();
        return byId.get(customCakeBookingId);
    }

    public static CustomCakeBooking getByOrderId(int orderId) {
        if (orderIdToBookingId.isEmpty()) refresh();
        Integer bookingId = orderIdToBookingId.get(orderId);
        if (bookingId == null) return null;
        return getById(bookingId);
    }

    // ===================== REFRESH =====================

    public static void refresh() {
        list.clear();
        byId.clear();
        orderIdToBookingId.clear();

        for (CustomCakeBooking b : CustomCakeBookingDAO.getAllCustomCakeBooking()) {
            cacheOne(b);
        }
    }

    // ===================== CRUD WRAPPERS =====================

    public static boolean add(CustomCakeBooking booking) {
        if (booking == null) return false;

        boolean ok = CustomCakeBookingDAO.insertCustomCakeBooking(booking);
        if (ok) cacheOne(booking);

        return ok;
    }

    public static boolean update(CustomCakeBooking booking) {
        if (booking == null) return false;

        boolean ok = CustomCakeBookingDAO.updateCustomCakeBooking(booking);
        if (ok) cacheOne(booking);

        return ok;
    }

    public static boolean delete(int customCakeBookingId) {
        boolean ok = CustomCakeBookingDAO.deleteCustomCakeBooking(customCakeBookingId);
        if (ok) {
            CustomCakeBooking removed = byId.remove(customCakeBookingId);
            list.removeIf(x -> x.getCustomCakeOrderId() == customCakeBookingId);

            if (removed != null) {
                orderIdToBookingId.remove(removed.getOrderId());
            } else {
                rebuildOrderMapFromList();
            }
        }
        return ok;
    }

    // ===================== HELPERS =====================

    private static void cacheOne(CustomCakeBooking booking) {
        if (booking == null) return;

        int id = booking.getCustomCakeOrderId();

        int idx = findIndexById(id);
        if (idx >= 0) list.set(idx, booking);
        else list.add(booking);

        byId.put(id, booking);
        orderIdToBookingId.put(booking.getOrderId(), id);
    }

    private static int findIndexById(int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getCustomCakeOrderId() == id) return i;
        }
        return -1;
    }

    private static void rebuildOrderMapFromList() {
        orderIdToBookingId.clear();
        for (CustomCakeBooking b : list) {
            orderIdToBookingId.put(b.getOrderId(), b.getCustomCakeOrderId());
        }
    }
}
