package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Payment;
import com.cakeshopsystem.utils.cache.PaymentCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;

public class EditPaymentConfigController {

    @FXML private VBox vbPaymentContainer;
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    // current selection per payment
    private final Map<Integer, Boolean> changes = new HashMap<>();

    // baseline (what it was when screen loaded / after last save)
    private final Map<Integer, Boolean> originalStates = new HashMap<>();

    private static final Duration MSG_DURATION = Duration.seconds(2);

    @FXML
    public void initialize() {
        loadPaymentsUI();

        if (btnCancel != null) btnCancel.setOnAction(e -> handleCancel());
        if (btnSave != null) btnSave.setOnAction(e -> handleSave());

        updateSaveButtonState();
    }

    private void loadPaymentsUI() {
        if (vbPaymentContainer == null) return;

        vbPaymentContainer.getChildren().clear();
        changes.clear();
        originalStates.clear();

        ObservableList<Payment> payments = PaymentCache.getPaymentsList();
        for (Payment payment : payments) {
            originalStates.put(payment.getPaymentId(), payment.isActive());
            changes.put(payment.getPaymentId(), payment.isActive());
            vbPaymentContainer.getChildren().add(buildPaymentRow(payment));
        }
    }

    private void handleCancel() {
        loadPaymentsUI();
        updateSaveButtonState();
        SnackBar.show(SnackBarType.INFO, "Cancelled", "Changes reverted.", MSG_DURATION);
    }

    private void handleSave() {
        int changedCount = 0;

        for (Map.Entry<Integer, Boolean> entry : changes.entrySet()) {
            int paymentId = entry.getKey();
            boolean newActive = entry.getValue();

            Boolean oldActive = originalStates.get(paymentId);
            if (oldActive == null || oldActive == newActive) continue;

            // IMPORTANT: use cache wrapper so cache stays correct
            boolean ok = PaymentCache.setPaymentActive(paymentId, newActive);
            if (!ok) {
                SnackBar.show(SnackBarType.ERROR, "Save failed", "Could not update payment id: " + paymentId, Duration.seconds(3));
                return;
            }

            changedCount++;
        }

        if (changedCount == 0) {
            SnackBar.show(SnackBarType.INFO, "Nothing to save", "No changes detected.", MSG_DURATION);
            updateSaveButtonState();
            return;
        }

        // optional but good: force refresh from DB to guarantee perfect sync
        PaymentCache.refreshPayments();

        SnackBar.show(SnackBarType.SUCCESS, "Saved", changedCount + " payment(s) updated.", MSG_DURATION);

        // reset baseline to new saved values
        loadPaymentsUI();
        updateSaveButtonState();
    }

    private HBox buildPaymentRow(Payment payment) {
        Label paymentName = new Label(payment.getPaymentName());
        paymentName.getStyleClass().add("payment-name");

        RadioButton rbActive = new RadioButton("Active");
        RadioButton rbInactive = new RadioButton("Inactive");

        ToggleGroup group = new ToggleGroup();
        rbActive.setToggleGroup(group);
        rbInactive.setToggleGroup(group);

        if (!payment.isActive()) {
            if (!rbInactive.getStyleClass().contains("inactive")) rbInactive.getStyleClass().add("inactive");
        } else {
            rbInactive.getStyleClass().remove("inactive");
        }

        group.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) return;

            boolean isActive = (newT == rbActive);
            changes.put(payment.getPaymentId(), isActive);

            if (isActive) {
                rbInactive.getStyleClass().remove("inactive");
            } else {
                if (!rbInactive.getStyleClass().contains("inactive")) rbInactive.getStyleClass().add("inactive");
            }

            updateSaveButtonState();
        });


        rbActive.setSelected(payment.isActive());
        rbInactive.setSelected(!payment.isActive());

        group.selectedToggleProperty().addListener((obs, oldT, newT) -> {
            if (newT == null) return;

            changes.put(payment.getPaymentId(), newT == rbActive);
            updateSaveButtonState();
        });

        HBox statusBox = new HBox(10, rbActive, rbInactive);
        statusBox.setAlignment(Pos.CENTER_RIGHT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, paymentName, spacer, statusBox);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("payment-row");

        return row;
    }

    private void updateSaveButtonState() {
        if (btnSave == null) return;

        boolean dirty = false;
        for (Map.Entry<Integer, Boolean> e : changes.entrySet()) {
            Boolean oldV = originalStates.get(e.getKey());
            if (oldV != null && oldV != e.getValue()) {
                dirty = true;
                break;
            }
        }
        btnSave.setDisable(!dirty);
    }
}
