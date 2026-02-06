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
    private static final Map<Integer, Integer> orderIdToDiyId = new HashMap<>();

    private static boolean loaded = false;

    private DiyCakeBookingCache() {}

    // ===================== GETTERS =====================

    public static ObservableList<DiyCakeBooking> getList() {
        if (!loaded) refresh();
        return list;
    }

    public static DiyCakeBooking getById(int diyCakeBookingId) {
        if (!loaded) refresh();
        return byId.get(diyCakeBookingId);
    }

    public static DiyCakeBooking getByOrderId(int orderId) {
        if (!loaded) refresh();
        Integer diyId = orderIdToDiyId.get(orderId);
        return (diyId == null) ? null : byId.get(diyId);
    }

    // ===================== REFRESH =====================

    public static void refresh() {
        list.clear();
        byId.clear();
        orderIdToDiyId.clear();

        for (DiyCakeBooking b : DiyCakeBookingDAO.getAllDiyCakeBookings()) {
            cacheOne(b);
        }

        loaded = true;
    }

    // ===================== READ-ONLY CACHE =====================
    // Option B reminder:
    // - Do NOT insert/update/delete diy_cake_bookings here.
    // - Insert must be done in OrderService transaction (reserve quota + insert booking).
    // - Cancel should be done via BookingDAO.updateBookingStatus(...) which releases quota.

    // ===================== HELPERS =====================

    private static void cacheOne(DiyCakeBooking booking) {
        if (booking == null) return;

        int id = booking.getDiyCakeBookingId();

        int idx = findIndexById(id);
        if (idx >= 0) list.set(idx, booking);
        else list.add(booking);

        byId.put(id, booking);
        orderIdToDiyId.put(booking.getOrderId(), id);
    }

    private static int findIndexById(int id) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getDiyCakeBookingId() == id) return i;
        }
        return -1;
    }
}
