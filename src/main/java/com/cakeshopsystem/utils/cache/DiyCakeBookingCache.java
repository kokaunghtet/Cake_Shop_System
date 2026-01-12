package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.DiyCakeBooking;
import com.cakeshopsystem.utils.dao.DiyCakeBookingDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class DiyCakeBookingCache {

    private static final ObservableList<DiyCakeBooking> list = FXCollections.observableArrayList();
    private static final Map<Integer, DiyCakeBooking> byId = new HashMap<>();
    private static final Map<Integer, Integer> orderIdToBookingId = new HashMap<>();

    private DiyCakeBookingCache() {}

    // ===================== GETTERS =====================

    public static ObservableList<DiyCakeBooking> getList() {
        if (list.isEmpty()) refresh();
        return list;
    }

    public static DiyCakeBooking getById(int diyCakeBookingId) {
        if (byId.isEmpty()) refresh();
        return byId.get(diyCakeBookingId);
    }

    public static DiyCakeBooking getByOrderId(int orderId) {
        if (orderIdToBookingId.isEmpty()) refresh();
        Integer id = orderIdToBookingId.get(orderId);
        if (id == null) return null;
        return getById(id);
    }

    // ===================== REFRESH =====================

    public static void refresh() {
        list.clear();
        byId.clear();
        orderIdToBookingId.clear();

        for (DiyCakeBooking b : DiyCakeBookingDAO.getAllDiyCakeBooking()) {
            cacheOne(b);
        }
    }

    // ===================== CRUD WRAPPERS =====================

    public static boolean add(DiyCakeBooking booking) {
        if (booking == null) return false;

        boolean ok = DiyCakeBookingDAO.insertDiyCakeBooking(booking);
        if (ok) cacheOne(booking);

        return ok;
    }

    public static boolean update(DiyCakeBooking booking) {
        if (booking == null) return false;

        boolean ok = DiyCakeBookingDAO.updateDiyCakeBooking(booking);
        if (ok) cacheOne(booking);

        return ok;
    }

    public static boolean delete(int diyCakeBookingId) {
        boolean ok = DiyCakeBookingDAO.deleteDiyCakeBooking(diyCakeBookingId);
        if (ok) {
            DiyCakeBooking removed = byId.remove(diyCakeBookingId);
            list.removeIf(x -> x.getDiyCakeBookingId() == diyCakeBookingId);

            if (removed != null) {
                orderIdToBookingId.remove(removed.getOrderId());
            } else {
                rebuildOrderMapFromList();
            }
        }
        return ok;
    }

    // ===================== HELPERS =====================

    private static void cacheOne(DiyCakeBooking booking) {
        if (booking == null) return;

        int id = booking.getDiyCakeBookingId();

        int idx = findIndexById(id);
        if (idx >= 0) list.set(idx, booking);
        else list.add(booking);

        byId.put(id, booking);
        orderIdToBookingId.put(booking.getOrderId(), id);
    }

    private static int findIndexById(int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getDiyCakeBookingId() == id) return i;
        }
        return -1;
    }

    private static void rebuildOrderMapFromList() {
        orderIdToBookingId.clear();
        for (DiyCakeBooking b : list) {
            orderIdToBookingId.put(b.getOrderId(), b.getDiyCakeBookingId());
        }
    }
}
