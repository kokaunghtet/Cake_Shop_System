package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.controllers.MainController;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.cache.CakeCache;
import com.cakeshopsystem.utils.cache.DrinkCache;
import com.cakeshopsystem.utils.cache.InventoryCache;
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

    // =====================================
    // ============ FXML FIELDS ============
    // =====================================

    /* Product Info Section */
    @FXML
    private ImageView previewImageView;
    @FXML
    private Button btnEditImage;
    @FXML
    private TextField txtName;
    @FXML
    private TextField txtPrice;

    /* DIY Options (Cakes) */
    @FXML
    private Label diyLabel;
    @FXML
    private HBox diyHBox;
    @FXML
    private RadioButton rbDiyYes;
    @FXML
    private RadioButton rbDiyNo;

    /* Product Status */
    @FXML
    private RadioButton rbActive;
    @FXML
    private RadioButton rbInactive;

    /* Shelf-life / Expiry UI */
    @FXML
    private Label lblShelfLifeDays;
    @FXML
    private TextField txtShelfLifeDays;
    @FXML
    private HBox expireDateHBox;
    @FXML
    private Label lblExpireDate;
    @FXML
    private Label lblExpireDateText;

    /* Stock Section */
    @FXML
    private VBox stockBox;
    @FXML
    private Label lblCurrentStock;
    @FXML
    private TextField txtStockQty;
    @FXML
    private Button btnAddStock;

    /* Global Actions */
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnSave;

    // =====================================
    // =========== STATE FIELDS ============
    // =====================================

    private Product currentProduct;
    private String selectedImagePath;
    private Runnable onSaved;

    private static final String DEFAULT_PRODUCT_IMAGE = "/images/default-product.png";
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("MMM d uuuu", Locale.ENGLISH);

    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    // =====================================
    // ============= LIFECYCLE =============
    // =====================================

    @FXML
    private void initialize() {
        btnEditImage.setOnAction(e -> chooseAndPreviewImage());
        btnCancel.setOnAction(e -> MainController.handleClosePopupContent());
        btnSave.setOnAction(e -> handleSaveChanges());

        btnAddStock.setOnAction(e -> handleAddStock());
        setupShelfLifePreview();
    }

    // =====================================
    // ======== PUBLIC API & BINDING =======
    // =====================================

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

    private void bindFields(Product p) {
        txtName.setText(p.getProductName());
        txtPrice.setText(String.valueOf(p.getPrice()));
    }

    private void bindAvailability(Product p) {
        if (p.isActive()) rbActive.setSelected(true);
        else rbInactive.setSelected(true);
    }

    private void bindStock(Product p) {
//        int totalStock = InventoryDAO.getTotalAvailableQuantityByProductId(p.getProductId());
//        lblCurrentStock.setText(String.valueOf(totalStock));
        int totalStock = InventoryCache.getTotalQtyByProductId(p.getProductId());
        lblCurrentStock.setText(String.valueOf(totalStock));
    }

    private void bindDiyIfCake(Product p) {
        if (!isCake(p)) return;

        var cake = CakeCache.getCakeByProductId(p.getProductId());
        boolean diyAllowed = cake != null && cake.isDiyAllowed();

        rbDiyYes.setSelected(diyAllowed);
        rbDiyNo.setSelected(!diyAllowed);
    }

    private void applyCategoryRules(Product p) {
        if (!isCake(p)) hide(diyLabel, diyHBox);
        if (isDrink(p)) hide(stockBox);
        if (isAccessory(p)) hide(lblShelfLifeDays, txtShelfLifeDays, lblExpireDate, expireDateHBox);
    }

    // =====================================
    // ========= SAVE & STOCK LOGIC ========
    // =====================================

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

        ProductCache.refreshProducts();

        if (isCake(currentProduct)) {
            boolean diyAllowed = rbDiyYes != null && rbDiyYes.isSelected();
            boolean cakeOk = CakeDAO.updateDiyAllowedIfPrebaked(productId, diyAllowed);
            if (!cakeOk) {
                showError("Save failed (cakes).");
                return;
            }
            CakeCache.refreshCake();
        }

        SnackBar.show(SnackBarType.SUCCESS, "Updated", "Changes to the product were saved.", Duration.seconds(2));

        if (onSaved != null) {
            onSaved.run();
        }

        MainController.handleClosePopupContent();
    }

    private void handleAddStock() {
        if (currentProduct == null) return;
        if (isDrink(currentProduct)) return;

        // 1) Quantity validation
        if (txtStockQty.getText().isEmpty()) {
            showError("Quantity is required.");
            return;
        }
        int qty;
        try {
            qty = Integer.parseInt(safeTrim(txtStockQty.getText()));
            if (qty <= 0) {
                showError("Quantity must be greater than 0.");
                return;
            }
        } catch (NumberFormatException ex) {
            showError("Quantity must be a valid integer.");
            return;
        }

        // 2) shelfLifeDays validation
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

        // 4) Save to Database
        Integer userId = SessionManager.getUser() != null ? SessionManager.getUser().getUserId() : null;
        boolean ok = InventoryDAO.addStockBatch(currentProduct.getProductId(), qty, expDate, userId);
        if (!ok) {
            showError("Failed to add stock.");
            return;
        }

        InventoryCache.refresh();

        // 5) Refresh UI
//        int totalStock = InventoryDAO.getTotalAvailableQuantityByProductId(currentProduct.getProductId());
//        lblCurrentStock.setText(String.valueOf(totalStock));
        int totalStock = InventoryCache.getTotalQtyByProductId(currentProduct.getProductId());
        lblCurrentStock.setText(String.valueOf(totalStock));

        txtStockQty.clear();
        txtShelfLifeDays.clear();
        SnackBar.show(SnackBarType.SUCCESS, "Stock updated", "Inventory was updated.", Duration.seconds(2));

        if (onSaved != null) {
            onSaved.run();
        }
    }

    // =====================================
    // ========== IMAGE PROCESSING =========
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
            if (path.matches("^(https?|file|jar):.*")) return new Image(path, true);

            String res = path.startsWith("/") ? path : "/" + path;
            var url = getClass().getResource(res);
            if (url != null) return new Image(url.toExternalForm(), true);

            File f = new File(path);
            if (f.exists()) return new Image(f.toURI().toString(), true);
        } catch (Exception ignored) {}
        return null;
    }

    // =====================================
    // =========== UTILITY HELPERS =========
    // =====================================

    private boolean isCake(Product p)      { return p.getCategoryId() == 1; }
    private boolean isDrink(Product p)     { return p.getCategoryId() == 2; }
    private boolean isAccessory(Product p) { return p.getCategoryId() == 4; }

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
        if (shelfLifeDays == null || shelfLifeDays < 1) return null;
        return LocalDate.now().plusDays(shelfLifeDays - 1);
    }

    private void setupShelfLifePreview() {
        if (txtShelfLifeDays == null || lblExpireDateText == null) return;
        updateExpirePreview(txtShelfLifeDays.getText());
        txtShelfLifeDays.textProperty().addListener((obs, oldVal, newVal) -> updateExpirePreview(newVal));
    }

    private void updateExpirePreview(String daysTextRaw) {
        if (currentProduct != null && isAccessory(currentProduct)) {
            lblExpireDateText.setText("-");
            return;
        }
        String text = safeTrim(daysTextRaw);
        if (text.isEmpty()) {
            lblExpireDateText.setText("Required");
            return;
        }
        final int days;
        try {
            days = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            lblExpireDateText.setText("Invalid");
            return;
        }
        LocalDate lastGood = computeLastGoodDate(days);
        if (lastGood == null) {
            lblExpireDateText.setText("Invalid");
            return;
        }
        lblExpireDateText.setText(lastGood.format(DATE_FMT));
    }
}