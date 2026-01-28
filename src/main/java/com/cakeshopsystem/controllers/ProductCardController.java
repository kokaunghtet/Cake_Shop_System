package com.cakeshopsystem.controllers;

import com.cakeshopsystem.controllers.admin.EditProductController;
import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.cache.CakeCache;
import com.cakeshopsystem.utils.dao.DrinkDAO;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import com.cakeshopsystem.utils.dao.ProductDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Toggle;

public class ProductCardController {

    // =====================================
    // ============ FXML FIELDS ============
    // =====================================

    @FXML
    private StackPane productCardStackPane;
    @FXML
    private VBox productCardVBox;
    @FXML
    private ImageView productImage;
    @FXML
    private Label productName;

    /* Drink Options UI */
    @FXML
    private HBox drinkOptions;
    @FXML
    private RadioButton hotOption;
    @FXML
    private RadioButton coldOption;
    @FXML
    private ToggleGroup drinkOptionsRadioBtn;

    /* Product Info UI */
    @FXML
    private Label productPrice;
    @FXML
    private Label diyAvailability;

    /* Stock Section UI */
    @FXML
    private HBox inStockItemsHBox;
    @FXML
    private Label stockQuantityLabel;
    @FXML
    private Label productStock;

    /* Quantity Stepper UI */
    @FXML
    private HBox qtyStepperHBox;
    @FXML
    private Button decreaseBtn;
    @FXML
    private Label quantityLabel;
    @FXML
    private Button increaseBtn;

    /* Action Buttons */
    @FXML
    private Button addToCartBtn;
    @FXML
    private Button addStockQtyBtn;

    // =====================================
    // =========== STATE FIELDS ============
    // =====================================

    private int quantity = 1;
    private int maxQty = Integer.MAX_VALUE;
    private Product currentProduct;
    private double drinkHotPrice;
    private double drinkColdPrice;
    private ChangeListener<Toggle> drinkToggleListener;
    private Label inactiveOverlay;
    private boolean excludeDiscountStock = false;

    // =====================================
    // ============= LIFECYCLE =============
    // =====================================

    @FXML
    private void initialize() {
        increaseBtn.setOnAction(e -> handleIncrementQuantity());
        decreaseBtn.setOnAction(e -> handleDecrementQuantity());

        addStockQtyBtn.setOnAction(e -> openEditProductPopup());
    }

    // =====================================
    // ======== PUBLIC DATA BINDING ========
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

    public void setDiscountedItemsData(Inventory inv) {
        disableDrinkOptions();
        disableDiyAvailability();
        showStockSection(true);

        Product product = ProductDAO.getProductById(inv.getProductId());
        if (product == null) return;

        loadProductImage(product.getImgPath());
        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(product.getPrice() * 0.4));

        int batchQty = inv.getQuantity();
        productStock.setText(String.valueOf(batchQty));

        stockQuantityLabel.setVisible(true);
        stockQuantityLabel.setManaged(true);

        setMaxQtyFromStock(batchQty);
        addToCartBtn.setDisable(batchQty <= 0);
        updateQtyButtonsState();
    }

    public void setCakeData(Product product) {
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
        setAddToCartDisable(product.getProductId());

        applyActiveState(product);
    }

    public void setDrinkData(Product product) {
        this.currentProduct = product;

        disableDiyAvailability();
        showStockSection(false);

        loadProductImage(product.getImgPath());
        productName.setText(product.getProductName());

        var drinks = DrinkDAO.getDrinksByProductId(product.getProductId());
        double base = product.getPrice();

        drinkHotPrice = base + drinks.getFirst().getPriceDelta();
        drinkColdPrice = base + drinks.getLast().getPriceDelta();

        ensureDrinkToggleGroup();
        installDrinkPriceListenerIfNeeded();

        hotOption.setSelected(true);
        productPrice.setText(String.valueOf(drinkHotPrice));

        // Drinks: no stock limit
        maxQty = Integer.MAX_VALUE;
        if (quantity < 1) quantity = 1;
        quantityLabel.setText(String.valueOf(quantity));
        updateQtyButtonsState();

        applyActiveState(product);
    }

    public void setBakedGoodsData(Product product) {
        this.currentProduct = product;

        disableDrinkOptions();
        disableDiyAvailability();
        showStockSection(true);

        loadProductImage(product.getImgPath());
        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(product.getPrice()));

        updateStockUI(product.getProductId());
        setAddToCartDisable(product.getProductId());

        applyActiveState(product);
    }

    public void setAccessoryData(Product product) {
        this.currentProduct = product;

        disableDrinkOptions();
        disableDiyAvailability();
        showStockSection(true);

        loadProductImage(product.getImgPath());
        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(product.getPrice()));

        updateStockUI(product.getProductId());
        setAddToCartDisable(product.getProductId());

        applyActiveState(product);
    }

    // =====================================
    // ======== STOCK & QTY LOGIC ==========
    // =====================================

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
                ? InventoryDAO.getRegularQuantityByProductId(productId)
                : InventoryDAO.getTotalAvailableQuantityByProductId(productId);
    }

    private void setMaxQtyFromStock(int stockQty) {
        this.maxQty = Math.max(stockQty, 0);

        if (maxQty == 0) {
            quantity = 0;
            quantityLabel.setText("0");
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
    // ========= UI STATE HELPERS ==========
    // =====================================

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
    // ========= RESOURCE METHODS ==========
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
    // ========= POPUPS & REFRESH ==========
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

        Product fresh = ProductDAO.getProductById(currentProduct.getProductId());
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