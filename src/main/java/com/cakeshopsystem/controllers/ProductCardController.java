package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.cache.CakeCache;
import com.cakeshopsystem.utils.dao.DrinkDAO;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import com.cakeshopsystem.utils.dao.ProductDAO;
import javafx.fxml.FXML;
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
    @FXML
    public VBox productCardVBox;
    @FXML
    public ImageView productImage;
    @FXML
    public Label productName;

    @FXML
    public HBox drinkOptions;
    @FXML
    public RadioButton hotOption;
    @FXML
    public RadioButton coldOption;
    @FXML
    public ToggleGroup drinkOptionsRadioBtn;

    @FXML
    public Label productPrice;
    @FXML
    public Label diyAvailability;

    @FXML
    public HBox inStockItemsHBox;
    @FXML
    public Label stockQuantityLabel;
    @FXML
    public Label productStock;

    @FXML
    public Button decreaseBtn;
    @FXML
    public Label quantityLabel;
    @FXML
    public Button increaseBtn;
    @FXML
    public Button addToCartBtn;

    /* =========================================================
       State
       ========================================================= */
    private int quantity = 1;
    private int maxQty = Integer.MAX_VALUE;

    /* =========================================================
       Lifecycle
       ========================================================= */
    public void initialize() {
        increaseBtn.setOnAction(e -> handleIncrementQuantity());
        decreaseBtn.setOnAction(e -> handleDecrementQuantity());
    }

    /* =========================================================
       Card Bindings
       ========================================================= */
    public void setDiscountedItemsData(Inventory inventory) {
        disableDrinkOptions();
        disableDiyAvailability();

        Product product = Objects.requireNonNull(ProductDAO.getProductById(inventory.getProductId()));

        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(product.getPrice() * 0.4));

        updateStockUI(inventory.getProductId());
    }

    public void setCakeData(Product product) {
        disableDrinkOptions();

        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(product.getPrice()));

        if (Objects.requireNonNull(CakeCache.getCakeByProductId(product.getProductId())).isDiyAllowed()) {
            diyAvailability.setText("DIY Available");
        } else {
            disableDiyAvailability();
        }

        updateStockUI(product.getProductId());
        setBtnDisable(product);
        setRadioBtnDisable(product);
    }

    public void setDrinkData(Product product) {
        disableDiyAvailability();

        var drinks = DrinkDAO.getDrinksByProductId(product.getProductId());
        double base = product.getPrice();
        double hotPrice = base + drinks.getFirst().getPriceDelta();
        double coldPrice = base + drinks.getLast().getPriceDelta();

        productName.setText(product.getProductName());

        if (drinkOptionsRadioBtn == null) {
            drinkOptionsRadioBtn = new ToggleGroup();
        }
        hotOption.setToggleGroup(drinkOptionsRadioBtn);
        coldOption.setToggleGroup(drinkOptionsRadioBtn);

        hotOption.setSelected(true);
        productPrice.setText(String.valueOf(hotPrice));

        drinkOptionsRadioBtn.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) return;

            if (newToggle == hotOption) {
                productPrice.setText(String.valueOf(hotPrice));
            } else if (newToggle == coldOption) {
                productPrice.setText(String.valueOf(coldPrice));
            }
        });

        inStockItemsHBox.setVisible(false);
        inStockItemsHBox.setManaged(false);
    }

    public void setBakedGoodsData(Product product) {
        disableDrinkOptions();
        disableDiyAvailability();

        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(product.getPrice()));

        updateStockUI(product.getProductId());
        setBtnDisable(product);
        setRadioBtnDisable(product);
    }

    public void setAccessoryData(Product product) {
        disableDrinkOptions();
        disableDiyAvailability();

        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(product.getPrice()));

        updateStockUI(product.getProductId());
        setBtnDisable(product);
        setRadioBtnDisable(product);
    }

    /* =========================================================
       Stock + Button State
       ========================================================= */
    private void updateStockUI(int productId) {
        int totalQty = InventoryDAO.getTotalAvailableQuantityByProductId(productId);

        setMaxQtyFromStock(totalQty);

        if (totalQty > 0) {
            productStock.setText(String.valueOf(totalQty));
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

        updateQtyButtonsState();
    }

    private void setBtnDisable(Product product) {
        addToCartBtn.setDisable(InventoryDAO.getTotalAvailableQuantityByProductId(product.getProductId()) <= 0);
    }

    private void setRadioBtnDisable(Product product) {
        boolean outOfStock = maxQty <= 0;
        increaseBtn.setDisable(outOfStock || quantity >= maxQty);
        decreaseBtn.setDisable(outOfStock || quantity <= 1);
    }

    /* =========================================================
       Image
       ========================================================= */
    public void loadProductImage(String imagePath) {
        try {
            Image image = new Image(imagePath, true);
            productImage.setImage(image);
        } catch (Exception err) {
            System.err.println("Error loading product image : " + err.getLocalizedMessage());
        }
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


    /* =========================================================
       UI Helpers
       ========================================================= */
    public void disableDrinkOptions() {
        drinkOptions.setVisible(false);
        drinkOptions.setManaged(false);
    }

    public void disableDiyAvailability() {
        diyAvailability.setVisible(false);
        diyAvailability.setManaged(false);
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

    private void updateQtyButtonsState() {
        boolean outOfStock = maxQty <= 0;

        increaseBtn.setDisable(outOfStock || quantity >= maxQty);
        decreaseBtn.setDisable(outOfStock || quantity <= 1);
    }
}
