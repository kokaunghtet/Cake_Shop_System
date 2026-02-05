package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Flavour;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.models.Size;
import com.cakeshopsystem.models.Topping;
import com.cakeshopsystem.utils.cache.ProductCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.CakeShape;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.FlavourDAO;
import com.cakeshopsystem.utils.dao.SizeDAO;
import com.cakeshopsystem.utils.dao.ToppingDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.time.LocalDate;

public class CustomOrderFormController {

    // =================================================
    // FXML UI components
    // =================================================
    @FXML private DatePicker dpPickupDate;
    @FXML private ComboBox<Flavour> cbFlavour;
    @FXML private ComboBox<Topping> cbTopping;
    @FXML private ComboBox<Size> cbSize;
    @FXML private ComboBox<CakeShape> cbShape;
    @FXML private TextArea taMessage;
    @FXML private Label lblPrice;
    @FXML private Button btnConfirm;
    @FXML private ImageView cakeImage;

    // =================================================
    // Constants
    // =================================================
    private static final double MESSAGE_FEE = 5000;
    private static final String DEFAULT_PRODUCT_IMAGE = "/images/default-product.png";

    // =================================================
    // State
    // =================================================
    private Product baseCakeProduct;

    // =================================================
    // JavaFX lifecycle
    // =================================================
    @FXML
    public void initialize() {
        initCakeCombos();
        initCakePriceAutoCalc();
        updateCakePrice();

        blockPastDates(dpPickupDate);

        btnConfirm.setOnAction(e -> handleAddToCart());
    }

    // =================================================
    // Date handling
    // =================================================
    public static void blockPastDates(DatePicker datePicker) {
        LocalDate today = LocalDate.now();

        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) return;

                boolean isPast = item.isBefore(today);
                setDisable(isPast);
                setStyle(isPast ? "-fx-opacity: 0.4;" : "");
            }
        });

        datePicker.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.isBefore(today)) datePicker.setValue(null);
        });
    }

    // =================================================
    // Confirm / add-to-cart flow
    // =================================================
    private void handleAddToCart() {

        markInvalid(dpPickupDate, dpPickupDate.getValue() == null);
        markInvalid(cbFlavour, cbFlavour.getValue() == null);
        markInvalid(cbSize, cbSize.getValue() == null);
        markInvalid(cbShape, cbShape.getValue() == null);

        if (dpPickupDate.getValue() == null) { showError("Pickup date is required."); return; }
        if (cbFlavour.getValue() == null) { showError("Flavour is required."); return; }
        if (cbSize.getValue() == null) { showError("Size is required."); return; }
        if (cbShape.getValue() == null) { showError("Shape is required."); return; }

        Product customCakeProduct = ProductCache.getProductsByCategoryId(1)
                .stream()
                .filter(p -> "Custom Cake".equalsIgnoreCase(p.getProductName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing 'Custom Cake' product in DB"));

        int cakeIdForBooking = resolveCakeIdForBooking(customCakeProduct);

        Flavour flavour = cbFlavour.getValue();
        Size size = cbSize.getValue();
        CakeShape shape = cbShape.getValue();
        Topping topping = cbTopping.getValue();

        double total = flavour.getPrice() + size.getPrice();
        if (topping != null) total += topping.getPrice();

        String msg = taMessage.getText();
        if (msg != null) {
            msg = msg.trim();
            if (msg.isEmpty()) msg = null;
            else if (msg.length() > 100) msg = msg.substring(0, 100);
        }
        if (msg != null) total += MESSAGE_FEE;

        String displayName = (topping == null)
                ? String.format("Custom %d\" %s (%s)", size.getSizeInches(), shortShape(shape), flavour.getFlavourName())
                : String.format("Custom %d\" %s (%s + %s)", size.getSizeInches(), shortShape(shape), flavour.getFlavourName(), topping.getToppingName());

        Integer toppingId = (topping == null) ? null : topping.getToppingId();

        com.cakeshopsystem.utils.services.CartService.getInstance().addOrReplaceCustomCake(
                customCakeProduct,
                displayName,
                total,
                cakeIdForBooking,
                dpPickupDate.getValue(),
                msg,
                flavour.getFlavourId(),
                toppingId,
                size.getSizeId(),
                shape
        );

        MainController.handleClosePopupContent();
    }

    // =================================================
    // Booking & cake resolution
    // =================================================
    private int resolveCakeIdForBooking(Product customCakeProduct) {
        if (baseCakeProduct != null) {
            var baseCake = com.cakeshopsystem.utils.cache.CakeCache.getCakeByProductId(baseCakeProduct.getProductId());
            if (baseCake == null) throw new IllegalStateException("Missing cake row for selected base cake.");
            return baseCake.getCakeId();
        }

        var template = com.cakeshopsystem.utils.cache.CakeCache.getCakeByProductId(customCakeProduct.getProductId());
        if (template == null) {
            throw new IllegalStateException(
                    "Missing cake row for product 'Custom Cake'. Create a template row in cakes table for the Custom Cake product."
            );
        }
        return template.getCakeId();
    }

    private String shortShape(CakeShape shape) {
        return switch (shape) {
            case CIRCLE -> "C";
            case SQUARE -> "S";
            case HEART  -> "H";
        };
    }

    // =================================================
    // Validation helpers
    // =================================================
    private void markInvalid(Control c, boolean invalid) {
        if (c == null) return;
        c.setStyle(invalid ? "-fx-border-color: #e74c3c; -fx-border-width: 1.2;" : "");
    }

    // =================================================
    // ComboBox initialization
    // =================================================
    private void initCakeCombos() {
        cbFlavour.setItems(FlavourDAO.getAllFlavours());
        cbTopping.setItems(ToppingDAO.getAllToppings());
        cbSize.setItems(SizeDAO.getAllSizes());
        cbShape.setItems(FXCollections.observableArrayList(CakeShape.values()));

        setupComboBox(cbFlavour, Flavour::getFlavourName);
        setupComboBox(cbTopping, Topping::getToppingName);
        setupComboBox(cbSize, s -> s.getSizeInches() + "\"");
    }

    private <T> void setupComboBox(ComboBox<T> combo, java.util.function.Function<T, String> textFn) {
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textFn.apply(item));
            }
        });

        combo.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : textFn.apply(item));
            }
        });
    }

    // =================================================
    // Price calculation
    // =================================================
    private void initCakePriceAutoCalc() {
        cbFlavour.valueProperty().addListener((obs, o, n) -> updateCakePrice());
        cbTopping.valueProperty().addListener((obs, o, n) -> updateCakePrice());
        cbSize.valueProperty().addListener((obs, o, n) -> updateCakePrice());
        taMessage.textProperty().addListener((obs, o, n) -> updateCakePrice());
    }

    private void updateCakePrice() {
        Flavour f = cbFlavour.getValue();
        Topping t = cbTopping.getValue();
        Size s = cbSize.getValue();

        double total = 0;
        if (f != null) total += f.getPrice();
        if (t != null) total += t.getPrice();
        if (s != null) total += s.getPrice();

        String msg = taMessage.getText();
        if (msg != null && !msg.trim().isEmpty()) {
            total += MESSAGE_FEE;
        }

        lblPrice.setText(total <= 0 ? "" : String.format("%.0f", total));
    }

    private double computeCakePrice(Flavour flavour, Topping topping, Size size) {
        return flavour.getPrice() + topping.getPrice() + size.getPrice();
    }

    // =================================================
    // Base cake preset handling
    // =================================================
    public void setBaseCake(Product p) {
        this.baseCakeProduct = p;

        if (p == null) {
            Image img = loadImageSmart(DEFAULT_PRODUCT_IMAGE);
            if (img != null) cakeImage.setImage(img);

            cbFlavour.setValue(null);
            cbTopping.setValue(null);
            cbSize.setValue(null);
            cbShape.setValue(null);
            taMessage.clear();
            dpPickupDate.setValue(null);

            updateCakePrice();
            return;
        }

        String path = (p.getImgPath() == null || p.getImgPath().isBlank())
                ? DEFAULT_PRODUCT_IMAGE
                : p.getImgPath();

        Image img = loadImageSmart(path);
        if (img == null || img.isError()) img = loadImageSmart(DEFAULT_PRODUCT_IMAGE);
        cakeImage.setImage(img);

        var baseCake = com.cakeshopsystem.utils.cache.CakeCache.getCakeByProductId(p.getProductId());
        if (baseCake == null) return;

        selectFlavourById(baseCake.getFlavourId());
        selectSizeById(baseCake.getSizeId());
        cbShape.setValue(baseCake.getShape());

        updateCakePrice();
    }

    // =================================================
    // Selection helpers
    // =================================================
    private void selectFlavourById(Integer id) {
        if (id == null) return;
        for (Flavour f : cbFlavour.getItems()) {
            if (f.getFlavourId() == id) {
                cbFlavour.setValue(f);
                return;
            }
        }
    }

    private void selectSizeById(Integer id) {
        if (id == null) return;
        for (Size s : cbSize.getItems()) {
            if (s.getSizeId() == id) {
                cbSize.setValue(s);
                return;
            }
        }
    }

    // =================================================
    // Image loading
    // =================================================
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

    // =================================================
    // Error feedback
    // =================================================
    private void showError(String body) {
        SnackBar.show(SnackBarType.ERROR, "Failed!", body, Duration.seconds(2));
    }

}
