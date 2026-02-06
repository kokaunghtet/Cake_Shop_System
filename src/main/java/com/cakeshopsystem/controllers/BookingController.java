package com.cakeshopsystem.controllers;

import com.cakeshopsystem.utils.constants.BookingStatus;
import com.cakeshopsystem.utils.dao.BookingDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class BookingController {

    @FXML
    private HBox hbCustomOrder;
    @FXML
    private HBox hbDiyOrder;
    @FXML
    private ScrollPane spCustomOrder;
    @FXML
    private ScrollPane spDiyOrder;

    private static final String BOOKING_CARD_FXML = "/views/BookingCard.fxml";
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy MMM dd", Locale.ENGLISH);

    @FXML
    public void initialize() {
        loadCustomCakeBookings();
        loadDiyBookings();
    }

    private void loadCustomCakeBookings() {
        hbCustomOrder.getChildren().clear();

        var rows = BookingDAO.getCustomCakeCards(); // you implement
        for (var row : rows) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(BOOKING_CARD_FXML));
                Parent card = loader.load();
                BookingCardController c = loader.getController();

                c.setupForCustomCake(); // make sure you implemented in BookingCardController

                c.lblBookingName.setText(buildCustomName(row));
                c.lblPickupDate.setText("Pickup: " + row.getPickupDate().format(DATE_FMT));

                wireStatusSave(
                        c,
                        row.getBookingId(),
                        row.getStatus()
                );

                hbCustomOrder.getChildren().add(card);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void loadDiyBookings() {
        hbDiyOrder.getChildren().clear();

        var rows = BookingDAO.getDiyBookingCards();
        for (var row : rows) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(BOOKING_CARD_FXML));
                Parent card = loader.load();
                BookingCardController c = loader.getController();

                c.setupForDiy(); // make sure you implemented in BookingCardController

                c.lblBookingName.setText("DIY Booking (Order #" + row.getOrderId() + ")");
                c.lblPickupDate.setText("Date: " + row.getStartTime().toLocalDate().format(DATE_FMT));

                setSessionTime(c, row.getStartTime().toLocalTime().format(TIME_FMT),
                        row.getEndTime().toLocalTime().format(TIME_FMT));

                wireStatusSave(
                        c,
                        row.getBookingId(),
                        row.getStatus()
                );

                hbDiyOrder.getChildren().add(card);

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private void wireStatusSave(BookingCardController c, Integer bookingId, BookingStatus initialStatus) {

        c.cbBookingStatus.setItems(FXCollections.observableArrayList(BookingStatus.values()));
        c.cbBookingStatus.setValue(initialStatus);
        c.btnConfirm.setText("Save");

        // If no booking_id, disable everything
        if (bookingId == null) {
            c.cbBookingStatus.setDisable(true);
            c.btnConfirm.setDisable(true);
            c.applyStatusStyle(null);
            return;
        }

        // this holds the latest saved status (NOT the combo current selection)
        final BookingStatus[] savedStatus = { initialStatus };

        // apply initial style + lock
        c.applyStatusStyle(savedStatus[0]);
        applyLock(c, savedStatus[0]);

        // start with Save disabled
        c.btnConfirm.setDisable(true);

        // Enable Save only when user changes value AND current saved status isn't final
        c.cbBookingStatus.valueProperty().addListener((obs, oldV, newV) -> {
            if (isFinalStatus(savedStatus[0])) return; // locked
            c.btnConfirm.setDisable(newV == null || newV == savedStatus[0]);
        });

        c.btnConfirm.setOnAction(e -> {
            BookingStatus selected = c.cbBookingStatus.getValue();
            if (selected == null) return;

            // if already locked, ignore
            if (isFinalStatus(savedStatus[0])) return;

            boolean ok = BookingDAO.updateBookingStatus(bookingId, selected);
            if (!ok) return;

            // update "saved" status
            savedStatus[0] = selected;

            // update style + lock if final
            c.applyStatusStyle(selected);
            applyLock(c, selected);

            // after save: disable Save until changed again (or locked)
            c.btnConfirm.setDisable(true);
        });
    }

    private boolean isFinalStatus(BookingStatus st) {
        return st == BookingStatus.CONFIRMED || st == BookingStatus.CANCELLED;
    }

    private void applyLock(BookingCardController c, BookingStatus st) {
        boolean lock = isFinalStatus(st); // lock CONFIRMED + CANCELLED
        c.cbBookingStatus.setDisable(lock);
        c.btnConfirm.setDisable(lock);
    }

    private void setSessionTime(BookingCardController c, String start, String end) {
        if (c.hbSessionTime == null) return;
        if (c.hbSessionTime.getChildren().size() < 3) return;

        if (c.hbSessionTime.getChildren().get(0) instanceof Label s) s.setText(start);
        if (c.hbSessionTime.getChildren().get(2) instanceof Label e) e.setText(end);
    }

    private String buildCustomName(BookingDAO.CustomCakeCardRow row) {
        String base = "Custom " + row.getSizeInches() + "\" " + shortShape(row.getShape());
        String flavour = row.getFlavourName();

        String topping = row.getToppingName();
        if (topping == null || topping.isBlank()) return base + " (" + flavour + ")";
        return base + " (" + flavour + " + " + topping + ")";
    }

    private String shortShape(Object dbShape) {
        if (dbShape == null) return "";
        String s = dbShape.toString().trim().toUpperCase(); // "Circle" -> "CIRCLE"
        return switch (s) {
            case "CIRCLE" -> "C";
            case "SQUARE" -> "S";
            case "HEART" -> "H";
            default -> s.isEmpty() ? "" : s.substring(0, 1);
        };
    }
}
