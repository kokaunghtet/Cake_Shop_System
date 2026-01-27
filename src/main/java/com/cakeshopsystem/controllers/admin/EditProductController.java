package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.controllers.MainController;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.cache.CakeCache;
import com.cakeshopsystem.utils.cache.DrinkCache;
import com.cakeshopsystem.utils.cache.ProductCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.CakeDAO;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import com.cakeshopsystem.utils.dao.ProductDAO;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import javafx.util.Duration;


import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class EditProductController {

    /* =========================================================
       FXML: Product Info
       ========================================================= */
    @FXML
    private ImageView previewImageView;
    @FXML
    private Button btnEditImage;

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtPrice;

    @FXML
    private Label diyLabel;
    @FXML
    private HBox diyHBox;
    @FXML
    private RadioButton rbDiyYes;
    @FXML
    private RadioButton rbDiyNo;

    @FXML
    private RadioButton rbActive;
    @FXML
    private RadioButton rbInactive;

    @FXML
    private Label lblExpireDate;

    /* =========================================================
       FXML: Shelf-life / Expiry UI
       ========================================================= */
    @FXML
    private Label lblShelfLifeDays;
    @FXML
    private TextField txtShelfLifeDays;
    @FXML
    private HBox expireDateHBox;

    /* =========================================================
       FXML: Stock Section
       ========================================================= */
    @FXML
    private VBox stockBox;
    @FXML
    private Label lblCurrentStock;
    @FXML
    private TextField txtStockQty;
    @FXML
    private Button btnAddStock;
    @FXML
    private Label lblExpireDateText;

    /* =========================================================
       FXML: Actions
       ========================================================= */
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnSave;

    /* =========================================================
       State
       ========================================================= */
    private Product currentProduct;
    private String selectedImagePath;

    private static final String DEFAULT_PRODUCT_IMAGE = "/images/default-product.png";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM d uuuu", Locale.ENGLISH);

    private Runnable onSaved;

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    /* =========================================================
       Lifecycle
       ========================================================= */
    @FXML
    private void initialize() {
        btnEditImage.setOnAction(e -> chooseAndPreviewImage());
        btnCancel.setOnAction(e -> MainController.handleClosePopupContent());
        btnSave.setOnAction(e -> handleSaveChanges());

        btnAddStock.setOnAction(e -> handleAddStock());
        setupShelfLifePreview();
    }

    /* =========================================================
       Public API
       ========================================================= */
    public void setData(Product product) {
        if (product == null) return;

        this.currentProduct = product;
        this.selectedImagePath = product.getImgPath(); // default: current image

        applyCategoryRules(product);
        bindFields(product);
        bindStock(product);
        bindDiyIfCake(product);
        bindAvailability(product);

        loadPreviewImage(selectedImagePath);
    }

    /* =========================================================
       Bindings
       ========================================================= */
    private void bindFields(Product p) {
        txtName.setText(p.getProductName());
        txtPrice.setText(String.valueOf(p.getPrice()));
    }

    private void bindAvailability(Product p) {
        if (p.isActive()) rbActive.setSelected(true);
        else rbInactive.setSelected(true);
    }

    private void bindStock(Product p) {
        int totalStock = InventoryDAO.getTotalAvailableQuantityByProductId(p.getProductId());
        lblCurrentStock.setText(String.valueOf(totalStock));
    }

    private void bindDiyIfCake(Product p) {
        if (!isCake(p)) return;

        var cake = CakeDAO.getCakeByProductId(p.getProductId());
        boolean diyAllowed = cake != null && cake.isDiyAllowed();

        rbDiyYes.setSelected(diyAllowed);
        rbDiyNo.setSelected(!diyAllowed);
    }

    private void applyCategoryRules(Product p) {
        if (!isCake(p)) hide(diyLabel, diyHBox);
        if (isDrink(p)) hide(stockBox);
        if (isAccessory(p)) hide(lblShelfLifeDays, txtShelfLifeDays, lblExpireDate, expireDateHBox);
    }

    private boolean isCake(Product p) {
        return p.getCategoryId() == 1;
    }

    private boolean isDrink(Product p) {
        return p.getCategoryId() == 2;
    }

    private boolean isAccessory(Product p) {
        return p.getCategoryId() == 4;
    }

    /* =========================================================
       Image
       ========================================================= */
    private void chooseAndPreviewImage() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Product Image");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        // Works on Windows/Linux/Mac (user.home exists everywhere)
        File userHome = new File(System.getProperty("user.home", "."));
        if (userHome.exists() && userHome.isDirectory()) {
            chooser.setInitialDirectory(userHome);
        }

        Window window = (btnEditImage.getScene() != null) ? btnEditImage.getScene().getWindow() : null;
        File file = chooser.showOpenDialog(window);
        if (file == null) return;

        selectedImagePath = file.getAbsolutePath();
        loadPreviewImage(selectedImagePath);
    }

    private void loadPreviewImage(String imagePath) {
        Image img = loadImageSmart(imagePath);

        if (img == null || img.isError()) {
            img = loadImageSmart(DEFAULT_PRODUCT_IMAGE);
        }

        if (img != null && !img.isError()) {
            previewImageView.setImage(img);
        }
    }

    private Image loadImageSmart(String path) {
        if (path == null || path.isBlank()) return null;

        try {
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
            File f = new File(path);
            if (f.exists()) {
                return new Image(f.toURI().toString(), true);
            }
        } catch (Exception ignored) {
            // return null below
        }

        return null;
    }

    /* =========================================================
       Save
       ========================================================= */
    private void handleSaveChanges() {
        if (currentProduct == null) return;

        String name = safeTrim(txtName.getText());
        String priceText = safeTrim(txtPrice.getText());

        if (name.isEmpty()) {
            showError("Name is required.");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
            if (price < 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Price must be a valid number (>= 0).");
            return;
        }

        boolean isActive = rbActive != null && rbActive.isSelected();

        String imgPathToSave = (selectedImagePath == null || selectedImagePath.isBlank())
                ? currentProduct.getImgPath()
                : selectedImagePath;

        int productId = currentProduct.getProductId();

        boolean ok = ProductDAO.updateProductEditFields(productId, name, price, isActive, imgPathToSave);
        if (!ok) {
            showError("Save failed. Please try again.");
            return;
        }

        if (isCake(currentProduct)) {
            boolean diyAllowed = rbDiyYes != null && rbDiyYes.isSelected();
            boolean cakeOk = CakeDAO.updateDiyAllowedIfPrebaked(productId, diyAllowed);
            if (!cakeOk) {
                showError("Save failed (cakes).");
                return;
            }
        }

        SnackBar.show(SnackBarType.SUCCESS, "", "Saved successfully.", Duration.seconds(2));

        ProductCache.refreshProducts();
        CakeCache.refreshCake();
        DrinkCache.refreshDrinks();

        if (onSaved != null) {
            onSaved.run();
        }

        MainController.handleClosePopupContent();
    }

    /* =========================================================
       Adding Stock
       ========================================================= */
    private void handleAddStock() {
        if (currentProduct == null) return;

        if (isDrink(currentProduct)) return;

        // 1) Quantity
        int qty;
        try {
            qty = Integer.parseInt(safeTrim(txtStockQty.getText()));
            if (txtStockQty.getText().isEmpty()) {
                showError("Quantity is required.");
                return;
            }
            if (qty <= 0) {
                showError("Quantity must be greater than 0.");
                return;
            }
        } catch (NumberFormatException ex) {
            showError("Quantity must be a valid integer.");
            return;
        }

        // 2) shelfLifeDays
        Integer shelfLifeDays = null;

        if (!isAccessory(currentProduct)) {
            String daysText = safeTrim(txtShelfLifeDays.getText());

            if (!daysText.isEmpty()) {
                try {
                    shelfLifeDays = Integer.parseInt(daysText);
                    if (shelfLifeDays < 0) {
                        showError("Shelf life days cannot be negative.");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showError("Shelf life days must be a valid integer.");
                    return;
                }
            } else {
                showError("Shelf life days is required.");
                return;
            }
        }

        // 3) Compute expiry date
        LocalDate expDate = computeLastGoodDate(shelfLifeDays);

        // 4) Save
        Integer userId = SessionManager.getUser() != null ? SessionManager.getUser().getUserId() : null;

        boolean ok = InventoryDAO.addStockBatch(currentProduct.getProductId(), qty, expDate, userId);
        if (!ok) {
            showError("Failed to add stock.");
            return;
        }

        // 5) Refresh UI
        int totalStock = InventoryDAO.getTotalAvailableQuantityByProductId(currentProduct.getProductId());
        lblCurrentStock.setText(String.valueOf(totalStock));

        txtStockQty.clear();
        SnackBar.show(SnackBarType.SUCCESS, "", "Stock added.", Duration.seconds(2));
    }


    /* =========================================================
       Small helpers
       ========================================================= */
    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    private void showError(String msg) {
        SnackBar.show(SnackBarType.ERROR, "Failed", msg, Duration.seconds(2));
    }

    private void hide(Node... nodes) {
        for (Node node : nodes) {
            if (node == null) continue;
            node.setVisible(false);
            node.setManaged(false);
        }
    }

    private LocalDate computeLastGoodDate(Integer shelfLifeDays) {
        if (shelfLifeDays == null) return null;

        if (shelfLifeDays < 1) return null;

        return LocalDate.now().plusDays(shelfLifeDays - 1);
    }

    private void setupShelfLifePreview() {
        if (txtShelfLifeDays == null || lblExpireDateText == null) return;

        updateExpirePreview(txtShelfLifeDays.getText());
        txtShelfLifeDays.textProperty().addListener((obs, oldVal, newVal) -> updateExpirePreview(newVal));
    }

    private void updateExpirePreview(String daysTextRaw) {
        // Accessories: no expiry concept
        if (currentProduct != null && isAccessory(currentProduct)) {
            lblExpireDateText.setText("-");
            return;
        }

        String text = safeTrim(daysTextRaw);

        // Required
        if (text.isEmpty()) {
            lblExpireDateText.setText("Required");
            return;
        }

        // Must be an integer
        final int days;
        try {
            days = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            lblExpireDateText.setText("Invalid");
            return;
        }

        // Must be >= 1
        LocalDate lastGood = computeLastGoodDate(days);
        if (lastGood == null) {
            lblExpireDateText.setText("Invalid");
            return;
        }

        lblExpireDateText.setText(lastGood.format(DATE_FMT));
    }

}
