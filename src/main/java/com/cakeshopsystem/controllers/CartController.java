package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.CartItem;
import com.cakeshopsystem.models.Payment;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.PaymentDAO;
import com.cakeshopsystem.utils.services.CartService;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

public class CartController {

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

    private final CartService cartService = CartService.getInstance();

    @FXML
    private void initialize() {
        cbPaymentOptions.setItems(PaymentDAO.getActivePayments());
        setupComboBox(cbPaymentOptions, Payment::getPaymentName);

        tblCart.setItems(cartService.getItems());

        colName.setCellValueFactory(c -> {
            CartItem it = c.getValue();
            String opt = it.getOption();
            String display = (opt == null || opt.isBlank()) ? it.getName() : it.getName() + " (" + opt + ")";
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

                box.setAlignment(Pos.CENTER_LEFT);

                btnMinus.setOnAction(e -> {
                    CartItem it = getCurrentItem();
                    if (it == null) return;
                    cartService.changeQty(it, -1);
                });

                btnPlus.setOnAction(e -> {
                    CartItem it = getCurrentItem();
                    if (it == null) return;

                    if (!canIncrease(it)) {
                        return;
                    }

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

    @FXML
    private void onConfirmOrder() {
        if (cartService.getItems().isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Cart is empty.").showAndWait();
            return;
        }

        try {
            // OrderService.placeOrder(cashierId, cartService.getItems());
            cartService.clear();
            new Alert(Alert.AlertType.INFORMATION, "Order confirmed.").showAndWait();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private boolean canIncrease(CartItem it) {
        String opt = it.getOption();

        // Drinks: unlimited
        if ("HOT".equals(opt) || "COLD".equals(opt)) {
            return true;
        }

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

}
