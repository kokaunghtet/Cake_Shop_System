package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.CartItem;
import com.cakeshopsystem.models.Payment;
import com.cakeshopsystem.models.ReceiptData;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import com.cakeshopsystem.utils.dao.PaymentDAO;
import com.cakeshopsystem.utils.services.CartService;
import com.cakeshopsystem.utils.services.OrderService;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.animation.PauseTransition;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller responsible for managing the shopping cart UI,
 * handling quantity adjustments, and finalizing orders.
 */
public class CartController {

    // ---------------------------------------------------------
    // FXML UI Components
    // ---------------------------------------------------------
    @FXML private Label lblTotalAmount;
    @FXML private ComboBox<Payment> cbPaymentOptions;
    @FXML private Button confirmOrderBtn;
    @FXML private TableView<CartItem> tblCart;
    @FXML private TableColumn<CartItem, String> colName;
    @FXML private TableColumn<CartItem, Number> colPrice;
    @FXML private TableColumn<CartItem, Number> colQty;
    @FXML private TableColumn<CartItem, Number> colSub;
    @FXML private TableColumn<CartItem, Void> colActions;

    // ---------------------------------------------------------
    // Services & Dependencies
    // ---------------------------------------------------------
    private final CartService cartService = CartService.getInstance();

    // ---------------------------------------------------------
    // Payment Icons (local resources)
    // ---------------------------------------------------------
    private static final Map<String, String> PAYMENT_ICON = Map.of(
            "CASH", "/images/payments/cash.png",
            "KPAY", "/images/payments/kpay-logo.png",
            "WAVEMONEY", "/images/payments/wavemoney-logo.png",
            "AYAPAY", "/images/payments/ayapay-logo.png"
    );

    private static final double ICON_SIZE = 18;

    private String paymentIconPath(String name) {
        if (name == null) return null;
        String key = name.replaceAll("\\s+", "").toUpperCase();
        return PAYMENT_ICON.get(key);
    }

    // Cache decoded images (avoid reloading/decoding on every selection)
    private final Map<String, Image> iconCache = new HashMap<>();

    /**
     * Load icon with requested size and background loading to keep UI smooth.
     */
    private Image loadIcon(String path) {
        if (path == null || path.isBlank()) return null;

        String p = path.startsWith("/") ? path : "/" + path;

        return iconCache.computeIfAbsent(p, key -> {
            var url = getClass().getResource(key);
            if (url == null) return null;

            return new Image(
                    url.toExternalForm(),
                    ICON_SIZE, ICON_SIZE,
                    true,
                    true,
                    true
            );
        });
    }

    // ---------------------------------------------------------
    // Initialization Logic
    // ---------------------------------------------------------
    @FXML
    private void initialize() {
        // Preload icons to remove first-click lag
        PAYMENT_ICON.values().forEach(this::loadIcon);

        // Initialize Payment Dropdown
        cbPaymentOptions.setItems(PaymentDAO.getActivePayments());
        setupComboBox(
                cbPaymentOptions,
                Payment::getPaymentName,
                p -> paymentIconPath(p.getPaymentName())
        );

        // Bind Table Items
        tblCart.setItems(cartService.getItems());

        // Define Column Cell Value Factories
        colName.setCellValueFactory(c -> {
            CartItem it = c.getValue();
            String opt = it.getOption();
            String display = (opt == null || opt.isBlank())
                    ? it.getName()
                    : it.getName() + " (" + opt + ")";
            return new javafx.beans.property.SimpleStringProperty(display);
        });

        colPrice.setCellValueFactory(c -> c.getValue().unitPriceProperty());
        colQty.setCellValueFactory(c -> c.getValue().quantityProperty());
        colSub.setCellValueFactory(c -> c.getValue().subTotalProperty());

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button btnMinus = new Button("-");
            private final Button btnPlus = new Button("+");
            private final Button btnDel = new Button();
            private final HBox box = new HBox(6, btnMinus, btnPlus, btnDel);

            {
                FontIcon trash = new FontIcon("fas-trash");
                trash.setIconSize(14);
                btnDel.setGraphic(trash);
                btnDel.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                btnDel.getStyleClass().add("btn-danger-icon");

                btnMinus.getStyleClass().add("card-dec-btn");
                btnPlus.getStyleClass().add("card-inc-btn");
                box.setAlignment(Pos.CENTER_LEFT);

                btnMinus.setOnAction(e -> {
                    CartItem it = getCurrentItem();
                    if (it == null) return;
                    cartService.changeQty(it, -1);
                });

                btnPlus.setOnAction(e -> {
                    CartItem it = getCurrentItem();
                    if (it == null) return;
                    if (!canIncrease(it)) return;
                    cartService.changeQty(it, +1);
                });

                btnDel.setOnAction(e -> {
                    CartItem it = getCurrentItem();
                    if (it == null) return;
                    cartService.remove(it);
                });
            }

            private CartItem getCurrentItem() {
                if (getIndex() < 0) return null;
                if (getIndex() >= getTableView().getItems().size()) return null;
                return getTableView().getItems().get(getIndex());
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // Bind Grand Total Label
        updateTotalLabel();

        cartService.getItems().addListener((ListChangeListener<CartItem>) c -> updateTotalLabel());

        // also listen quantity changes of each item
        for (CartItem it : cartService.getItems()) {
            it.quantityProperty().addListener((obs, o, n) -> updateTotalLabel());
        }

        cartService.getItems().addListener((ListChangeListener<CartItem>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (CartItem it : c.getAddedSubList()) {
                        it.quantityProperty().addListener((obs, o, n) -> updateTotalLabel());
                    }
                }
            }
        });
    }

    // ---------------------------------------------------------
    // Order Processing
    // ---------------------------------------------------------
    @FXML
    private void onConfirmOrder() {
        if (cartService.getItems().isEmpty()) {
            SnackBar.show(SnackBarType.WARNING, "Cart Empty", "Please add items first.", Duration.seconds(2));
            return;
        }

        Payment pay = cbPaymentOptions.getValue();
        if (pay == null) {
            SnackBar.show(SnackBarType.WARNING, "Payment Required", "Please choose a payment method.", Duration.seconds(2));
            return;
        }

        int userId = SessionManager.getUser().getUserId();
        int paymentId = pay.getPaymentId();
        String cashierName = SessionManager.getUser().getUserName();
        String paymentName = pay.getPaymentName();

        var snapshot = new java.util.ArrayList<>(cartService.getItems());

        confirmOrderBtn.setDisable(true);
        try {
            ReceiptData rd = OrderService.placeOrderAndBuildReceipt(
                    userId, paymentId, cashierName, paymentName, snapshot
            );

            cartService.clear();
            com.cakeshopsystem.utils.events.AppEvents.fireOrderCompleted();

            SnackBar.show(
                    SnackBarType.SUCCESS,
                    "Success",
                    "Order confirmed (ID: " + rd.getOrderId() + ")",
                    Duration.seconds(2)
            );

            BigDecimal oneLakh = new BigDecimal("100000");
            if (rd.getGrandTotal().compareTo(oneLakh) >= 0) {
                openMemberRegistrationPopup(rd.getOrderId(), () -> {
                    try { openReceiptPopup(rd); }
                    catch (Exception ex) {
                        SnackBar.show(SnackBarType.ERROR, "Failed", ex.getMessage(), Duration.seconds(3));
                    }
                });
            } else {
                openReceiptPopup(rd);
            }

        } catch (Exception ex) {
            SnackBar.show(SnackBarType.ERROR, "Failed", ex.getMessage(), Duration.seconds(3));
        } finally {
            confirmOrderBtn.setDisable(false);
        }
    }

    // ---------------------------------------------------------
    // Helper Methods & Validations
    // ---------------------------------------------------------
    private BigDecimal computeGrandTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem it : cartService.getItems()) {
            BigDecimal unit = BigDecimal.valueOf(it.getUnitPrice());
            BigDecimal qty = BigDecimal.valueOf(it.getQuantity());
            total = total.add(unit.multiply(qty));
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private boolean canIncrease(CartItem it) {
        String opt = it.getOption();
        if ("HOT".equals(opt) || "COLD".equals(opt)) return true;

        int available;
        if ("DISCOUNT".equals(opt)) {
            available = InventoryDAO.getDiscountQuantityByProductId(it.getProductId());
        } else {
            available = InventoryDAO.getRegularQuantityByProductId(it.getProductId());
        }

        if (it.getQuantity() >= available) {
            SnackBar.show(
                    SnackBarType.WARNING,
                    "Failed",
                    "Cannot increase quantity beyond available stock.",
                    Duration.seconds(2)
            );
            return false;
        }
        return true;
    }

    /**
     * Smooth ComboBox rendering:
     * - Preloads icons (done in initialize)
     * - Loads icons at final size (ICON_SIZE)
     * - Reuses ImageView per cell (no new ImageView per updateItem)
     */
    private <T> void setupComboBox(
            ComboBox<T> combo,
            java.util.function.Function<T, String> textFn,
            java.util.function.Function<T, String> iconPathFn
    ) {
        java.util.function.Supplier<ListCell<T>> cellSupplier = () -> new ListCell<>() {
            private final ImageView iv = new ImageView();

            {
                iv.setFitWidth(ICON_SIZE);
                iv.setFitHeight(ICON_SIZE);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
            }

            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    iv.setImage(null);
                    return;
                }

                String text = textFn.apply(item);
                String iconPath = (iconPathFn == null) ? null : iconPathFn.apply(item);

                setText(text);

                Image icon = loadIcon(iconPath);
                if (icon == null) {
                    setGraphic(null);
                    iv.setImage(null);
                } else {
                    iv.setImage(icon);
                    setGraphic(iv);
                }
            }
        };

        combo.setCellFactory(lv -> cellSupplier.get());
        combo.setButtonCell(cellSupplier.get());
    }

    private void updateTotalLabel() {
        lblTotalAmount.setText("Total Amount : " + computeGrandTotal().toPlainString());
    }

    private void showPopup(Parent root) {
        MainController.handleClosePopupContent();
        PauseTransition pt = new PauseTransition(Duration.millis(180));
        pt.setOnFinished(e -> MainController.togglePopupContent(root));
        pt.play();
    }

    private void openMemberRegistrationPopup(int orderId, Runnable afterDone) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/RegisterMember.fxml"));
        Parent root = loader.load();

        RegisterMemberController controller = loader.getController();
        controller.setData(orderId);

        controller.setOnRegistered(afterDone);
        controller.setOnSkipped(afterDone);

        showPopup(root);
    }

    private void openReceiptPopup(ReceiptData rd) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Receipt.fxml"));
        Parent root = loader.load();

        ReceiptController controller = loader.getController();
        controller.setData(rd);

        showPopup(root);
    }
}