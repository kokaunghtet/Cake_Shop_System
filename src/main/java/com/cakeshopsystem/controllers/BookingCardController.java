package com.cakeshopsystem.controllers;

import com.cakeshopsystem.utils.constants.BookingStatus;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class BookingCardController {

    // =====================================
    // FXML UI COMPONENTS
    // =====================================
    @FXML public Button btnConfirm;

    @FXML public StackPane spBookingCard;
    @FXML public VBox vbBookingCard;

    @FXML public ImageView ivBooking;

    @FXML public Label lblMemberPhone;
    @FXML public Label lblBookingName;
    @FXML public Label lblPickupDate;

    @FXML public HBox hbSessionTime;
    @FXML public Label lblStartTime;
    @FXML public Label lblEndTime;

    @FXML public ComboBox<BookingStatus> cbBookingStatus;
    @FXML public HBox boxButton;

    // =====================================
    // STYLE CLASS CONSTANTS
    // =====================================
    private static final String CONFIRMED = "status-confirmed";
    private static final String CANCELLED = "status-cancelled";

    // =====================================
    // STATUS STYLING
    // =====================================
    public void applyStatusStyle(BookingStatus st) {
        if (spBookingCard == null) return;

        var cl = spBookingCard.getStyleClass();
        cl.removeAll(CONFIRMED, CANCELLED);

        if (st == null) return;

        switch (st) {
            case CONFIRMED -> cl.add(CONFIRMED);
            case CANCELLED -> cl.add(CANCELLED);
        }
    }

    // =====================================
    // VISIBILITY HELPERS
    // =====================================
    private void setVisibleManaged(javafx.scene.Node node, boolean visible) {
        if (node == null) return;
        node.setVisible(visible);
        node.setManaged(visible);
    }

    // =====================================
    // CARD MODE CONFIGURATION
    // =====================================
    public void setupForCustomCake() {
        setVisibleManaged(lblMemberPhone, false);
        setVisibleManaged(ivBooking, false);
        setVisibleManaged(hbSessionTime, false);
        setVisibleManaged(lblStartTime, false);
        setVisibleManaged(lblEndTime, false);
    }

    public void setupForDiy() {
        setVisibleManaged(lblMemberPhone, true);
        setVisibleManaged(ivBooking, true);
        setVisibleManaged(hbSessionTime, true);
        setVisibleManaged(lblStartTime, true);
        setVisibleManaged(lblEndTime, true);
    }
}
