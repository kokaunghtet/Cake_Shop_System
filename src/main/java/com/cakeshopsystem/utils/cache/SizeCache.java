package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Size;
import com.cakeshopsystem.utils.dao.SizeDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class SizeCache {

    private static final ObservableList<Size> sizesList = FXCollections.observableArrayList();
    private static final Map<Integer, Size> sizesMap = new HashMap<>();
    private static final Map<Integer, Integer> inchesToIdMap = new HashMap<>();

    private SizeCache() {}

    // ===================== Refresh =====================

    public static void refreshSize() {
        sizesList.clear();
        sizesMap.clear();
        inchesToIdMap.clear();

        for (Size size : SizeDAO.getAllSizes()) {
            cacheSize(size);
        }
    }

    // ===================== Getters =====================

    public static ObservableList<Size> getSizesList() {
        if (sizesList.isEmpty()) refreshSize();
        return sizesList;
    }

    public static Map<Integer, Size> getSizesMap() {
        if (sizesMap.isEmpty()) refreshSize();
        return sizesMap;
    }

    public static Size getSizeById(int sizeId) {
        if (sizesMap.isEmpty()) refreshSize();
        return sizesMap.get(sizeId);
    }

    public static int getSizeIdByInches(int sizeInches) {
        if (inchesToIdMap.isEmpty()) refreshSize();
        return inchesToIdMap.getOrDefault(sizeInches, -1);
    }

    public static Integer getSizeInchesById(int sizeId) {
        Size s = getSizeById(sizeId);
        return s == null ? null : s.getSizeInches();
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addSize(Size size) {
        if (size == null) return false;

        boolean ok = SizeDAO.insertSize(size);
        if (ok) cacheSize(size);

        return ok;
    }

    public static boolean updateSize(Size size) {
        if (size == null) return false;

        Size old = sizesMap.get(size.getSizeId());

        boolean ok = SizeDAO.updateSize(size);
        if (ok) {
            if (old != null) inchesToIdMap.remove(old.getSizeInches());
            cacheSize(size);
        }

        return ok;
    }

    public static boolean deleteSize(int sizeId) {
        boolean ok = SizeDAO.deleteSize(sizeId);
        if (ok) {
            Size removed = sizesMap.remove(sizeId);
            sizesList.removeIf(s -> s.getSizeId() == sizeId);

            if (removed != null) {
                inchesToIdMap.remove(removed.getSizeInches());
            } else {
                rebuildInchesMapFromList();
            }
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cacheSize(Size size) {
        if (size == null) return;

        int id = size.getSizeId();

        int idx = findIndexById(id);
        if (idx >= 0) sizesList.set(idx, size);
        else sizesList.add(size);

        sizesMap.put(id, size);
        inchesToIdMap.put(size.getSizeInches(), id);
    }

    private static int findIndexById(int sizeId) {
        for (int i = 0; i < sizesList.size(); i++) {
            if (sizesList.get(i).getSizeId() == sizeId) return i;
        }
        return -1;
    }

    private static void rebuildInchesMapFromList() {
        inchesToIdMap.clear();
        for (Size size : sizesList) {
            inchesToIdMap.put(size.getSizeInches(), size.getSizeId());
        }
    }
}
