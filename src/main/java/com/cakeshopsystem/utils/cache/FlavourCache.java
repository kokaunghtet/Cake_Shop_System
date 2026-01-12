package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Flavour;
import com.cakeshopsystem.utils.dao.FlavourDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class FlavourCache {

    private static final ObservableList<Flavour> flavoursList = FXCollections.observableArrayList();
    private static final Map<Integer, Flavour> flavoursMap = new HashMap<>();
    private static final Map<String, Integer> flavourNameToIdMap = new HashMap<>();

    private FlavourCache() {}

    // ===================== Cache Refresh =====================

    public static void refreshFlavour() {
        flavoursList.clear();
        flavoursMap.clear();
        flavourNameToIdMap.clear();

        for (Flavour flavour : FlavourDAO.getAllFlavours()) {
            cacheFlavour(flavour);
        }
    }

    // ===================== Getters =====================

    public static ObservableList<Flavour> getFlavoursList() {
        if (flavoursList.isEmpty()) refreshFlavour();
        return flavoursList;
    }

    public static Map<Integer, Flavour> getFlavoursMap() {
        if (flavoursMap.isEmpty()) refreshFlavour();
        return flavoursMap;
    }

    public static Flavour getFlavourById(int flavourId) {
        if (flavoursMap.isEmpty()) refreshFlavour();
        return flavoursMap.get(flavourId);
    }

    public static int getFlavourIdByName(String flavourName) {
        if (flavourNameToIdMap.isEmpty()) refreshFlavour();
        return flavourNameToIdMap.getOrDefault(normalize(flavourName), -1);
    }

    public static String getFlavourNameById(int flavourId) {
        Flavour flavour = getFlavourById(flavourId);
        return flavour == null ? null : flavour.getFlavourName();
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addFlavour(Flavour flavour) {
        if (flavour == null) return false;

        boolean ok = FlavourDAO.insertFlavour(flavour);
        if (ok) cacheFlavour(flavour);

        return ok;
    }

    public static boolean updateFlavour(Flavour flavour) {
        if (flavour == null) return false;

        boolean ok = FlavourDAO.updateFlavour(flavour);
        if (ok) {
            int idx = findIndexById(flavour.getFlavourId());
            if (idx >= 0) flavoursList.set(idx, flavour);
            else flavoursList.add(flavour);

            flavoursMap.put(flavour.getFlavourId(), flavour);
            flavourNameToIdMap.put(normalize(flavour.getFlavourName()), flavour.getFlavourId());
        }

        return ok;
    }

    public static boolean deleteFlavour(int flavourId) {
        boolean ok = FlavourDAO.deleteFlavour(flavourId);
        if (ok) {
            Flavour removed = flavoursMap.remove(flavourId);
            if (removed != null) {
                flavoursList.removeIf(f -> f.getFlavourId() == flavourId);
                flavourNameToIdMap.remove(normalize(removed.getFlavourName()));
            } else {
                flavoursList.removeIf(f -> f.getFlavourId() == flavourId);
                rebuildNameMapFromList();
            }
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cacheFlavour(Flavour flavour) {
        if (flavour == null) return;

        int id = flavour.getFlavourId();

        int idx = findIndexById(id);
        if (idx >= 0) flavoursList.set(idx, flavour);
        else flavoursList.add(flavour);

        flavoursMap.put(id, flavour);
        flavourNameToIdMap.put(normalize(flavour.getFlavourName()), id);
    }

    private static int findIndexById(int flavourId) {
        for (int i = 0; i < flavoursList.size(); i++) {
            if (flavoursList.get(i).getFlavourId() == flavourId) return i;
        }
        return -1;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static void rebuildNameMapFromList() {
        flavourNameToIdMap.clear();
        for (Flavour flavour : flavoursList) {
            flavourNameToIdMap.put(normalize(flavour.getFlavourName()), flavour.getFlavourId());
        }
    }
}
