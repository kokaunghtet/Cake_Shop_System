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
    @FXML public VBox productCardVBox;
    @FXML public ImageView productImage;
    @FXML public Label productName;

    @FXML public HBox drinkOptions;
    @FXML public RadioButton hotOption;
    @FXML public RadioButton coldOption;
    @FXML public ToggleGroup drinkOptionsRadioBtn;

    @FXML public Label productPrice;
    @FXML public Label diyAvailability;

    @FXML public HBox inStockItemsHBox;
    @FXML public Label stockQuantityLabel;
    @FXML public Label productStock;

    @FXML public Button decreaseBtn;
    @FXML public Label quantityLabel;
    @FXML public Button increaseBtn;
    @FXML public Button addToCartBtn;

    /* =========================================================
       State
       ========================================================= */
    private int quantity = 1;

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
    }

//    public void setDrinkData(Product product) {
//        disableDiyAvailability();
//
//        var drinks = DrinkDAO.getDrinksByProductId(product.getProductId());
//        double base = product.getPrice();
//        double hot = base + drinks.getFirst().getPriceDelta();
//        double cold = base + drinks.getLast().getPriceDelta();
//
//        productName.setText(product.getProductName());
//        productPrice.setText(String.valueOf(base));
//
//        inStockItemsHBox.setVisible(false);
//        inStockItemsHBox.setManaged(false);
//    }
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
    }

    public void setAccessoryData(Product product) {
        disableDrinkOptions();
        disableDiyAvailability();

        productName.setText(product.getProductName());
        productPrice.setText(String.valueOf(product.getPrice()));

        updateStockUI(product.getProductId());
        setBtnDisable(product);
    }

    /* =========================================================
       Stock + Button State
       ========================================================= */
    private void updateStockUI(int productId) {
        int totalQty = InventoryDAO.getTotalAvailableQuantityByProductId(productId);

        if (totalQty > 0) {
            productStock.setText(String.valueOf(totalQty));
        } else {
            productStock.setText("Out of Stock");
            productStock.getStyleClass().add("badge-danger");

            stockQuantityLabel.setVisible(false);
            stockQuantityLabel.setManaged(false);
        }
    }

    private void setBtnDisable(Product product) {
        addToCartBtn.setDisable(InventoryDAO.getTotalAvailableQuantityByProductId(product.getProductId()) <= 0);
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
    private void handleDecrementQuantity() {
        if (quantity > 1) {
            quantity--;
            quantityLabel.setText(String.valueOf(quantity));
        }
    }

    private void handleIncrementQuantity() {
        if (quantity < 99) {
            quantity++;
            quantityLabel.setText(String.valueOf(quantity));
        }
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
}
