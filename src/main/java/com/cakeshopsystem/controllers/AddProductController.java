package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Flavour;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.models.Size;
import com.cakeshopsystem.models.Topping;
import com.cakeshopsystem.utils.cache.CakeCache;
import com.cakeshopsystem.utils.cache.DrinkCache;
import com.cakeshopsystem.utils.cache.ProductCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.CakeShape;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;

import java.io.File;

public class AddProductController {

    // =====================================
    // ============ FXML FIELDS ============
    // =====================================

    /* Category Selector */
    @FXML
    private ToggleGroup tgCategory;
    @FXML
    private RadioButton rbCake;
    @FXML
    private RadioButton rbDrink;
    @FXML
    private RadioButton rbBaked;
    @FXML
    private RadioButton rbAccessory;

    /* Common Product Fields */
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtPrice;
    @FXML
    private Button btnChooseImage;
    @FXML
    private Label lblImagePath;
    @FXML
    private ImageView chooseImageView;

    /* Category Containers */
    @FXML
    private StackPane categoryStack;
    @FXML
    private VBox cakeBox;

    /* Cake-Specific Fields */
    @FXML
    private ComboBox<Flavour> cbFlavour;
    @FXML
    private ComboBox<Topping> cbTopping;
    @FXML
    private ComboBox<Size> cbSize;
    @FXML
    private ComboBox<CakeShape> cbShape;
    @FXML
    private ToggleGroup tgDiy;
    @FXML
    private RadioButton rbDiyYes;
    @FXML
    private RadioButton rbDiyNo;

    /* Actions */
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnSave;

    // =====================================
    // =========== STATE FIELDS ============
    // =====================================

    private String selectedImagePath;
    private static final String DEFAULT_PRODUCT_IMAGE = "/images/default-product.jpg";
    private Runnable onSaved;

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    // =====================================
    // ============= LIFECYCLE =============
    // =====================================

    @FXML
    private void initialize() {
        bindManagedToVisible(cakeBox);

        initCategorySwitcher();
        initCakeCombos();
        initCakePriceAutoCalc();

        btnChooseImage.setOnAction(e -> chooseAndPreviewImage());
        btnCancel.setOnAction(e -> MainController.handleClosePopupContent());
        btnSave.setOnAction(e -> handleSaveProduct());

        // Defaults
        if (rbCake != null) rbCake.setSelected(true);
        if (rbDiyNo != null) rbDiyNo.setSelected(true);

        applyCategoryUI();
        applyPriceMode();
        updateCakePrice();
        loadPreviewImage(DEFAULT_PRODUCT_IMAGE);
        lblImagePath.setText("No file selected");
    }

    // =====================================
    // ======= INITIALIZATION LOGIC ========
    // =====================================

    private void initCategorySwitcher() {
        if (tgCategory == null) return;

        tgCategory.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            applyCategoryUI();
            applyPriceMode();
            updateCakePrice();
        });
    }

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

    private void initCakePriceAutoCalc() {
        cbFlavour.valueProperty().addListener((obs, o, n) -> updateCakePrice());
        cbTopping.valueProperty().addListener((obs, o, n) -> updateCakePrice());
        cbSize.valueProperty().addListener((obs, o, n) -> updateCakePrice());
    }

    // =====================================
    // ======== CATEGORY & PRICE LOGIC =====
    // =====================================

    private void applyCategoryUI() {
        boolean isCake = rbCake != null && rbCake.isSelected();
        cakeBox.setVisible(isCake);
    }

    private void applyPriceMode() {
        boolean isCakeSelected = rbCake != null && rbCake.isSelected();

        if (isCakeSelected) {
            txtPrice.setEditable(false);
            txtPrice.setFocusTraversable(false);
            txtPrice.setPromptText("Auto-calculated");
        } else {
            txtPrice.setEditable(true);
            txtPrice.setFocusTraversable(true);
            txtPrice.setPromptText("e.g. 3500");
            txtPrice.clear();
        }
    }

    private void updateCakePrice() {
        if (rbCake == null || !rbCake.isSelected()) return;

        Flavour f = cbFlavour.getValue();
        Topping t = cbTopping.getValue();
        Size s = cbSize.getValue();

        double total = 0;
        if (f != null) total += f.getPrice();
        if (t != null) total += t.getPrice();
        if (s != null) total += s.getPrice();

        txtPrice.setText(total <= 0 ? "" : String.format("%.0f", total));
    }

    private double computeCakePrice(Flavour flavour, Topping topping, Size size) {
        return flavour.getPrice() + topping.getPrice() + size.getPrice();
    }

    // =====================================
    // ========== SAVE HANDLERS ============
    // =====================================

    private void handleSaveProduct() {
        boolean ok = false;

        if (rbCake.isSelected()) ok = saveCake();
        else if (rbDrink.isSelected()) ok = saveDrink();
        else if (rbBaked.isSelected()) ok = saveBakedGood();
        else if (rbAccessory.isSelected()) ok = saveAccessory();

        if (!ok) return;

        ProductCache.refreshProducts();
        CakeCache.refreshCake();
        DrinkCache.refreshDrinks();

        if (onSaved != null) onSaved.run();

        MainController.handleClosePopupContent();
    }

    private boolean saveCake() {
        try {
            String name = requiredText(txtName, "Name");

            Flavour flavour = requiredValue(cbFlavour, "Flavour");
            Topping topping = requiredValue(cbTopping, "Topping");
            Size size = requiredValue(cbSize, "Size");
            CakeShape shape = requiredValue(cbShape, "Shape");

            double price = computeCakePrice(flavour, topping, size);
            boolean isDiy = rbDiyYes != null && rbDiyYes.isSelected();

            String imgPathToSave = (selectedImagePath == null || selectedImagePath.isBlank())
                    ? DEFAULT_PRODUCT_IMAGE
                    : selectedImagePath;

            Product p = new Product();
            p.setProductName(name);
            p.setCategoryId(1);
            p.setPrice(price);
            p.setActive(true);
            p.setTrackInventory(true);
            p.setShelfLifeDays(null);
            p.setImgPath(imgPathToSave);

            boolean ok = CakeDAO.insertPrebakedCakeWithProduct(
                    p,
                    flavour.getFlavourId(),
                    topping.getToppingId(),
                    size.getSizeId(),
                    "Prebaked",
                    shape.toString(),
                    isDiy
            );

            if (!ok) {
                SnackBar.show(SnackBarType.ERROR, "Failed", "Failed to save cake.", Duration.seconds(2));
                return false;
            }

            SnackBar.show(SnackBarType.SUCCESS, "", "Cake saved successfully.", Duration.seconds(2));
            return true;

        } catch (IllegalArgumentException ex) {
            SnackBar.show(SnackBarType.ERROR, "Failed", ex.getMessage(), Duration.seconds(2));
            return false;
        }
    }

    private boolean saveDrink() {
        try {
            String name = requiredText(txtName, "Name");
            double basePrice = requiredPrice(txtPrice);

            String imgPathToSave = (selectedImagePath == null || selectedImagePath.isBlank())
                    ? DEFAULT_PRODUCT_IMAGE
                    : selectedImagePath;

            Product p = new Product();
            p.setProductName(name);
            p.setCategoryId(2);
            p.setPrice(basePrice);
            p.setActive(true);
            p.setTrackInventory(false);
            p.setShelfLifeDays(null);
            p.setImgPath(imgPathToSave);

            boolean ok = DrinkDAO.insertDrinkWithProduct(p);
            if (!ok) {
                SnackBar.show(SnackBarType.ERROR, "Failed", "Failed to save drink.", Duration.seconds(2));
                return false;
            }

            SnackBar.show(SnackBarType.SUCCESS, "", "Drink saved successfully.", Duration.seconds(2));
            return true;

        } catch (IllegalArgumentException ex) {
            SnackBar.show(SnackBarType.ERROR, "Failed", ex.getMessage(), Duration.seconds(2));
            return false;
        }
    }

    private boolean saveBakedGood() {
        try {
            String name = requiredText(txtName, "Name");
            double basePrice = requiredPrice(txtPrice);

            String imgPathToSave = (selectedImagePath == null || selectedImagePath.isBlank())
                    ? DEFAULT_PRODUCT_IMAGE
                    : selectedImagePath;

            Product p = new Product();
            p.setProductName(name);
            p.setCategoryId(3);
            p.setPrice(basePrice);
            p.setActive(true);
            p.setTrackInventory(true);
            p.setShelfLifeDays(null);
            p.setImgPath(imgPathToSave);

            boolean ok = ProductDAO.insertProduct(p);
            if (!ok) {
                SnackBar.show(SnackBarType.ERROR, "Failed", "Failed to save baked goods.", Duration.seconds(2));
                return false;
            }

            SnackBar.show(SnackBarType.SUCCESS, "", "Baked goods saved successfully.", Duration.seconds(2));
            return true;

        } catch (IllegalArgumentException ex) {
            SnackBar.show(SnackBarType.ERROR, "Failed", ex.getMessage(), Duration.seconds(2));
            return false;
        }
    }

    private boolean saveAccessory() {
        try {
            String name = requiredText(txtName, "Name");
            double basePrice = requiredPrice(txtPrice);

            String imgPathToSave = (selectedImagePath == null || selectedImagePath.isBlank())
                    ? DEFAULT_PRODUCT_IMAGE
                    : selectedImagePath;

            Product p = new Product();
            p.setProductName(name);
            p.setCategoryId(4);
            p.setPrice(basePrice);
            p.setActive(true);
            p.setTrackInventory(true);
            p.setShelfLifeDays(null);
            p.setImgPath(imgPathToSave);

            boolean ok = ProductDAO.insertProduct(p);
            if (!ok) {
                SnackBar.show(SnackBarType.ERROR, "Failed", "Failed to save accessory.", Duration.seconds(2));
                return false;
            }

            SnackBar.show(SnackBarType.SUCCESS, "", "Accessory saved successfully.", Duration.seconds(2));
            return true;

        } catch (IllegalArgumentException ex) {
            SnackBar.show(SnackBarType.ERROR, "Failed", ex.getMessage(), Duration.seconds(2));
            return false;
        }
    }

    // =====================================
    // ========== IMAGE MANAGEMENT =========
    // =====================================

    private void chooseAndPreviewImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Product Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        File userHome = new File(System.getProperty("user.home", "."));
        if (userHome.exists() && userHome.isDirectory()) {
            chooser.setInitialDirectory(userHome);
        }

        Window window = (btnChooseImage.getScene() != null) ? btnChooseImage.getScene().getWindow() : null;
        File file = chooser.showOpenDialog(window);
        if (file == null) return;

        selectedImagePath = file.getAbsolutePath();
        lblImagePath.setText(file.getName());
        loadPreviewImage(selectedImagePath);
    }

    private void loadPreviewImage(String imagePath) {
        Image img = loadImageSmart(imagePath);
        if (img == null || img.isError()) img = loadImageSmart(DEFAULT_PRODUCT_IMAGE);

        if (img != null && !img.isError() && chooseImageView != null) {
            chooseImageView.setImage(img);
        }
    }

    private Image loadImageSmart(String path) {
        if (path == null || path.isBlank()) return null;

        try {
            if (path.matches("^(https?|file|jar):.*")) return new Image(path, true);

            String res = path.startsWith("/") ? path : "/" + path;
            var url = getClass().getResource(res);
            if (url != null) return new Image(url.toExternalForm(), true);

            File f = new File(path);
            if (f.exists()) return new Image(f.toURI().toString(), true);
        } catch (Exception ignored) {
        }

        return null;
    }

    // =====================================
    // ========= VALIDATION HELPERS ========
    // =====================================

    private String requiredText(TextField tf, String fieldName) {
        String v = tf.getText() == null ? "" : tf.getText().trim();
        if (v.isEmpty()) throw new IllegalArgumentException(fieldName + " is required.");
        return v;
    }

    private double requiredPrice(TextField tf) {
        String raw = requiredText(tf, "Price");

        boolean ok = raw.matches("^\\d+$") || raw.matches("^\\d{1,3}(,\\d{3})+$");
        if (!ok) {
            throw new IllegalArgumentException("Price must be numbers only (example: 3500 or 3,500).");
        }

        String cleaned = raw.replace(",", "");
        try {
            double price = Double.parseDouble(cleaned);
            if (price < 0) throw new NumberFormatException();
            return price;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Price must be a valid number (>= 0).");
        }
    }

    private <T> T requiredValue(ComboBox<T> cb, String fieldName) {
        T v = cb.getValue();
        if (v == null) throw new IllegalArgumentException(fieldName + " is required.");
        return v;
    }

    private void bindManagedToVisible(Region... regions) {
        for (Region r : regions) {
            if (r == null) continue;
            r.managedProperty().bind(r.visibleProperty());
        }
    }
}