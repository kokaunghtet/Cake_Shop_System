package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Category;
import com.cakeshopsystem.utils.dao.CategoryDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class CategoryCache {

    private static final ObservableList<Category> categoriesList = FXCollections.observableArrayList();
    private static final Map<Integer, Category> categoriesMap = new HashMap<>();
    private static final Map<String, Integer> categoryNameToIdMap = new HashMap<>();

    private CategoryCache() {}

    // ===================== get =====================

    public static ObservableList<Category> getCategoriesList() {
        if (categoriesList.isEmpty()) refreshCategories();
        return categoriesList;
    }

    public static Map<Integer, Category> getCategoriesMap() {
        if (categoriesMap.isEmpty()) refreshCategories();
        return categoriesMap;
    }

    public static Category getCategoryById(int categoryId) {
        if (categoriesMap.isEmpty()) refreshCategories();
        return categoriesMap.get(categoryId);
    }

    public static int getCategoryIdByName(String categoryName) {
        if (categoryName == null || categoryName.isBlank()) return -1;
        if (categoryNameToIdMap.isEmpty()) refreshCategories();
        return categoryNameToIdMap.getOrDefault(normalize(categoryName), -1);
    }

    // ===================== refresh =====================

    public static void refreshCategories() {
        categoriesList.clear();
        categoriesMap.clear();
        categoryNameToIdMap.clear();

        ObservableList<Category> fresh = CategoryDAO.getAllCategories();
        for (Category category : fresh) {
            cacheCategory(category);
        }
    }

    // ===================== crud wrappers =====================

    public static boolean addCategory(Category category) {
        if (category == null) return false;

        boolean ok = CategoryDAO.insertCategory(category);
        if (ok) {
            // DAO sets generated id into category object
            cacheCategory(category);
        }
        return ok;
    }

    public static boolean updateCategory(Category category) {
        if (category == null) return false;

        boolean ok = CategoryDAO.updateCategory(category);
        if (ok) {
            // update list
            int idx = findIndexById(category.getCategoryId());
            if (idx >= 0) categoriesList.set(idx, category);
            else categoriesList.add(category);

            // update maps
            categoriesMap.put(category.getCategoryId(), category);
            categoryNameToIdMap.put(normalize(category.getCategoryName()), category.getCategoryId());
        }
        return ok;
    }

    public static boolean deleteCategory(int categoryId) {
        boolean ok = CategoryDAO.deleteCategory(categoryId);
        if (ok) {
            Category removed = categoriesMap.remove(categoryId);

            // remove from list
            categoriesList.removeIf(c -> c.getCategoryId() == categoryId);

            // remove from name map
            if (removed != null) {
                categoryNameToIdMap.remove(normalize(removed.getCategoryName()));
            } else {
                // fallback: rebuild name map from list
                rebuildNameMapFromList();
            }
        }
        return ok;
    }

    // ===================== helpers =====================

    private static void cacheCategory(Category category) {
        if (category == null) return;

        int id = category.getCategoryId();

        int idx = findIndexById(id);
        if (idx >= 0) categoriesList.set(idx, category);
        else categoriesList.add(category);

        categoriesMap.put(id, category);
        categoryNameToIdMap.put(normalize(category.getCategoryName()), id);
    }

    private static int findIndexById(int categoryId) {
        for (int i = 0; i < categoriesList.size(); i++) {
            if (categoriesList.get(i).getCategoryId() == categoryId) return i;
        }
        return -1;
    }

    private static String normalize(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }

    private static void rebuildNameMapFromList() {
        categoryNameToIdMap.clear();
        for (Category c : categoriesList) {
            categoryNameToIdMap.put(normalize(c.getCategoryName()), c.getCategoryId());
        }
    }
}
