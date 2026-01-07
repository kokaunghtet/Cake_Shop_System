package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.dao.ProductDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class ProductCache {

    private static final ObservableList<Product> productsList = FXCollections.observableArrayList();
    private static final Map<Integer, Product> productsMap = new HashMap<>();

    // NOTE: If product_name is not unique, this will keep the latest one cached for that name.
    private static final Map<String, Integer> productNameToIdMap = new HashMap<>();

    // Helpful index for category -> products
    private static final Map<Integer, ObservableList<Product>> categoryIdToProductsMap = new HashMap<>();

    private ProductCache() {
    }

    // ===================== getters =====================

    public static ObservableList<Product> getProductsList() {
        if (productsList.isEmpty()) refreshProducts();
        return productsList;
    }

    public static Map<Integer, Product> getProductsMap() {
        if (productsMap.isEmpty()) refreshProducts();
        return productsMap;
    }

    public static Product getProductById(int productId) {
        if (productsMap.isEmpty()) refreshProducts();
        return productsMap.get(productId);
    }

    public static int getProductIdByName(String productName) {
        if (productName == null || productName.isBlank()) return -1;
        if (productNameToIdMap.isEmpty()) refreshProducts();
        return productNameToIdMap.getOrDefault(normalize(productName), -1);
    }

    public static ObservableList<Product> getProductsByCategoryId(int categoryId) {
        if (categoryIdToProductsMap.isEmpty()) refreshProducts();
        return categoryIdToProductsMap.getOrDefault(categoryId, FXCollections.observableArrayList());
    }

    // ===================== refresh =====================

    public static void refreshProducts() {
        productsList.clear();
        productsMap.clear();
        productNameToIdMap.clear();
        categoryIdToProductsMap.clear();

        ObservableList<Product> fresh = ProductDAO.getAllProducts();
        for (Product product : fresh) {
            cacheProduct(product);
        }
    }

    // ===================== crud wrappers =====================

    public static boolean addProduct(Product product) {
        if (product == null) return false;

        boolean ok = ProductDAO.insertProduct(product);
        if (ok) {
            // DAO sets generated id into product object
            cacheProduct(product);
        }
        return ok;
    }

    public static boolean updateProduct(Product product) {
        if (product == null) return false;

        boolean ok = ProductDAO.updateProduct(product);
        if (ok) {
            // If category changed, remove from old category list first
            Product old = productsMap.get(product.getProductId());
            if (old != null && old.getCategoryId() != product.getCategoryId()) {
                ObservableList<Product> oldList = categoryIdToProductsMap.get(old.getCategoryId());
                if (oldList != null) oldList.removeIf(p -> p.getProductId() == product.getProductId());
            }

            cacheProduct(product);
        }
        return ok;
    }

    public static boolean deleteProduct(int productId) {
        boolean ok = ProductDAO.deleteProduct(productId);
        if (ok) {
            Product removed = productsMap.remove(productId);

            // remove from list
            productsList.removeIf(p -> p.getProductId() == productId);

            // remove from name map
            if (removed != null) {
                productNameToIdMap.remove(normalize(removed.getProductName()));

                // remove from category map
                ObservableList<Product> catList = categoryIdToProductsMap.get(removed.getCategoryId());
                if (catList != null) {
                    catList.removeIf(p -> p.getProductId() == productId);
                    if (catList.isEmpty()) categoryIdToProductsMap.remove(removed.getCategoryId());
                }
            } else {
                // fallback: rebuild maps from list
                rebuildNameMapFromList();
                rebuildCategoryMapFromList();
            }
        }
        return ok;
    }

    // ===================== helpers =====================

    private static void cacheProduct(Product product) {
        if (product == null) return;

        int id = product.getProductId();

        int idx = findIndexById(id);
        if (idx >= 0) productsList.set(idx, product);
        else productsList.add(product);

        productsMap.put(id, product);
        productNameToIdMap.put(normalize(product.getProductName()), id);

        // category index
        categoryIdToProductsMap.putIfAbsent(product.getCategoryId(), FXCollections.observableArrayList());

        ObservableList<Product> list = categoryIdToProductsMap.get(product.getCategoryId());
        list.removeIf(p -> p.getProductId() == id);
        list.add(product);
    }

    private static int findIndexById(int productId) {
        for (int i = 0; i < productsList.size(); i++) {
            if (productsList.get(i).getProductId() == productId) return i;
        }
        return -1;
    }

    private static String normalize(String s) {
        return (s == null) ? "" : s.trim().toLowerCase();
    }

    private static void rebuildNameMapFromList() {
        productNameToIdMap.clear();
        for (Product p : productsList) {
            productNameToIdMap.put(normalize(p.getProductName()), p.getProductId());
        }
    }

    private static void rebuildCategoryMapFromList() {
        categoryIdToProductsMap.clear();
        for (Product p : productsList) {
            categoryIdToProductsMap.putIfAbsent(p.getCategoryId(), FXCollections.observableArrayList());
            categoryIdToProductsMap.get(p.getCategoryId()).add(p);
        }
    }
}
