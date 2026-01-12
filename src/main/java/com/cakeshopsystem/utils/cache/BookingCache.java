package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Booking;
import com.cakeshopsystem.utils.dao.BookingDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class BookingCache {

    private static final ObservableList<Booking> bookingsList = FXCollections.observableArrayList();
    private static final Map<Integer, Booking> bookingsMap = new HashMap<>();

    private BookingCache() {}

    // ===================== Getters =====================

    public static ObservableList<Booking> getBookingsList() {
        if (bookingsList.isEmpty()) refreshBookings();
        return bookingsList;
    }

    public static Map<Integer, Booking> getBookingsMap() {
        if (bookingsMap.isEmpty()) refreshBookings();
        return bookingsMap;
    }

    public static Booking getBookingById(int bookingId) {
        if (bookingsMap.isEmpty()) refreshBookings();
        return bookingsMap.get(bookingId);
    }

    // Convenience (filter from cache)
    public static ObservableList<Booking> getBookingsByDate(LocalDate date) {
        if (bookingsList.isEmpty()) refreshBookings();

        ObservableList<Booking> result = FXCollections.observableArrayList();
        if (date == null) return result;

        for (Booking b : bookingsList) {
            if (date.equals(b.getBookingDate())) result.add(b);
        }
        return result;
    }

    // ===================== Refresh =====================

    public static void refreshBookings() {
        bookingsList.clear();
        bookingsMap.clear();

        for (Booking b : BookingDAO.getAllBookings()) {
            cacheBooking(b);
        }
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addBooking(Booking booking) {
        if (booking == null) return false;

        boolean ok = BookingDAO.insertBooking(booking);
        if (ok) cacheBooking(booking);

        return ok;
    }

    public static boolean updateBooking(Booking booking) {
        if (booking == null) return false;

        boolean ok = BookingDAO.updateBooking(booking);
        if (ok) {
            int idx = findIndexById(booking.getBookingId());
            if (idx >= 0) bookingsList.set(idx, booking);
            else bookingsList.add(booking);

            bookingsMap.put(booking.getBookingId(), booking);
        }

        return ok;
    }

    public static boolean deleteBooking(int bookingId) {
        boolean ok = BookingDAO.deleteBooking(bookingId);
        if (ok) {
            bookingsMap.remove(bookingId);
            bookingsList.removeIf(b -> b.getBookingId() == bookingId);
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cacheBooking(Booking booking) {
        if (booking == null) return;

        int id = booking.getBookingId();

        int idx = findIndexById(id);
        if (idx >= 0) bookingsList.set(idx, booking);
        else bookingsList.add(booking);

        bookingsMap.put(id, booking);
    }

    private static int findIndexById(int bookingId) {
        for (int i = 0; i < bookingsList.size(); i++) {
            if (bookingsList.get(i).getBookingId() == bookingId) return i;
        }
        return -1;
    }
}
