package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.CartItem;
import com.cakeshopsystem.models.Payment;
import com.cakeshopsystem.models.ReceiptData;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.PaymentDAO;
import com.cakeshopsystem.utils.services.CartService;
import com.cakeshopsystem.utils.services.OrderService;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;

/**
 * Controller responsible for managing the shopping cart UI,
 * handling quantity adjustments, and finalizing orders.
 */
public class CartController {

    // ---------------------------------------------------------
    // FXML UI Components
    // ---------------------------------------------------------
    @FXML
    private Label lblTotalAmount;
    @FXML
    private ComboBox<Payment> cbPaymentOptions;
    @FXML
    private Button confirmOrderBtn;
    @FXML
    private TableView<CartItem> tblCart;
    @FXML
    private TableColumn<CartItem, String> colName;
    @FXML
    private TableColumn<CartItem, Number> colPrice;
    @FXML
    private TableColumn<CartItem, Number> colQty;
    @FXML
    private TableColumn<CartItem, Number> colSub;
    @FXML
    private TableColumn<CartItem, Void> colActions;

    // ---------------------------------------------------------
    // Services & Dependencies
    // ---------------------------------------------------------
    private final CartService cartService = CartService.getInstance();

    // ---------------------------------------------------------
    // Initialization Logic
    // ---------------------------------------------------------
    @FXML
    private void initialize() {
        // Initialize Payment Dropdown
        cbPaymentOptions.setItems(PaymentDAO.getActivePayments());
        setupComboBox(cbPaymentOptions, Payment::getPaymentName);

        // Bind Table Items
        tblCart.setItems(cartService.getItems());

        // Define Column Cell Value Factories
        colName.setCellValueFactory(c -> {
            CartItem it = c.getValue();
            String opt = it.getOption();
            String display = (opt == null || opt.isBlank()) ? it.getName() : it.getName() + " (" + opt + ")";
            return new javafx.beans.property.SimpleStringProperty(display);
        });

        colPrice.setCellValueFactory(c -> c.getValue().unitPriceProperty());
        colQty.setCellValueFactory(c -> c.getValue().quantityProperty());
        colSub.setCellValueFactory(c -> c.getValue().subTotalProperty());

        // Set up Action Buttons (Plus, Minus, Delete) within the Table
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

        cartService.getItems().addListener((javafx.collections.ListChangeListener<CartItem>) c -> updateTotalLabel());

        // also listen quantity changes of each item
        for (CartItem it : cartService.getItems()) {
            it.quantityProperty().addListener((obs, o, n) -> updateTotalLabel());
        }

        cartService.getItems().addListener((javafx.collections.ListChangeListener<CartItem>) c -> {
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

            SnackBar.show(SnackBarType.SUCCESS, "Success", "Order confirmed (ID: " + rd.getOrderId() + ")", Duration.seconds(2));

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
    private java.math.BigDecimal computeGrandTotal() {
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (CartItem it : cartService.getItems()) {
            java.math.BigDecimal unit = java.math.BigDecimal.valueOf(it.getUnitPrice());
            java.math.BigDecimal qty = java.math.BigDecimal.valueOf(it.getQuantity());
            total = total.add(unit.multiply(qty));
        }
        return total.setScale(2, java.math.RoundingMode.HALF_UP);
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
            SnackBar.show(SnackBarType.WARNING, "Failed", "Cannot increase quantity beyond available stock.", Duration.seconds(2));
            return false;
        }
        return true;
    }

    private java.math.BigDecimal getProductBasePrice(int productId) throws Exception {
        String sql = "SELECT price FROM products WHERE product_id = ? LIMIT 1";
        try (var con = com.cakeshopsystem.utils.databaseconnection.DB.connect();
             var ps = con.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Missing product price for product_id=" + productId);
                return money2(rs.getBigDecimal(1));
            }
        }
    }

    private java.math.BigDecimal money2(java.math.BigDecimal v) {
        if (v == null) return java.math.BigDecimal.ZERO.setScale(2, java.math.RoundingMode.HALF_UP);
        return v.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String normalizeOpt(String s) {
        return s == null ? "" : s.trim().toUpperCase();
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

    private void updateTotalLabel() {
        lblTotalAmount.setText("Total Amount : " + computeGrandTotal().toPlainString());
    }

    private void showPopup(Parent root) {
        MainController.handleClosePopupContent();
        javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(Duration.millis(180));
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