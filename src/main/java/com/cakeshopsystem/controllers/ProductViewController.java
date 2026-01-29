package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Cake;
import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.CakeType;
import com.cakeshopsystem.utils.constants.SnackBarType;
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
import javafx.scene.layout.HBox;
import javafx.util.Duration;

public class ProductViewController {
    // =====================================
    // ============ FXML FIELDS ============
    // =====================================

    @FXML
    private ScrollPane mainScrollPane;

    /* Top Action Controls */
    @FXML
    private Button addNewProductBtn;
    @FXML
    private Button cartBtn;
    @FXML
    private Button processExpiredBtn;

    /* Discount Section Controls */
    @FXML
    private ScrollPane discountedItemsScrollPane;
    @FXML
    private HBox discountedItemsHBox;

    /* Category Containers (Horizontal Scrollers) */
    @FXML
    private HBox cakeHBox;
    @FXML
    private HBox drinkHBox;
    @FXML
    private HBox bakedGoodHBox;
    @FXML
    private HBox accessoryHBox;

    // =====================================
    // =========== STATE & CONSTS ==========
    // =====================================

    private static final String PRODUCT_CARD_FXML = "/views/ProductCard.fxml";
    private String roleName = "";

    // =====================================
    // ============= LIFECYCLE =============
    // =====================================

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

        addNewProductBtn.setOnAction(e -> handleAddNewProduct());
        processExpiredBtn.setOnAction(e -> handleWasteProduct());
    }

    // =====================================
    // ========= ROLE-BASED ACCESS =========
    // =====================================

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

    // =====================================
    // ========= DATA LOADING LOGIC ========
    // =====================================

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

    private void loadCakes() {
        ObservableList<Product> cakeList = ProductDAO.getProductsByCategoryId(1);

        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        for (Product productCake : cakeList) {

            if (!isAdmin && !productCake.isActive()) continue;

            Cake cake = CakeDAO.getCakeByProductId(productCake.getProductId());
            if (cake == null) continue;

            if (cake.getCakeType() != CakeType.PREBAKED) continue;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setExcludeDiscountStock(!isAdmin);
                controller.setCakeData(productCake);
                controller.applyRoleBasedBtn(roleName);

                cakeHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading cake card: ");
                err.printStackTrace();
            }
        }
    }

    private void loadDrinks() {
        ObservableList<Product> drinkList = ProductDAO.getProductsByCategoryId(2);

        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        for (Product productDrink : drinkList) {

            if (!isAdmin && !productDrink.isActive()) continue;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setDrinkData(productDrink);
                controller.applyRoleBasedBtn(roleName);

                drinkHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading drink card: ");
                err.printStackTrace();
            }
        }
    }

    private void loadBakedGoods() {
        ObservableList<Product> bakedGoodList = ProductDAO.getProductsByCategoryId(3);

        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        for (Product productBakedGood : bakedGoodList) {

            if (!isAdmin && !productBakedGood.isActive()) continue;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setExcludeDiscountStock(!isAdmin);
                controller.setBakedGoodsData(productBakedGood);
                controller.applyRoleBasedBtn(roleName);

                bakedGoodHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading baked good card: ");
                err.printStackTrace();
            }
        }
    }

    private void loadAccessories() {
        ObservableList<Product> accessoryList = ProductDAO.getProductsByCategoryId(4);

        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        for (Product productAccessory : accessoryList) {

            if (!isAdmin && !productAccessory.isActive()) continue;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setAccessoryData(productAccessory);
                controller.applyRoleBasedBtn(roleName);

                accessoryHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading accessory card: ");
                err.printStackTrace();
            }
        }
    }

    // =====================================
    // ========= REFRESH & ACTIONS =========
    // =====================================

    private void handleWasteProduct() {
        Integer userId = SessionManager.getUser().getUserId();
        InventoryDAO.wasteExpiredInventory(userId);

        SnackBar.show(SnackBarType.SUCCESS, "Success", "Expired stock has been processed.", Duration.seconds(2));
    }

    private void reloadAllProducts() {
        discountedItemsHBox.getChildren().clear();
        cakeHBox.getChildren().clear();
        drinkHBox.getChildren().clear();
        bakedGoodHBox.getChildren().clear();
        accessoryHBox.getChildren().clear();

        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        if (isAdmin) {
            discountedItemsScrollPane.setVisible(false);
            discountedItemsScrollPane.setManaged(false);
        } else {
            discountedItemsScrollPane.setVisible(true);
            discountedItemsScrollPane.setManaged(true);
            loadDiscountedItems();
        }

        loadCakes();
        loadDrinks();
        loadBakedGoods();
        loadAccessories();
    }

    private void handleAddNewProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AddProduct.fxml"));
            Parent root = loader.load();

            AddProductController controller = loader.getController();
            controller.setOnSaved(this::afterAddProductSaved);

            MainController.togglePopupContent(root);
        } catch (Exception e) {
            System.out.println("Failed to open AddProduct popup:");
            e.printStackTrace();
        }
    }

    private void afterAddProductSaved() {
        reloadAllProducts();
    }
}