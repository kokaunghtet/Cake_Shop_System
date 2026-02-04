package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.cache.CakeCache;
import com.cakeshopsystem.utils.cache.InventoryCache;
import com.cakeshopsystem.utils.cache.ProductCache;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.CakeType;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import com.cakeshopsystem.utils.events.AppEvents;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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
    // ========== STATE & CONSTANTS ========
    // =====================================

    private static final String PRODUCT_CARD_FXML = "/views/ProductCard.fxml";
    private String roleName = "";

    private final ChangeListener<Number> orderListener = (obs, oldV, newV) -> {
        Platform.runLater(() -> {
            InventoryCache.refresh();
            reloadAllProducts();
        });
    };

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
            discountedItemsScrollPane.setVisible(true);
            discountedItemsScrollPane.setManaged(true);
            loadDiscountedItems();
        }

        loadCakes();
        loadDrinks();
        loadBakedGoods();
        loadAccessories();

        addNewProductBtn.setOnAction(e -> handleAddNewProduct());
        processExpiredBtn.setOnAction(e -> handleWasteProduct());

        // add listener
        AppEvents.orderCompletedCounterProperty().addListener(orderListener);

        // remove listener automatically when this view is detached
        mainScrollPane.sceneProperty().addListener(new ChangeListener<>() {
            @Override
            public void changed(ObservableValue<? extends Scene> obs, javafx.scene.Scene oldScene, javafx.scene.Scene newScene) {
                if (oldScene != null && newScene == null) {
                    AppEvents.orderCompletedCounterProperty().removeListener(orderListener);
                }
            }
        });
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
        if (roleName.equalsIgnoreCase("Cashier")) {
            addNewProductBtn.setVisible(false);
            addNewProductBtn.setManaged(false);
        }
    }

    // =====================================
    // ========= DATA LOADING LOGIC ========
    // =====================================

    private void loadCategory(
            int categoryId,
            HBox targetHBox,
            java.util.function.BiConsumer<ProductCardController, Product> binder,
            java.util.function.Predicate<Product> extraFilter
    ) {
        var list = ProductCache.getProductsByCategoryId(categoryId);
        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        for (Product p : list) {
            if (!isAdmin && !p.isActive()) continue;
            if (extraFilter != null && !extraFilter.test(p)) continue;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setExcludeDiscountStock(!isAdmin);
                controller.applyRoleBasedBtn(roleName);

                binder.accept(controller, p);

                targetHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading product card: ");
                err.printStackTrace();
            }
        }
    }

    private void loadDiscountedItems() {
        var list = InventoryCache.getDiscountCandidates();
        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        for (Inventory inv : list) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setExcludeDiscountStock(!isAdmin);
                controller.applyRoleBasedBtn(roleName);

                Product p = ProductCache.getProductById(inv.getProductId());
                controller.setDiscountedItemsData(inv, p);

                discountedItemsHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading discounted item card: ");
                err.printStackTrace();
            }
        }
    }

    private void loadCakes() {
        loadCategory(
                1,
                cakeHBox,
                (c, p) -> c.setCakeData(p),
                p -> {
                    var cake = CakeCache.getCakeByProductId(p.getProductId());
                    return cake != null && cake.getCakeType() == CakeType.PREBAKED;
                }
        );
    }

    private void loadDrinks() {
        loadCategory(2, drinkHBox, (c, p) -> c.setDrinkData(p), null);
    }

    private void loadBakedGoods() {
        loadCategory(3, bakedGoodHBox, (c, p) -> c.setBakedGoodsData(p), null);
    }

    private void loadAccessories() {
        loadCategory(4, accessoryHBox, (c, p) -> c.setAccessoryData(p), null);
    }

    // =====================================
    // ========= REFRESH & ACTIONS =========
    // =====================================

    private void handleWasteProduct() {
        Integer userId = SessionManager.getUser().getUserId();
        InventoryDAO.wasteExpiredInventory(userId);

        InventoryCache.refresh();
        reloadAllProducts();

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