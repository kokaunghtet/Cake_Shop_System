package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Cake;
import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.constants.CakeType;
import com.cakeshopsystem.utils.dao.CakeDAO;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import com.cakeshopsystem.utils.dao.ProductDAO;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;

import java.io.IOException;

public class ProductViewController {

    /* =========================================================
       FXML - Main Containers
       ========================================================= */
    @FXML
    private ScrollPane mainScrollPane;

    /* =========================================================
       FXML - Top Actions
       ========================================================= */
    @FXML
    private Button addNewProductBtn;
    @FXML
    private Button cartBtn;

    /* =========================================================
       FXML - Discount Section
       ========================================================= */
    @FXML
    private ScrollPane discountedItemsScrollPane;
    @FXML
    private HBox discountedItemsHBox;

    /* =========================================================
       FXML - Category Sections (Horizontal Scrollers)
       ========================================================= */
    @FXML
    private ScrollPane cakeScrollPane;
    @FXML
    private HBox cakeHBox;

    @FXML
    private ScrollPane drinkScrollPane;
    @FXML
    private HBox drinkHBox;

    @FXML
    private ScrollPane bakedGoodScrollPane;
    @FXML
    private HBox bakedGoodHBox;

    @FXML
    private ScrollPane accessoryScrollPane;
    @FXML
    private HBox accessoryHBox;

    /* =========================================================
       Constants
       ========================================================= */
    private static final String PRODUCT_CARD_FXML = "/views/ProductCard.fxml";
    private String roleName = "";

    /* =========================================================
       Lifecycle
       ========================================================= */
    @FXML
    private void initialize() {
        roleName = resolveRoleName();
        configureRoleBasedAccess(roleName);

        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        if (isAdmin) {
            discountedItemsScrollPane.setVisible(false);
            discountedItemsScrollPane.setManaged(false);
        } else {
            loadDiscountedItems();
        }

        loadCakes();
        loadDrinks();
        loadBakedGoods();
        loadAccessories();
    }


    /* =========================================================
       Role-Based UI Access Control
       ========================================================= */
    private String resolveRoleName() {
        User user = SessionManager.getUser();
        if (user == null) return "";

        var rolesMap = RoleCache.getRolesMap();
        if (rolesMap == null || !rolesMap.containsKey(user.getRole())) return "";

        String rn = rolesMap.get(user.getRole()).getRoleName();
        return rn == null ? "" : rn;
    }

    private void configureRoleBasedAccess(String roleName) {
        if (roleName.equalsIgnoreCase("Admin")) {
            cartBtn.setVisible(false);
            cartBtn.setManaged(false);
        } else if (roleName.equalsIgnoreCase("Cashier")) {
            addNewProductBtn.setVisible(false);
            addNewProductBtn.setManaged(false);
        }
    }

    /* =========================================================
       Data Loading - Discounted Items
       ========================================================= */
    private void loadDiscountedItems() {
        ObservableList<Inventory> discountedItemList = InventoryDAO.getInventoryDiscountCandidates();

        for (Inventory inventoryRow : discountedItemList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setDiscountedItemsData(inventoryRow);
                controller.applyRoleBasedBtn(roleName);

                discountedItemsHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading discounted item card: ");
                err.printStackTrace();
            }
        }
    }

    /* =========================================================
       Data Loading - Cakes
       ========================================================= */
    private void loadCakes() {
        ObservableList<Product> cakeList = ProductDAO.getProductsByCategoryId(1);

        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        for (Product productCake : cakeList) {

            // ✅ ADD THIS (FILTER)
            if (!isAdmin && !productCake.isActive()) {
                continue;
            }

            Cake cake = CakeDAO.getCakeByProductId(productCake.getProductId());
            if (cake == null) continue;

            if (cake.getCakeType() != CakeType.PREBAKED) {
                continue;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setCakeData(productCake);
                controller.applyRoleBasedBtn(roleName);

                cakeHBox.getChildren().add(card);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }


    /* =========================================================
       Data Loading - Drinks
       ========================================================= */
    private void loadDrinks() {

        ObservableList<Product> drinkList = ProductDAO.getProductsByCategoryId(2);

        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        for (Product productDrink : drinkList) {

            // ✅ ADD THIS
            if (!isAdmin && !productDrink.isActive()) {
                continue;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setDrinkData(productDrink);
                controller.applyRoleBasedBtn(roleName);

                drinkHBox.getChildren().add(card);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }


    /* =========================================================
       Data Loading - Baked Goods
       ========================================================= */
    private void loadBakedGoods() {

        ObservableList<Product> bakedGoodList = ProductDAO.getProductsByCategoryId(3);

        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        for (Product productBakedGood : bakedGoodList) {

            // ✅ ADD THIS
            if (!isAdmin && !productBakedGood.isActive()) {
                continue;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setBakedGoodsData(productBakedGood);
                controller.applyRoleBasedBtn(roleName);

                bakedGoodHBox.getChildren().add(card);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }


    /* =========================================================
       Data Loading - Accessories
       ========================================================= */
    private void loadAccessories() {

        ObservableList<Product> accessoryList = ProductDAO.getProductsByCategoryId(4);

        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        for (Product productAccessory : accessoryList) {

            // ✅ ADD THIS
            if (!isAdmin && !productAccessory.isActive()) {
                continue;
            }

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setAccessoryData(productAccessory);
                controller.applyRoleBasedBtn(roleName);

                accessoryHBox.getChildren().add(card);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }
}

