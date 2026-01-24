package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import com.cakeshopsystem.utils.dao.ProductDAO;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

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

    /* =========================================================
       Lifecycle
       ========================================================= */
    @FXML
    private void initialize() {
        configureRoleBasedAccess();

        // Load all sections
        loadDiscountedItems();
        loadCakes();
        loadDrinks();
        loadBakedGoods();
        loadAccessories();
    }

    /* =========================================================
       Role-Based UI Access Control
       ========================================================= */
    private void configureRoleBasedAccess() {
        User user = SessionManager.getUser();
        if (user == null) return;

        var rolesMap = RoleCache.getRolesMap();
        if (rolesMap == null || !rolesMap.containsKey(user.getRole())) return;

        String roleName = rolesMap.get(user.getRole()).getRoleName();
        if (roleName == null) return;

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

                discountedItemsHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading discounted item card: " + err.getLocalizedMessage());
                err.printStackTrace();
            }
        }
    }

    /* =========================================================
       Data Loading - Cakes
       ========================================================= */
    private void loadCakes() {
        ObservableList<Product> cakeList = ProductDAO.getProductsByCategoryId(1);

        for (Product productCake : cakeList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setCakeData(productCake);

                cakeHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading cake card: " + err.getLocalizedMessage());
            }
        }
    }

    /* =========================================================
       Data Loading - Drinks
       ========================================================= */
    private void loadDrinks() {
        ObservableList<Product> drinkList = ProductDAO.getProductsByCategoryId(2);

        for (Product productDrink : drinkList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setDrinkData(productDrink);

                drinkHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading drink card: " + err.getLocalizedMessage());
            }
        }
    }

    /* =========================================================
       Data Loading - Baked Goods
       ========================================================= */
    private void loadBakedGoods() {
        ObservableList<Product> bakedGoodList = ProductDAO.getProductsByCategoryId(3);

        for (Product productBakedGood : bakedGoodList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setBakedGoodsData(productBakedGood);

                bakedGoodHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading baked good card: " + err.getLocalizedMessage());
            }
        }
    }

    /* =========================================================
       Data Loading - Accessories
       ========================================================= */
    private void loadAccessories() {
        ObservableList<Product> accessoryList = ProductDAO.getProductsByCategoryId(4);

        for (Product productAccessory : accessoryList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setAccessoryData(productAccessory);

                accessoryHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading accessory card: " + err.getLocalizedMessage());
            }
        }
    }
}
