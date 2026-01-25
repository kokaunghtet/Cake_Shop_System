package com.cakeshopsystem.controllers;

import com.cakeshopsystem.controllers.admin.EditProductController;
import com.cakeshopsystem.models.Cake;
import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.cache.CakeCache;
import com.cakeshopsystem.utils.constants.CakeType;
import com.cakeshopsystem.utils.dao.CakeDAO;
import com.cakeshopsystem.utils.dao.DrinkDAO;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import com.cakeshopsystem.utils.dao.ProductDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.Objects;

public class ProductCardController {

    /* =========================================================
       FXML Nodes
       ========================================================= */
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

    @FXML private Button decreaseBtn;
    @FXML private Label quantityLabel;
    @FXML private Button increaseBtn;

    @FXML private Button addToCartBtn;
    @FXML private Button addStockQtyBtn;

    /* =========================================================
       State
       ========================================================= */
    private int quantity = 1;
    private int maxQty = Integer.MAX_VALUE;

    private Product currentProduct;

    /* =========================================================
       Lifecycle
       ========================================================= */
    @FXML
    private void initialize() {
        increaseBtn.setOnAction(e -> handleIncrementQuantity());
        decreaseBtn.setOnAction(e -> handleDecrementQuantity());

        // Admin-only button (visibility controlled by applyRoleBasedBtn)
        addStockQtyBtn.setOnAction(e -> openEditProductPopup());
    }

    /* =========================================================
       Role-based buttons
       ========================================================= */
    public void applyRoleBasedBtn(String roleName) {
        boolean isAdmin = "Admin".equalsIgnoreCase(roleName);

        addStockQtyBtn.setVisible(isAdmin);
        addStockQtyBtn.setManaged(isAdmin);

        addToCartBtn.setVisible(!isAdmin);
        addToCartBtn.setManaged(!isAdmin);
    }

    /* =========================================================
       Card Bindings
       ========================================================= */
    public void setDiscountedItemsData(Inventory inventory) {
        disableDrinkOptions();
        disableDiyAvailability();

        Product product = Objects.requireNonNull(ProductDAO.getProductById(inventory.getProductId()));
        this.currentProduct = product;

        loadProductImage(product.getImgPath());
        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(product.getPrice() * 0.4));

        updateStockUI(product.getProductId());
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
    }

    public void setDrinkData(Product product) {
        this.currentProduct = product;

        disableDiyAvailability();
        showStockSection(false);

        loadProductImage(product.getImgPath());
        productName.setText(product.getProductName());

        var drinks = DrinkDAO.getDrinksByProductId(product.getProductId());
        double base = product.getPrice();
        double hotPrice = base + drinks.getFirst().getPriceDelta();
        double coldPrice = base + drinks.getLast().getPriceDelta();

        ensureDrinkToggleGroup();

        hotOption.setSelected(true);
        productPrice.setText(String.valueOf(hotPrice));

        drinkOptionsRadioBtn.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) return;
            if (newToggle == hotOption) productPrice.setText(String.valueOf(hotPrice));
            if (newToggle == coldOption) productPrice.setText(String.valueOf(coldPrice));
        });

        // Drinks are made on order -> unlimited qty concept
        maxQty = Integer.MAX_VALUE;
        if (quantity < 1) quantity = 1;
        quantityLabel.setText(String.valueOf(quantity));
        updateQtyButtonsState();
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
    }

    /* =========================================================
       Popup: Edit Product (Admin)
       ========================================================= */
    private void openEditProductPopup() {
        if (currentProduct == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/EditProduct.fxml"));
            Parent root = loader.load();

            EditProductController controller = loader.getController();
            controller.setData(currentProduct);

            MainController.togglePopupContent(root);
        } catch (Exception ex) {
            System.err.println("Failed to open EditProduct popup: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    /* =========================================================
       Stock + Button State
       ========================================================= */
    private void updateStockUI(int productId) {
        int totalQty = InventoryDAO.getTotalAvailableQuantityByProductId(productId);
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

    private void setAddToCartDisable(int productId) {
        int totalQty = InventoryDAO.getTotalAvailableQuantityByProductId(productId);
        addToCartBtn.setDisable(totalQty <= 0);
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

    /* =========================================================
       Drink Options
       ========================================================= */
    private void ensureDrinkToggleGroup() {
        if (drinkOptionsRadioBtn == null) {
            drinkOptionsRadioBtn = new ToggleGroup();
        }
        hotOption.setToggleGroup(drinkOptionsRadioBtn);
        coldOption.setToggleGroup(drinkOptionsRadioBtn);

        drinkOptions.setVisible(true);
        drinkOptions.setManaged(true);
    }

    /* =========================================================
       Image
       ========================================================= */
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

        // 1) URL forms (http/file/jar)
        if (path.matches("^(https?|file|jar):.*")) {
            return new Image(path, true);
        }

        // 2) Classpath resource
        String res = path.startsWith("/") ? path : "/" + path;
        var url = getClass().getResource(res);
        if (url != null) {
            return new Image(url.toExternalForm(), true);
        }

        // 3) File system path
        var f = new java.io.File(path);
        if (f.exists()) {
            return new Image(f.toURI().toString(), true);
        }

        return null;
    }

    /* =========================================================
       Qty Stepper
       ========================================================= */
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

    /* =========================================================
       UI Helpers
       ========================================================= */
    private void disableDrinkOptions() {
        drinkOptions.setVisible(false);
        drinkOptions.setManaged(false);
    }

    private void disableDiyAvailability() {
        diyAvailability.setVisible(false);
        diyAvailability.setManaged(false);
    }

    private void showStockSection(boolean show) {
        inStockItemsHBox.setVisible(show);
        inStockItemsHBox.setManaged(show);
    }
}
