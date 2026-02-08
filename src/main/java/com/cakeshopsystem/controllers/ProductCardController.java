package com.cakeshopsystem.controllers;

import com.cakeshopsystem.controllers.admin.EditProductController;
import com.cakeshopsystem.models.CartItem;
import com.cakeshopsystem.models.Drink;
import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.cache.CakeCache;
import com.cakeshopsystem.utils.cache.DrinkCache;
import com.cakeshopsystem.utils.cache.InventoryCache;
import com.cakeshopsystem.utils.cache.ProductCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.services.CartService;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ProductCardController {

    // =====================================
    // FXML UI COMPONENTS
    // =====================================
    @FXML private StackPane productCardStackPane;
    @FXML private VBox productCardVBox;
    @FXML private ImageView productImage;
    @FXML private Label productName;

    @FXML private HBox drinkOptions;
    @FXML private RadioButton hotOption;
    @FXML private RadioButton coldOption;
    @FXML private ToggleGroup drinkOptionsRadioBtn;

    @FXML private Label productPrice;
    @FXML private Label diyAvailability;

    @FXML private HBox inStockItemsHBox;
    @FXML private Label stockQuantityLabel;
    @FXML private Label productStock;

    @FXML private HBox qtyBtnHBox;
    @FXML private HBox qtyStepperHBox;
    @FXML private Button decreaseBtn;
    @FXML private Label quantityLabel;
    @FXML private Button increaseBtn;

    @FXML private HBox priceHBox;
    @FXML private Button addToCartBtn;
    @FXML private Button addStockQtyBtn;

    // =====================================
    // INTERNAL STATE
    // =====================================
    private int quantity = 1;
    private int maxQty = Integer.MAX_VALUE;
    private Product currentProduct;
    private double drinkHotPrice;
    private double drinkColdPrice;
    private ChangeListener<Toggle> drinkToggleListener;
    private Label inactiveOverlay;
    private boolean excludeDiscountStock = false;
    private boolean isTopProductCard = false;
    private final CartService cartService = CartService.getInstance();
    private Double unitPriceOverride = null;
    private String optionOverride = null;

    private java.util.function.Consumer<Product> onCardDoubleClick;

    // =====================================
    // LIFECYCLE
    // =====================================
    @FXML
    private void initialize() {
        increaseBtn.setOnAction(e -> handleIncrementQuantity());
        decreaseBtn.setOnAction(e -> handleDecrementQuantity());

        addStockQtyBtn.setOnAction(e -> openEditProductPopup());
        addToCartBtn.setOnAction(e -> handleAddToCart());

        productCardStackPane.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && onCardDoubleClick != null && currentProduct != null) {
                onCardDoubleClick.accept(currentProduct);
            }
        });

        cartService.getItems().addListener((javafx.collections.ListChangeListener<CartItem>) c -> {
            refreshRemainingStockLabel();
        });
    }

    // =====================================
    // PUBLIC CONFIGURATION
    // =====================================
    public void setExcludeDiscountStock(boolean exclude) {
        this.excludeDiscountStock = exclude;
    }

    public void applyRoleBasedBtn(String roleName) {
        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        addStockQtyBtn.setVisible(isAdmin);
        addStockQtyBtn.setManaged(isAdmin);

        addToCartBtn.setVisible(!isAdmin);
        addToCartBtn.setManaged(!isAdmin);

        qtyStepperHBox.setVisible(!isAdmin);
        qtyStepperHBox.setManaged(!isAdmin);
    }

    public void setOnCardDoubleClick(java.util.function.Consumer<Product> handler) {
        this.onCardDoubleClick = handler;
    }

    // =====================================
    // DATA BINDING — PRODUCT TYPES
    // =====================================
    public void setDiscountedItemsData(Inventory inv, Product product) {
        resetCardState();

        if (inv == null || product == null) return;

        this.currentProduct = product;
        this.unitPriceOverride = product.getPrice() * SessionManager.discountRate;
        this.optionOverride = "DISCOUNT";

        disableDrinkOptions();
        disableDiyAvailability();
        showStockSection(true);

        loadProductImage(product.getImgPath());
        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(unitPriceOverride));

        int batchQty = inv.getQuantity();
        productStock.setText(String.valueOf(batchQty));

        stockQuantityLabel.setVisible(true);
        stockQuantityLabel.setManaged(true);

        setMaxQtyFromStock(batchQty);
        refreshRemainingStockLabel();
        addToCartBtn.setDisable(batchQty <= 0);
        updateQtyButtonsState();

        applyActiveState(product);
    }

    public void setCakeData(Product product) {
        resetCardState();
        this.currentProduct = product;

        disableDrinkOptions();
        showStockSection(true);

        loadProductImage(product.getImgPath());
        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(product.getPrice()));

        var cake = CakeCache.getCakeByProductId(product.getProductId());
        if (cake != null && cake.isDiyAllowed()) {
            diyAvailability.setText("DIY Available");
            diyAvailability.setVisible(true);
            diyAvailability.setManaged(true);
        } else {
            disableDiyAvailability();
        }

        updateStockUI(product.getProductId());
        refreshRemainingStockLabel();
        applyActiveState(product);
    }

    public void setDrinkData(Product product) {
        resetCardState();
        this.currentProduct = product;

        disableDiyAvailability();
        showStockSection(false);

        loadProductImage(product.getImgPath());
        productName.setText(product.getProductName());

        int productId = product.getProductId();
        double base = product.getPrice();

        Drink hot = DrinkCache.getDrinkByProductIdAndCold(productId, false);
        Drink cold = DrinkCache.getDrinkByProductIdAndCold(productId, true);

        double hotDelta = (hot == null) ? 0.0 : hot.getPriceDelta();
        double coldDelta = (cold == null) ? 0.0 : cold.getPriceDelta();

        drinkHotPrice = base + hotDelta;
        drinkColdPrice = base + coldDelta;

        ensureDrinkToggleGroup();
        installDrinkPriceListenerIfNeeded();

        hotOption.setSelected(true);
        productPrice.setText(String.valueOf(drinkHotPrice));

        maxQty = Integer.MAX_VALUE;
        if (quantity < 1) quantity = 1;
        quantityLabel.setText(String.valueOf(quantity));
        updateQtyButtonsState();

        applyActiveState(product);
    }

    public void setBakedGoodsData(Product product) {
        resetCardState();
        this.currentProduct = product;

        disableDrinkOptions();
        disableDiyAvailability();
        showStockSection(true);

        loadProductImage(product.getImgPath());
        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(product.getPrice()));

        updateStockUI(product.getProductId());
        refreshRemainingStockLabel();
        applyActiveState(product);
    }

    public void setAccessoryData(Product product) {
        resetCardState();
        this.currentProduct = product;

        disableDrinkOptions();
        disableDiyAvailability();
        showStockSection(true);

        loadProductImage(product.getImgPath());
        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(product.getPrice()));

        updateStockUI(product.getProductId());
        refreshRemainingStockLabel();
        applyActiveState(product);
    }

    public void setProductData(Product product) {
        this.currentProduct = product;
        isTopProductCard = true;

        qtyBtnHBox.setVisible(false);
        qtyBtnHBox.setManaged(false);

        disableDrinkOptions();
        disableDiyAvailability();
        showStockSection(false);

        addToCartBtn.setVisible(false);
        addToCartBtn.setManaged(false);

        priceHBox.setVisible(false);
        priceHBox.setManaged(false);

        loadProductImage(product.getImgPath());
        productName.setText(product.getProductName());
        productName.setAlignment(Pos.CENTER);
    }

    // =====================================
    // CART ACTIONS
    // =====================================
    public void handleAddToCart() {
        if (currentProduct == null) return;
        if (!currentProduct.isActive()) return;
        if (quantity <= 0) return;

        SnackBar.show(SnackBarType.INFO, "Added to Cart", "You can adjust quantity in Cart", Duration.seconds(1));

        String option = optionOverride;
        double unitPrice = unitPriceOverride != null ? unitPriceOverride : currentProduct.getPrice();

        if (currentProduct.getCategoryId() == 2) {
            Toggle t = drinkOptionsRadioBtn == null ? null : drinkOptionsRadioBtn.getSelectedToggle();
            boolean cold = (t == coldOption);
            option = cold ? "COLD" : "HOT";
            unitPrice = cold ? drinkColdPrice : drinkHotPrice;

            cartService.addItem(currentProduct, option, unitPrice, quantity);

            quantity = 1;
            quantityLabel.setText("1");
            updateQtyButtonsState();
            return;
        }

        int alreadyInCart = cartService.getQuantity(currentProduct.getProductId(), option);
        int remaining = maxQty - alreadyInCart;

        if (remaining <= 0) {
            addToCartBtn.setDisable(true);
            return;
        }

        int addQty = Math.min(quantity, remaining);
        cartService.addItem(currentProduct, option, unitPrice, addQty);

        if (alreadyInCart + addQty >= maxQty) {
            addToCartBtn.setDisable(true);
        }

        quantity = 1;
        quantityLabel.setText("1");
        updateQtyButtonsState();
    }

    // =====================================
    // STOCK & QUANTITY LOGIC
    // =====================================
    private void refreshRemainingStockLabel() {
        if (currentProduct == null) return;

        if (currentProduct.getCategoryId() == 2) return;

        if (productStock == null || !productStock.isVisible()) return;

        String option = optionOverride;
        int alreadyInCart = cartService.getQuantity(currentProduct.getProductId(), option);

        int remaining = Math.max(maxQty - alreadyInCart, 0);
        if (remaining > 0) {
            productStock.setText(String.valueOf(remaining));
            productStock.getStyleClass().remove("badge-danger");

            stockQuantityLabel.setVisible(true);
            stockQuantityLabel.setManaged(true);

            addToCartBtn.setDisable(!currentProduct.isActive());

        } else {
            productStock.setText("Out of Stock");
            if (!productStock.getStyleClass().contains("badge-danger")) {
                productStock.getStyleClass().add("badge-danger");
            }

            stockQuantityLabel.setVisible(false);
            stockQuantityLabel.setManaged(false);

            addToCartBtn.setDisable(true);

            quantity = 1;
            quantityLabel.setText("1");
        }

        updateQtyButtonsState();
    }

    private void updateStockUI(int productId) {
        int totalQty = getAvailableQty(productId);
        setMaxQtyFromStock(totalQty);

        if (totalQty > 0) {
            productStock.setText(String.valueOf(totalQty));
            productStock.getStyleClass().remove("badge-danger");

            stockQuantityLabel.setVisible(true);
            stockQuantityLabel.setManaged(true);
        } else {
            productStock.setText("Out of Stock");
            if (!productStock.getStyleClass().contains("badge-danger")) {
                productStock.getStyleClass().add("badge-danger");
            }
            stockQuantityLabel.setVisible(false);
            stockQuantityLabel.setManaged(false);
        }

        setAddToCartDisable(productId);
        updateQtyButtonsState();
    }

    private int getAvailableQty(int productId) {
        return excludeDiscountStock
                ? InventoryCache.getRegularQtyByProductId(productId)
                : InventoryCache.getTotalQtyByProductId(productId);
    }

    private void setMaxQtyFromStock(int stockQty) {
        this.maxQty = Math.max(stockQty, 0);

        if (maxQty == 0) {
            quantity = 1;
            quantityLabel.setText("1");
            return;
        }
        clampQuantity();
    }

    private void clampQuantity() {
        if (quantity < 1) quantity = 1;
        if (quantity > maxQty) quantity = maxQty;
        quantityLabel.setText(String.valueOf(quantity));
    }

    private void setAddToCartDisable(int productId) {
        int totalQty = getAvailableQty(productId);
        addToCartBtn.setDisable(totalQty <= 0);
    }

    private void handleIncrementQuantity() {
        if (quantity < maxQty) {
            quantity++;
            quantityLabel.setText(String.valueOf(quantity));
        }
        updateQtyButtonsState();
    }

    private void handleDecrementQuantity() {
        if (maxQty <= 0) return;

        if (quantity > 1) {
            quantity--;
            quantityLabel.setText(String.valueOf(quantity));
        }
        updateQtyButtonsState();
    }

    private void updateQtyButtonsState() {
        boolean outOfStock = maxQty <= 0;
        increaseBtn.setDisable(outOfStock || quantity >= maxQty);
        decreaseBtn.setDisable(outOfStock || quantity <= 1);
    }

    // =====================================
    // UI STATE HELPERS
    // =====================================
    private void resetCardState() {
        unitPriceOverride = null;
        optionOverride = null;
        quantity = 1;
        quantityLabel.setText("1");
    }

    private void applyActiveState(Product product) {
        boolean active = product != null && product.isActive();

        ensureInactiveOverlay();

        inactiveOverlay.setVisible(!active);
        inactiveOverlay.setManaged(!active);

        productCardStackPane.setOpacity(active ? 1.0 : 0.6);
        setInteractiveDisabled(!active);
    }

    private void ensureInactiveOverlay() {
        if (inactiveOverlay != null) return;

        inactiveOverlay = new Label("Product Not Available");
        inactiveOverlay.getStyleClass().add("badge-danger");

        StackPane.setAlignment(inactiveOverlay, Pos.BASELINE_CENTER);
        StackPane.setMargin(inactiveOverlay, new Insets(10));

        inactiveOverlay.setMouseTransparent(true);
        productCardStackPane.getChildren().add(inactiveOverlay);
    }

    private void setInteractiveDisabled(boolean disabled) {
        if (addToCartBtn != null) addToCartBtn.setDisable(disabled);
        if (increaseBtn != null) increaseBtn.setDisable(disabled);
        if (decreaseBtn != null) decreaseBtn.setDisable(disabled);
        if (hotOption != null) hotOption.setDisable(disabled);
        if (coldOption != null) coldOption.setDisable(disabled);
    }

    private void showStockSection(boolean show) {
        inStockItemsHBox.setVisible(show);
        inStockItemsHBox.setManaged(show);
    }

    private void disableDrinkOptions() {
        drinkOptions.setVisible(false);
        drinkOptions.setManaged(false);
    }

    private void disableDiyAvailability() {
        diyAvailability.setVisible(false);
        diyAvailability.setManaged(false);
    }

    private void ensureDrinkToggleGroup() {
        if (drinkOptionsRadioBtn == null) {
            drinkOptionsRadioBtn = new ToggleGroup();
        }
        hotOption.setToggleGroup(drinkOptionsRadioBtn);
        coldOption.setToggleGroup(drinkOptionsRadioBtn);

        drinkOptions.setVisible(true);
        drinkOptions.setManaged(true);
    }

    private void installDrinkPriceListenerIfNeeded() {
        if (drinkToggleListener != null) return;

        drinkToggleListener = (obs, oldToggle, newToggle) -> {
            if (newToggle == null) return;

            if (newToggle == hotOption) {
                productPrice.setText(String.valueOf(drinkHotPrice));
            } else if (newToggle == coldOption) {
                productPrice.setText(String.valueOf(drinkColdPrice));
            }
        };

        drinkOptionsRadioBtn.selectedToggleProperty().addListener(drinkToggleListener);
    }

    // =====================================
    // IMAGE LOADING
    // =====================================
    private void loadProductImage(String imagePath) {
        try {
            Image image = loadImageSmart(imagePath);

            if (image == null || image.isError()) {
                image = loadImageSmart("/images/default-product.png");
            }

            if (image != null && !image.isError()) {
                productImage.setImage(image);
            }
        } catch (Exception err) {
            System.err.println("Error loading product image: " + err.getLocalizedMessage());
            err.printStackTrace();
        }
    }

    private Image loadImageSmart(String path) {
        if (path == null || path.isBlank()) return null;

        if (path.matches("^(https?|file|jar):.*")) {
            return new Image(path, true);
        }

        String res = path.startsWith("/") ? path : "/" + path;
        var url = getClass().getResource(res);
        if (url != null) {
            return new Image(url.toExternalForm(), true);
        }

        var f = new java.io.File(path);
        if (f.exists()) {
            return new Image(f.toURI().toString(), true);
        }

        return null;
    }

    // =====================================
    // ADMIN POPUPS & REFRESH
    // =====================================
    private void openEditProductPopup() {
        if (currentProduct == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/EditProduct.fxml"));
            Parent root = loader.load();

            EditProductController controller = loader.getController();
            controller.setData(currentProduct);
            controller.setOnSaved(this::refreshThisCard);

            MainController.togglePopupContent(root);
        } catch (Exception ex) {
            System.err.println("Failed to open EditProduct popup: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void refreshThisCard() {
        if (currentProduct == null) return;

        Product fresh = ProductCache.getProductById(currentProduct.getProductId());
        if (fresh == null) return;

        currentProduct = fresh;

        switch (fresh.getCategoryId()) {
            case 1 -> setCakeData(fresh);
            case 2 -> setDrinkData(fresh);
            case 3 -> setBakedGoodsData(fresh);
            case 4 -> setAccessoryData(fresh);
        }

        applyActiveState(fresh);
    }
}
