package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.CartItem;
import com.cakeshopsystem.models.ReceiptData;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.PrinterJob;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
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
    @FXML
    private ScrollPane spItems;
    @FXML
    private VBox receiptRoot;
    @FXML
    private Label lblOrderId;
    @FXML
    private Label lblDate;
    @FXML
    private Label lblCashier;
    @FXML
    private Label lblPayment;
    @FXML
    private VBox itemsBox;

    @FXML
    private Label lblSubtotal;
    @FXML
    private Label lblDiscount;
    @FXML
    private Label lblGrandTotal;

    @FXML
    private Button btnCancel;
    @FXML
    private Button btnPrint;

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
//    @FXML
//    private void onPrint() {
//        if (data == null) return;
//
//        try {
//            // 1) make a full snapshot (expands scroll content temporarily)
//            var img = snapshotReceiptFull();
//
//            // 2) save PNG
//            saveImageToResources(img);
//
//            // 3) print the IMAGE (stable, no UI distortion)
//            printImage(img);
//
//            SnackBar.show(SnackBarType.SUCCESS, "Success", "Receipt saved & printed.", Duration.seconds(2));
//            MainController.handleClosePopupContent();
//
//        } catch (Exception ex) {
//            SnackBar.show(SnackBarType.ERROR, "Error", "Failed: " + ex.getMessage(), Duration.seconds(3));
//        }
//    }
    @FXML
    private void onPrint() {
        if (data == null) return;

        try {
            Parent printRoot = loadPrintableReceipt(data);      // off-screen node
            var img = snapshotNodeForPrint(printRoot);          // snapshot off-screen

//            saveImageToResources(img);                          // save PNG
            saveImageToResourcesAsync(img);
            printImage(img);                                    // print image

            SnackBar.show(SnackBarType.SUCCESS, "Success", "Receipt saved & printed.", Duration.seconds(2));
            MainController.handleClosePopupContent();

        } catch (Exception ex) {
            SnackBar.show(SnackBarType.ERROR, "Error", "Failed: " + ex.getMessage(), Duration.seconds(3));
        }
    }

    // ---------------------------------------------------------
    // File I/O Logic
    // ---------------------------------------------------------

//    private javafx.scene.image.WritableImage snapshotReceiptFull() {
//        // force css/layout
//        receiptRoot.applyCss();
//        receiptRoot.layout();
//
//        // --- backup scrollpane state
//        double oldPrefH = spItems.getPrefViewportHeight();
//        var oldV = spItems.getVbarPolicy();
//        var oldH = spItems.getHbarPolicy();
//
//        try {
//            // compute full content height
//            itemsBox.applyCss();
//            itemsBox.layout();
//            double contentH = itemsBox.getBoundsInLocal().getHeight();
//
//            // expand scrollpane so everything is visible
//            spItems.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//            spItems.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//            spItems.setPrefViewportHeight(contentH + 10);
//
//            receiptRoot.applyCss();
//            receiptRoot.layout();
//
//            var params = new javafx.scene.SnapshotParameters();
//
//            // if your receipt background becomes transparent, enable white background:
//            params.setFill(javafx.scene.paint.Color.WHITE);
//
//            return receiptRoot.snapshot(params, null);
//
//        } finally {
//            // restore scrollpane state (prevents UI "breaking")
//            spItems.setPrefViewportHeight(oldPrefH);
//            spItems.setVbarPolicy(oldV);
//            spItems.setHbarPolicy(oldH);
//
//            receiptRoot.applyCss();
//            receiptRoot.layout();
//        }
//    }

    private Parent loadPrintableReceipt(ReceiptData data) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ReceiptPrint.fxml"));
        Parent root = loader.load();

        ReceiptController ctrl = loader.getController();
        ctrl.setData(data);

        return root;
    }

    private javafx.scene.image.WritableImage snapshotNodeForPrint(Parent root) {
        StackPane wrapper = new StackPane(root);
        wrapper.setStyle("-fx-background-color: white;");

        Scene tmp = new Scene(wrapper);

        Scene current = receiptRoot.getScene();
        if (current != null) {
            tmp.getStylesheets().addAll(current.getStylesheets());
        }

        wrapper.applyCss();
        wrapper.layout();

        javafx.scene.SnapshotParameters params = new javafx.scene.SnapshotParameters();
        params.setFill(javafx.scene.paint.Color.WHITE);

        return wrapper.snapshot(params, null);
    }

//    private void saveImageToResources(javafx.scene.image.WritableImage img) throws Exception {
//        java.nio.file.Path dir = java.nio.file.Paths.get("src", "main", "resources", "receipts");
//        java.nio.file.Files.createDirectories(dir);
//
//        String stamp = java.time.LocalDateTime.now()
//                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
//        String filename = "receipt-" + data.getOrderId() + "-" + stamp + ".png";
//
//        java.io.File out = dir.resolve(filename).toFile();
//        javax.imageio.ImageIO.write(
//                javafx.embed.swing.SwingFXUtils.fromFXImage(img, null),
//                "png",
//                out
//        );
//    }

    private void saveImageToResourcesAsync(javafx.scene.image.WritableImage img) throws Exception {
        var buf = javafx.embed.swing.SwingFXUtils.fromFXImage(img, null);

        java.nio.file.Path dir = java.nio.file.Paths.get("src", "main", "resources", "receipts");
        java.nio.file.Files.createDirectories(dir);

        String stamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        String filename = "receipt-" + data.getOrderId() + "-" + stamp + ".png";
        java.io.File out = dir.resolve(filename).toFile();

        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                javax.imageio.ImageIO.write(buf, "png", out);
            } catch (Exception e) {
                javafx.application.Platform.runLater(() ->
                        SnackBar.show(SnackBarType.ERROR, "Save Failed", e.getMessage(), Duration.seconds(3))
                );
            }
        });
    }

    private void printImage(javafx.scene.image.WritableImage img) {
        PrinterJob job = PrinterJob.createPrinterJob();
        if (job == null) return;

        var window = receiptRoot.getScene().getWindow();
        if (!job.showPrintDialog(window)) return;

        var pageLayout = job.getJobSettings().getPageLayout();

        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
        iv.setPreserveRatio(true);

        // scale to printable area
        iv.setFitWidth(pageLayout.getPrintableWidth());
        iv.setFitHeight(pageLayout.getPrintableHeight());

        boolean ok = job.printPage(pageLayout, iv);
        if (ok) job.endJob();
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