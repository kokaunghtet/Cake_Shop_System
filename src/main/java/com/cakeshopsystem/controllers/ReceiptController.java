package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.CartItem;
import com.cakeshopsystem.models.ReceiptData;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import javafx.fxml.FXML;
import javafx.print.PrinterJob;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

/**
 * Controller responsible for displaying the order receipt,
 * handling the dynamic generation of item rows, and managing
 * the printing and file-saving processes.
 */
public class ReceiptController {

    // ---------------------------------------------------------
    // FXML UI Components
    // ---------------------------------------------------------
    @FXML private VBox receiptRoot;
    @FXML private Label lblOrderId;
    @FXML private Label lblDate;
    @FXML private Label lblCashier;
    @FXML private Label lblPayment;
    @FXML private VBox itemsBox;

    @FXML private Label lblSubtotal;
    @FXML private Label lblDiscount;
    @FXML private Label lblGrandTotal;

    @FXML private Button btnCancel;
    @FXML private Button btnPrint;

    // ---------------------------------------------------------
    // Data Members
    // ---------------------------------------------------------
    private ReceiptData data;

    // ---------------------------------------------------------
    // Data Binding & Initialization
    // ---------------------------------------------------------
    /**
     * Populates the receipt view with data from a ReceiptData object.
     */
    public void setData(ReceiptData data) {
        this.data = data;

        // Set Header Information
        lblOrderId.setText(String.valueOf(data.getOrderId()));
        lblDate.setText(data.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        lblCashier.setText(data.getCashierName());
        lblPayment.setText(data.getPaymentName());

        // Generate Item Rows Dynamically
        java.util.ArrayList<Node> rows = new java.util.ArrayList<>();
        rows.add(createHeaderRow());
        for (CartItem it : data.getItems()) {
            rows.add(createItemRow(it));
        }
        itemsBox.getChildren().setAll(rows);

        // Set Summary Totals
        lblSubtotal.setText(fmt(data.getSubtotal()));
        lblDiscount.setText(fmt(data.getDiscountAmount()));
        lblGrandTotal.setText(fmt(data.getGrandTotal()));
    }

    // ---------------------------------------------------------
    // Event Handlers
    // ---------------------------------------------------------
    @FXML
    private void onCancel() {
        MainController.handleClosePopupContent();
    }

    /**
     * Handles the printing process and saves a snapshot of the receipt to a PNG file.
     */
    @FXML
    private void onPrint() {
        if (data == null) return;

        try {
            saveReceiptToResourcesFolder();
            SnackBar.show(SnackBarType.SUCCESS, "Success", "Receipt saved successfully.", Duration.seconds(2));
            MainController.handleClosePopupContent();
        } catch (Exception ex) {
            new Alert(Alert.AlertType.ERROR, "Failed to save receipt: " + ex.getMessage()).showAndWait();
            return;
        }

        // Logic for triggering the physical printer dialog
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job != null && job.showPrintDialog(receiptRoot.getScene().getWindow())) {
            boolean ok = job.printPage(receiptRoot);
            if (ok) job.endJob();
        }
    }

    // ---------------------------------------------------------
    // File I/O Logic
    // ---------------------------------------------------------
    /**
     * Saves a snapshot of the receipt UI as a PNG image in the project's resource folder.
     */
    private void saveReceiptToResourcesFolder() throws Exception {
        java.nio.file.Path dir = java.nio.file.Paths.get("src", "main", "resources", "receipts");
        java.nio.file.Files.createDirectories(dir);

        String stamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filename = "receipt-" + data.getOrderId() + "-" + stamp + ".png";

        java.io.File out = dir.resolve(filename).toFile();

        // Capture a snapshot of the UI node
        javafx.scene.image.WritableImage img = receiptRoot.snapshot(new javafx.scene.SnapshotParameters(), null);
        javax.imageio.ImageIO.write(javafx.embed.swing.SwingFXUtils.fromFXImage(img, null), "png", out);
    }

    // ---------------------------------------------------------
    // Dynamic Layout Generation Helpers
    // ---------------------------------------------------------
    /**
     * Creates the column headers for the items list.
     */
    private Node createHeaderRow() {
        Label item = new Label("ITEM");
        item.setPrefWidth(260);
        item.setStyle("-fx-font-weight: bold;");

        Label qty = new Label("QTY");
        qty.setPrefWidth(60);
        qty.setAlignment(Pos.CENTER_RIGHT);
        qty.setStyle("-fx-font-weight: bold;");

        Label unit = new Label("UNIT");
        unit.setPrefWidth(90);
        unit.setAlignment(Pos.CENTER_RIGHT);
        unit.setStyle("-fx-font-weight: bold;");

        Label total = new Label("TOTAL");
        total.setPrefWidth(90);
        total.setAlignment(Pos.CENTER_RIGHT);
        total.setStyle("-fx-font-weight: bold;");

        HBox row = new HBox(10, item, qty, unit, total);
        row.setAlignment(Pos.CENTER_LEFT);

        Separator sep = new Separator();
        VBox wrap = new VBox(6, row, sep); // header + line under it
        return wrap;
    }

    /**
     * Creates a single HBox row representing one cart item.
     */
    private Node createItemRow(CartItem it) {
        String opt = it.getOption();
        String name = (opt == null || opt.isBlank()) ? it.getName() : it.getName() + " (" + opt + ")";

        Label item = new Label(name);
        item.setPrefWidth(260);

        Label qty = new Label(String.valueOf(it.getQuantity()));
        qty.setPrefWidth(60);
        qty.setAlignment(Pos.CENTER_RIGHT);

        Label unit = new Label(fmt(java.math.BigDecimal.valueOf(it.getUnitPrice())));
        unit.setPrefWidth(90);
        unit.setAlignment(Pos.CENTER_RIGHT);

        double lineTotal = it.getUnitPrice() * it.getQuantity();
        Label total = new Label(fmt(java.math.BigDecimal.valueOf(lineTotal)));
        total.setPrefWidth(90);
        total.setAlignment(Pos.CENTER_RIGHT);

        HBox row = new HBox(10, item, qty, unit, total);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // ---------------------------------------------------------
    // Formatting Utilities
    // ---------------------------------------------------------
    /**
     * Formats BigDecimal values to a standard 2-decimal money format.
     */
    private String fmt(BigDecimal v) {
        if (v == null) return "0.00";
        return v.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}