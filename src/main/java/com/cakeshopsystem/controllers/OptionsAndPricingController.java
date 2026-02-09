package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Flavour;
import com.cakeshopsystem.models.Topping;
import com.cakeshopsystem.utils.cache.FlavourCache;
import com.cakeshopsystem.utils.cache.ToppingCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Duration;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.util.function.UnaryOperator;

public class OptionsAndPricingController {

    @FXML private TableView<Flavour> tblFlavours;
    @FXML private TableView<Topping> tblToppings;

    @FXML private TableColumn<Flavour, String> tcFlavourName;
    @FXML private TableColumn<Flavour, Double> tcFlavourPrice;

    @FXML private TableColumn<Topping, String> tcToppingName;
    @FXML private TableColumn<Topping, Double> tcToppingPrice;

    @FXML private Button btnCancel;
    @FXML private Button btnSave;
    @FXML private TextField tfDiscountPercent;

    // ---- Add New Flavour ----
    @FXML private TextField tfNewFlavourName;
    @FXML private TextField tfNewFlavourPrice;
    @FXML private Button btnAddNewFlavour;

    // ---- Add New Topping ----
    @FXML private TextField tfNewToppingName;
    @FXML private TextField tfNewToppingPrice;
    @FXML private Button btnAddNewTopping;

    private ObservableList<Flavour> flavours;
    private ObservableList<Topping> toppings;

    private static final DecimalFormat DF = new DecimalFormat("0.##");

    private static final StringConverter<Double> DOUBLE_CONVERTER = new StringConverter<>() {
        @Override public String toString(Double value) {
            if (value == null) return "";
            return DF.format(value);
        }
        @Override public Double fromString(String text) {
            if (text == null) return 0.0;
            String t = text.trim();
            if (t.isEmpty()) return 0.0;
            return Double.parseDouble(t);
        }
    };

    @FXML
    public void initialize() {
        tfDiscountPercent.setText(formatPercent(SessionManager.getDiscountRate()));

        tblFlavours.setFixedCellSize(SessionManager.TABLE_CELL_SIZE);
        tblToppings.setFixedCellSize(SessionManager.TABLE_CELL_SIZE);

        // make columns stretch nicer
        tblFlavours.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tblToppings.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        // price fields numeric only (optional but highly recommended)
        applyDecimalTextFormatter(tfNewFlavourPrice);
        applyDecimalTextFormatter(tfNewToppingPrice);

        configureFlavourTable();
        configureToppingTable();

        btnSave.setOnAction(e -> handleDiscountSave());
        btnCancel.setOnAction(e -> resetUI());

        btnAddNewFlavour.setOnAction(e -> addNewFlavour());
        btnAddNewTopping.setOnAction(e -> addNewTopping());

        // Enter key convenience
        tfNewFlavourPrice.setOnAction(e -> addNewFlavour());
        tfNewToppingPrice.setOnAction(e -> addNewTopping());
    }

    // -------------------- TABLE SETUP --------------------

    private void configureFlavourTable() {
        flavours = FlavourCache.getFlavoursList();
        tblFlavours.setItems(flavours);

        tcFlavourName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFlavourName()));
        tcFlavourPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrice()));

        tblFlavours.setEditable(true);

        tcFlavourName.setCellFactory(TextFieldTableCell.forTableColumn());
        tcFlavourPrice.setCellFactory(TextFieldTableCell.forTableColumn(DOUBLE_CONVERTER));

        tcFlavourName.setOnEditCommit(evt -> {
            Flavour f = evt.getRowValue();
            String oldVal = evt.getOldValue();
            String newVal = safe(evt.getNewValue());

            if (newVal.isEmpty()) {
                SnackBar.show(SnackBarType.ERROR, "Invalid", "Flavour name can't be empty", Duration.seconds(2));
                f.setFlavourName(oldVal);
                tblFlavours.refresh();
                return;
            }

            int existingId = FlavourCache.getFlavourIdByName(newVal);
            if (existingId != -1 && existingId != f.getFlavourId()) {
                SnackBar.show(SnackBarType.ERROR, "Duplicate", "Flavour name already exists", Duration.seconds(2));
                f.setFlavourName(oldVal);
                tblFlavours.refresh();
                return;
            }

            f.setFlavourName(newVal);
            boolean ok = FlavourCache.updateFlavour(f);

            if (ok) {
                SnackBar.show(SnackBarType.SUCCESS, "Saved", "Flavour name updated", Duration.seconds(2));
            } else {
                f.setFlavourName(oldVal);
                tblFlavours.refresh();
                SnackBar.show(SnackBarType.ERROR, "DB Error", "Failed to update flavour", Duration.seconds(2));
            }
        });

        tcFlavourPrice.setOnEditCommit(evt -> {
            Flavour f = evt.getRowValue();
            Double oldVal = evt.getOldValue();
            Double newVal = evt.getNewValue();

            if (newVal == null || newVal < 0) {
                SnackBar.show(SnackBarType.ERROR, "Invalid", "Price must be >= 0", Duration.seconds(2));
                f.setPrice(oldVal == null ? 0.0 : oldVal);
                tblFlavours.refresh();
                return;
            }

            f.setPrice(newVal);
            boolean ok = FlavourCache.updateFlavour(f);

            if (ok) {
                SnackBar.show(SnackBarType.SUCCESS, "Saved", "Flavour price updated", Duration.seconds(2));
            } else {
                f.setPrice(oldVal == null ? 0.0 : oldVal);
                tblFlavours.refresh();
                SnackBar.show(SnackBarType.ERROR, "DB Error", "Failed to update flavour", Duration.seconds(2));
            }
        });
    }

    private void configureToppingTable() {
        toppings = ToppingCache.getToppingsList();
        tblToppings.setItems(toppings);

        tcToppingName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getToppingName()));
        tcToppingPrice.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getPrice()));

        tblToppings.setEditable(true);

        tcToppingName.setCellFactory(TextFieldTableCell.forTableColumn());
        tcToppingPrice.setCellFactory(TextFieldTableCell.forTableColumn(DOUBLE_CONVERTER));

        tcToppingName.setOnEditCommit(evt -> {
            Topping t = evt.getRowValue();
            String oldVal = evt.getOldValue();
            String newVal = safe(evt.getNewValue());

            if (newVal.isEmpty()) {
                SnackBar.show(SnackBarType.ERROR, "Invalid", "Topping name can't be empty", Duration.seconds(2));
                t.setToppingName(oldVal);
                tblToppings.refresh();
                return;
            }

            int existingId = ToppingCache.getToppingIdByName(newVal);
            if (existingId != -1 && existingId != t.getToppingId()) {
                SnackBar.show(SnackBarType.ERROR, "Duplicate", "Topping name already exists", Duration.seconds(2));
                t.setToppingName(oldVal);
                tblToppings.refresh();
                return;
            }

            t.setToppingName(newVal);
            boolean ok = ToppingCache.updateTopping(t);

            if (ok) {
                SnackBar.show(SnackBarType.SUCCESS, "Saved", "Topping name updated", Duration.seconds(2));
            } else {
                t.setToppingName(oldVal);
                tblToppings.refresh();
                SnackBar.show(SnackBarType.ERROR, "DB Error", "Failed to update topping", Duration.seconds(2));
            }
        });

        tcToppingPrice.setOnEditCommit(evt -> {
            Topping t = evt.getRowValue();
            Double oldVal = evt.getOldValue();
            Double newVal = evt.getNewValue();

            if (newVal == null || newVal < 0) {
                SnackBar.show(SnackBarType.ERROR, "Invalid", "Price must be >= 0", Duration.seconds(2));
                t.setPrice(oldVal == null ? 0.0 : oldVal);
                tblToppings.refresh();
                return;
            }

            t.setPrice(newVal);
            boolean ok = ToppingCache.updateTopping(t);

            if (ok) {
                SnackBar.show(SnackBarType.SUCCESS, "Saved", "Topping price updated", Duration.seconds(2));
            } else {
                t.setPrice(oldVal == null ? 0.0 : oldVal);
                tblToppings.refresh();
                SnackBar.show(SnackBarType.ERROR, "DB Error", "Failed to update topping", Duration.seconds(2));
            }
        });
    }

    // -------------------- ADD NEW --------------------

    private void addNewFlavour() {
        String name = safe(tfNewFlavourName.getText());
        String priceText = safe(tfNewFlavourPrice.getText());

        if (name.isEmpty()) {
            SnackBar.show(SnackBarType.ERROR, "Invalid", "Flavour name is required", Duration.seconds(2));
            tfNewFlavourName.requestFocus();
            return;
        }

        if (FlavourCache.getFlavourIdByName(name) != -1) {
            SnackBar.show(SnackBarType.ERROR, "Duplicate", "Flavour already exists", Duration.seconds(2));
            tfNewFlavourName.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            SnackBar.show(SnackBarType.ERROR, "Invalid", "Price must be a number", Duration.seconds(2));
            tfNewFlavourPrice.requestFocus();
            return;
        }

        if (price < 0) {
            SnackBar.show(SnackBarType.ERROR, "Invalid", "Price must be >= 0", Duration.seconds(2));
            tfNewFlavourPrice.requestFocus();
            return;
        }

        Flavour f = new Flavour(0, name, price);
        boolean ok = FlavourCache.addFlavour(f); // DAO sets ID, cache stores it

        if (ok) {
            tfNewFlavourName.clear();
            tfNewFlavourPrice.clear();
            tfNewFlavourName.requestFocus();
            SnackBar.show(SnackBarType.SUCCESS, "Success", "Flavour added", Duration.seconds(2));
        } else {
            SnackBar.show(SnackBarType.ERROR, "DB Error", "Failed to add flavour", Duration.seconds(2));
        }
    }

    private void addNewTopping() {
        String name = safe(tfNewToppingName.getText());
        String priceText = safe(tfNewToppingPrice.getText());

        if (name.isEmpty()) {
            SnackBar.show(SnackBarType.ERROR, "Invalid", "Topping name is required", Duration.seconds(2));
            tfNewToppingName.requestFocus();
            return;
        }

        if (ToppingCache.getToppingIdByName(name) != -1) {
            SnackBar.show(SnackBarType.ERROR, "Duplicate", "Topping already exists", Duration.seconds(2));
            tfNewToppingName.requestFocus();
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceText);
        } catch (NumberFormatException e) {
            SnackBar.show(SnackBarType.ERROR, "Invalid", "Price must be a number", Duration.seconds(2));
            tfNewToppingPrice.requestFocus();
            return;
        }

        if (price < 0) {
            SnackBar.show(SnackBarType.ERROR, "Invalid", "Price must be >= 0", Duration.seconds(2));
            tfNewToppingPrice.requestFocus();
            return;
        }

        Topping t = new Topping(0, name, price);
        boolean ok = ToppingCache.addTopping(t);

        if (ok) {
            tfNewToppingName.clear();
            tfNewToppingPrice.clear();
            tfNewToppingName.requestFocus();
            SnackBar.show(SnackBarType.SUCCESS, "Success", "Topping added", Duration.seconds(2));
        } else {
            SnackBar.show(SnackBarType.ERROR, "DB Error", "Failed to add topping", Duration.seconds(2));
        }
    }

    // -------------------- DISCOUNT --------------------

    private void handleDiscountSave() {
        String raw = tfDiscountPercent.getText();
        if (raw == null) raw = "";

        raw = raw.trim().replace("%", "").trim();
        if (raw.isEmpty()) return;

        try {
            double percent = Double.parseDouble(raw);

            if (percent < 0 || percent > 100) {
                SnackBar.show(SnackBarType.ERROR, "Invalid", "Discount must be between 0 and 100", Duration.seconds(2));
                return;
            }

            SessionManager.setDiscountRate(percent / 100.0);
            tfDiscountPercent.setText(formatPercent(SessionManager.getDiscountRate()));
            SnackBar.show(SnackBarType.SUCCESS, "Success", "Saved successfully", Duration.seconds(2));

        } catch (NumberFormatException e) {
            SnackBar.show(SnackBarType.ERROR, "Invalid", "Discount must be a number (0-100)", Duration.seconds(2));
        }
    }

    private void resetUI() {
        tfDiscountPercent.setText(formatPercent(SessionManager.getDiscountRate()));
        tblFlavours.refresh();
        tblToppings.refresh();
    }

    private String formatPercent(double rate) {
        return String.format("%.0f%%", rate * 100);
    }

    // -------------------- HELPERS --------------------

    private String safe(String s) {
        return s == null ? "" : s.trim();
    }

    private void applyDecimalTextFormatter(TextField tf) {
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String newText = change.getControlNewText();
            if (newText.isEmpty()) return change;
            return newText.matches("\\d*(\\.\\d{0,2})?") ? change : null;
        };
        tf.setTextFormatter(new TextFormatter<>(filter));
    }
}
