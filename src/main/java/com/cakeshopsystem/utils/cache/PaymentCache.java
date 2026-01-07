package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Payment;
import com.cakeshopsystem.utils.dao.PaymentDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class PaymentCache {

    private static final ObservableList<Payment> paymentsList = FXCollections.observableArrayList();
    private static final ObservableList<Payment> activePaymentsList = FXCollections.observableArrayList();
    private static final Map<Integer, Payment> paymentsMap = new HashMap<>();
    private static final Map<String, Integer> paymentNameToIdMap = new HashMap<>();

    private PaymentCache() {
    }

    // ===================== Getters =====================

    public static ObservableList<Payment> getPaymentsList() {
        if (paymentsList.isEmpty()) refreshPayments();
        return paymentsList;
    }

    // Use this for POS dropdown (only available methods)
    public static ObservableList<Payment> getActivePaymentsList() {
        if (paymentsList.isEmpty()) refreshPayments();
        return activePaymentsList;
    }

    public static Map<Integer, Payment> getPaymentsMap() {
        if (paymentsMap.isEmpty()) refreshPayments();
        return paymentsMap;
    }

    public static Payment getPaymentById(int paymentId) {
        if (paymentsMap.isEmpty()) refreshPayments();
        return paymentsMap.get(paymentId);
    }

    public static int getPaymentIdByName(String paymentName) {
        if (paymentName == null || paymentName.isBlank()) return -1;
        if (paymentNameToIdMap.isEmpty()) refreshPayments();
        return paymentNameToIdMap.getOrDefault(normalize(paymentName), -1);
    }

    // ===================== Refresh =====================

    public static void refreshPayments() {
        paymentsList.clear();
        activePaymentsList.clear();
        paymentsMap.clear();
        paymentNameToIdMap.clear();

        for (Payment p : PaymentDAO.getAllPayments()) {
            cachePayment(p);
        }
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addPayment(Payment payment) {
        if (payment == null) return false;

        boolean ok = PaymentDAO.insertPayment(payment);
        if (ok) cachePayment(payment);
        return ok;
    }

    public static boolean updatePayment(Payment payment) {
        if (payment == null) return false;

        boolean ok = PaymentDAO.updatePayment(payment);
        if (ok) {
            int idx = findIndexById(payment.getPaymentId());
            if (idx >= 0) paymentsList.set(idx, payment);
            else paymentsList.add(payment);

            paymentsMap.put(payment.getPaymentId(), payment);
            paymentNameToIdMap.put(normalize(payment.getPaymentName()), payment.getPaymentId());

            rebuildActiveList();
        }
        return ok;
    }

    public static boolean deletePayment(int paymentId) {
        boolean ok = PaymentDAO.deletePayment(paymentId);
        if (ok) {
            Payment removed = paymentsMap.remove(paymentId);

            paymentsList.removeIf(p -> p.getPaymentId() == paymentId);

            if (removed != null) {
                paymentNameToIdMap.remove(normalize(removed.getPaymentName()));
            } else {
                rebuildNameMapFromList();
            }

            rebuildActiveList();
        }
        return ok;
    }

    public static boolean setPaymentActive(int paymentId, boolean active) {
        boolean ok = PaymentDAO.setPaymentActive(paymentId, active);
        if (ok) {
            Payment p = paymentsMap.get(paymentId);
            if (p != null) p.setActive(active);
            rebuildActiveList();
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cachePayment(Payment payment) {
        if (payment == null) return;

        int id = payment.getPaymentId();

        int idx = findIndexById(id);
        if (idx >= 0) paymentsList.set(idx, payment);
        else paymentsList.add(payment);

        paymentsMap.put(id, payment);
        paymentNameToIdMap.put(normalize(payment.getPaymentName()), id);

        rebuildActiveList();
    }

    private static int findIndexById(int paymentId) {
        for (int i = 0; i < paymentsList.size(); i++) {
            if (paymentsList.get(i).getPaymentId() == paymentId) return i;
        }
        return -1;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static void rebuildNameMapFromList() {
        paymentNameToIdMap.clear();
        for (Payment p : paymentsList) {
            paymentNameToIdMap.put(normalize(p.getPaymentName()), p.getPaymentId());
        }
    }

    private static void rebuildActiveList() {
        activePaymentsList.clear();
        for (Payment p : paymentsList) {
            if (p.isActive()) activePaymentsList.add(p);
        }
    }
}
