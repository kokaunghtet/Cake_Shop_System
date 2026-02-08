//package com.cakeshopsystem.controllers.admin;
//
//import com.cakeshopsystem.controllers.ProductCardController;
//import com.cakeshopsystem.models.Product;
//import com.cakeshopsystem.models.ProductSales;
//import com.cakeshopsystem.utils.chart.ChartColorFixer;
//import com.cakeshopsystem.utils.dao.ProductDAO;
//import javafx.application.Platform;
//import javafx.collections.FXCollections;
//import javafx.collections.ObservableList;
//import javafx.fxml.FXML;
//import javafx.fxml.FXMLLoader;
//import javafx.scene.Parent;
//import javafx.scene.chart.BarChart;
//import javafx.scene.chart.PieChart;
//import javafx.scene.chart.XYChart;
//import javafx.scene.control.ComboBox;
//import javafx.scene.control.DatePicker;
//import javafx.scene.layout.HBox;
//
//public class SalesProductController {
//
//    @FXML
//    private ComboBox<String> dateFilter;
//
//    @FXML
//    private BarChart<String, Number> productBarChart;
//
//    @FXML
//    private ComboBox<String> productFilter;
//
//    @FXML
//    private PieChart productPieChart;
//
//    @FXML
//    private DatePicker specificDatePicker;
//
//    @FXML
//    private HBox topProductsContainer;
//
//    private static final String PRODUCT_CARD_FXML = "/views/ProductCard.fxml";
//
//    @FXML
//    public void initialize() {
//
//        dateFilter.getItems().addAll("Daily", "Weekly", "Monthly", "Yearly");
//        dateFilter.getSelectionModel().selectFirst();
//        dateFilter.setValue("Daily");
//
//        specificDatePicker.setValue(java.time.LocalDate.now());
//
//        productFilter.getItems().addAll(
//                "All", "Cake", "Drink", "Baked Goods",
//                "Accessory"
//        );
//        productFilter.getSelectionModel().selectFirst();
//        productFilter.setValue("All");
//
//        // --- Event handlers ---
//        dateFilter.setOnAction(e -> refreshAll());
//        productFilter.setOnAction(e -> refreshAll());
//        specificDatePicker.setOnAction(e -> refreshAll());
//
//        refreshAll();
//    }
//
//    private java.time.LocalDate[] getDateRange() {
//        java.time.LocalDate endDate = specificDatePicker.getValue() != null
//                ? specificDatePicker.getValue()
//                : java.time.LocalDate.now();
//        java.time.LocalDate startDate;
//
//        String filter = dateFilter.getValue();
//        if (filter == null) filter = "Daily";
//
//        switch (filter) {
//            case "Weekly" -> startDate = endDate.minusWeeks(1);
//            case "Monthly" -> startDate = endDate.minusMonths(1);
//            case "Yearly" -> startDate = endDate.minusYears(1);
//            default -> startDate = endDate; // Daily
//        }
//
//        return new java.time.LocalDate[]{startDate, endDate};
//    }
//
//    private void refreshAll() {
//        refreshChart();
//        loadTopProduct();
//    }
//
//    private void loadProductCards(ObservableList<Product> products) {
//        topProductsContainer.getChildren().clear();
//        for (Product product : products) {
//            try {
//                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
//                Parent card = loader.load();
//
//                ProductCardController controller = loader.getController();
//                controller.setProductData(product);
//
//                topProductsContainer.getChildren().add(card);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    private Integer mapFilterToCategoryId(String filter) {
//        return switch (filter) {
//            case "Cake" -> 1;         // category_id for Cake
//            case "Drink" -> 2;        // category_id for Drink
//            case "Baked Goods" -> 3;  // category_id for Baked Goods
//            case "Accessory" -> 4;    // category_id for Accessory
//            default -> null;          // For "All"
//        };
//    }
//
//    public void loadTopProduct() {
//        topProductsContainer.getChildren().clear();
//        String filter = productFilter.getValue();
//
//        java.time.LocalDate[] range = getDateRange();
//        java.time.LocalDate start = range[0];
//        java.time.LocalDate end = range[1];
//
//        ObservableList<Product> products = FXCollections.observableArrayList();
//
//        if (filter.equals("All")) {
//            // 1) Top Cake (category_id = 1)
//            ObservableList<Product> topCake = ProductDAO.getTopSellingProductsByType("Cake", start, end);
//            if (!topCake.isEmpty()) products.add(topCake.getFirst());
//
//            // 2) Top Drink
//            ObservableList<Product> topDrink = ProductDAO.getTopDrinks(start, end);
//            if (!topDrink.isEmpty()) products.add(topDrink.getFirst());
//
//            // 3) Top Baked Goods (category_id = 3)
//            ObservableList<Product> topBaked = ProductDAO.getTopSellingProductsByType("Baked Goods", start, end);
//            if (!topBaked.isEmpty()) products.add(topBaked.getFirst());
//
//            // 4) Top Accessory (category_id = 4)
//            ObservableList<Product> topAccessory = ProductDAO.getTopSellingProductsByType("Accessory", start, end);
//            if (!topAccessory.isEmpty()) products.add(topAccessory.getFirst());
//
//            loadProductCards(products);
//            return;
//        }
//
//        // existing behavior for single filter
//        switch (filter) {
//            case "Drink" -> products = ProductDAO.getTopDrinks(start, end);
//            case "Diy Cake" -> products = ProductDAO.getTopDiyCakes(start, end);
//            case "Custom Cake" -> products = ProductDAO.getTopCustomCakes(start, end);
//            default -> products = ProductDAO.getTopSellingProductsByType(filter, start, end);
//        }
//
//        if (!products.isEmpty()) loadProductCards(products);
//    }
//
//    private void refreshChart() {
//        productBarChart.getData().clear();
//        productPieChart.getData().clear();
//
//        String filter = productFilter.getValue();
//        if (filter == null || filter.isEmpty()) filter = "All";
//
//        java.time.LocalDate[] range = getDateRange();
//        java.time.LocalDate start = range[0];
//        java.time.LocalDate end = range[1];
//
//        ObservableList<ProductSales> sales = FXCollections.observableArrayList();
//
//        switch (filter) {
//            case "All" -> {
//                sales.addAll(ProductDAO.getSalesByCategoryId(1, start, end));
//                sales.addAll(ProductDAO.getSalesByCategoryId(3, start, end));
//                sales.addAll(ProductDAO.getSalesByCategoryId(4, start, end));
//                sales.addAll(ProductDAO.getTopDrinksSales(start, end));
//            }
//            case "Drink" -> sales = ProductDAO.getTopDrinksSales(start, end);
//            default -> {
//                Integer categoryId = mapFilterToCategoryId(filter);
//                sales = (categoryId != null)
//                        ? ProductDAO.getSalesByCategoryId(categoryId, start, end)
//                        : FXCollections.observableArrayList();
//            }
//        }
//
//        // ---------------- BAR CHART ----------------
//        XYChart.Series<String, Number> series = new XYChart.Series<>();
//        series.setName("Product Sales");
//
//        for (ProductSales ps : sales) {
//            if (ps.getTotalSold() > 0) {
//                series.getData().add(
//                        new XYChart.Data<>(ps.getName(), ps.getTotalSold())
//                );
//            }
//        }
//
//        if (!series.getData().isEmpty()) {
//            productBarChart.getData().add(series);
//        }
//
//        // ---------------- PIE CHART (MERGE DUPLICATES) ----------------
//        java.util.Map<String, Integer> merged = new java.util.LinkedHashMap<>();
//
//        for (ProductSales ps : sales) {
//            merged.merge(ps.getName(), ps.getTotalSold(), Integer::sum);
//        }
//
//        merged.forEach((name, qty) -> {
//            if (qty > 0) {
//                productPieChart.getData().add(new PieChart.Data(name, qty));
//            }
//        });
//
//        Platform.runLater(() -> {
//            ChartColorFixer.fixBarChartColors(productBarChart);
//            ChartColorFixer.fixPieChartColors(productPieChart);
//        });
//    }
//
//
//}

package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.controllers.ProductCardController;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.models.ProductSales;
import com.cakeshopsystem.utils.chart.ChartColorFixer;
import com.cakeshopsystem.utils.dao.ProductDAO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

public class SalesProductController {

    @FXML private ComboBox<String> dateFilter;
    @FXML private BarChart<String, Number> productBarChart;
    @FXML private ComboBox<String> productFilter;
    @FXML private PieChart productPieChart;
    @FXML private DatePicker specificDatePicker;
    @FXML private HBox topProductsContainer;

    private static final String PRODUCT_CARD_FXML = "/views/ProductCard.fxml";

    // =========================
    // Range picker state (ONE DatePicker, TWO clicks)
    // =========================
    private LocalDate startDate;
    private LocalDate endDate;
    private final Set<LocalDate> selectedRange = new HashSet<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    public void initialize() {

        dateFilter.getItems().addAll("Daily", "Weekly", "Monthly", "Yearly");
        dateFilter.getSelectionModel().selectFirst();
        dateFilter.setValue("Daily");

        productFilter.getItems().addAll("All", "Cake", "Drink", "Baked Goods", "Accessory");
        productFilter.getSelectionModel().selectFirst();
        productFilter.setValue("All");

        // Enable range selection on the SAME datepicker
        configureRangeDatePicker();

        // Default selection: today only
        startDate = LocalDate.now();
        endDate = null;
        selectedRange.clear();
        selectedRange.add(startDate);

        // keep calendar anchored
        specificDatePicker.setValue(startDate);
        updateDatePickerDisplay();

        // Event handlers (date range refresh happens after second click; combos refresh immediately)
        dateFilter.setOnAction(e -> refreshAll());
        productFilter.setOnAction(e -> refreshAll());

        refreshAll();
    }

    // =========================
    // Range DatePicker (1 control, 2 clicks)
    // =========================
    private void configureRangeDatePicker() {
        specificDatePicker.setEditable(false);

        // show "start → end" in the editor
        specificDatePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (startDate == null) return "";
                if (endDate == null) return formatter.format(startDate);
                return formatter.format(startDate) + " → " + formatter.format(endDate);
            }

            @Override
            public LocalDate fromString(String string) {
                return null; // no typing
            }
        });

        specificDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                // reset (cells are reused)
                setStyle("");
                setOnMouseClicked(null);

                if (empty || item == null) return;

                // range highlight
                if (selectedRange.contains(item)) {
                    setStyle("-fx-background-color: rgba(76,175,80,0.25);");
                }
                // start highlight
                if (startDate != null && item.equals(startDate)) {
                    setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
                }
                // end highlight
                if (endDate != null && item.equals(endDate)) {
                    setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
                }

                final LocalDate clicked = item;
                setOnMouseClicked(e -> {
                    handleDateSelection(clicked);
                    e.consume();
                });
            }
        });
    }

    private void handleDateSelection(LocalDate clickedDate) {
        // keep popup month in sync with click
        specificDatePicker.setValue(clickedDate);

        // FIRST click (or restart)
        if (startDate == null || endDate != null) {
            startDate = clickedDate;
            endDate = null;

            selectedRange.clear();
            selectedRange.add(startDate);

            updateDatePickerDisplay();

            // reopen so user can choose end date
            Platform.runLater(specificDatePicker::show);
            return; // don't refresh yet
        }

        // SECOND click (set end)
        if (clickedDate.isBefore(startDate)) {
            endDate = startDate;
            startDate = clickedDate;
        } else {
            endDate = clickedDate;
        }

        fillDateRange();
        updateDatePickerDisplay();
        specificDatePicker.hide();

        // refresh only after full range is selected
        refreshAll();
    }

    private void fillDateRange() {
        selectedRange.clear();
        if (startDate == null) return;

        LocalDate end = (endDate != null) ? endDate : startDate;

        LocalDate d = startDate;
        while (!d.isAfter(end)) {
            selectedRange.add(d);
            d = d.plusDays(1);
        }
    }

    private void updateDatePickerDisplay() {
        // force editor update (converter is used by skin, but this makes it immediate)
        if (startDate != null && endDate != null) {
            specificDatePicker.getEditor().setText(
                    formatter.format(startDate) + " → " + formatter.format(endDate)
            );
        } else if (startDate != null) {
            specificDatePicker.getEditor().setText(formatter.format(startDate));
        } else {
            specificDatePicker.getEditor().clear();
        }
    }

    // =========================
    // Date range used by charts/top products
    // =========================
    private LocalDate[] getDateRange() {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now();
        LocalDate end = (endDate != null) ? endDate : start;

        if (end.isBefore(start)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        return new LocalDate[]{start, end};
    }

    // =========================
    // Refresh + Loaders
    // =========================
    private void refreshAll() {
        refreshChart();
        loadTopProduct();
    }

    private void loadProductCards(ObservableList<Product> products) {
        topProductsContainer.getChildren().clear();
        for (Product product : products) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setProductData(product);

                topProductsContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Integer mapFilterToCategoryId(String filter) {
        return switch (filter) {
            case "Cake" -> 1;
            case "Drink" -> 2;
            case "Baked Goods" -> 3;
            case "Accessory" -> 4;
            default -> null;
        };
    }

    public void loadTopProduct() {
        topProductsContainer.getChildren().clear();
        String filter = productFilter.getValue();

        LocalDate[] range = getDateRange();
        LocalDate start = range[0];
        LocalDate end = range[1];

        ObservableList<Product> products = FXCollections.observableArrayList();

        if ("All".equals(filter)) {
            ObservableList<Product> topCake = ProductDAO.getTopSellingProductsByType("Cake", start, end);
            if (!topCake.isEmpty()) products.add(topCake.getFirst());

            ObservableList<Product> topDrink = ProductDAO.getTopDrinks(start, end);
            if (!topDrink.isEmpty()) products.add(topDrink.getFirst());

            ObservableList<Product> topBaked = ProductDAO.getTopSellingProductsByType("Baked Goods", start, end);
            if (!topBaked.isEmpty()) products.add(topBaked.getFirst());

            ObservableList<Product> topAccessory = ProductDAO.getTopSellingProductsByType("Accessory", start, end);
            if (!topAccessory.isEmpty()) products.add(topAccessory.getFirst());

            loadProductCards(products);
            return;
        }

        switch (filter) {
            case "Drink" -> products = ProductDAO.getTopDrinks(start, end);
            case "Diy Cake" -> products = ProductDAO.getTopDiyCakes(start, end);
            case "Custom Cake" -> products = ProductDAO.getTopCustomCakes(start, end);
            default -> products = ProductDAO.getTopSellingProductsByType(filter, start, end);
        }

        if (!products.isEmpty()) loadProductCards(products);
    }

    private void refreshChart() {
        productBarChart.getData().clear();
        productPieChart.getData().clear();

        String filter = productFilter.getValue();
        if (filter == null || filter.isBlank()) filter = "All";

        LocalDate[] range = getDateRange();
        LocalDate start = range[0];
        LocalDate end = range[1];

        ObservableList<ProductSales> sales = FXCollections.observableArrayList();

        switch (filter) {
            case "All" -> {
                sales.addAll(ProductDAO.getSalesByCategoryId(1, start, end));
                sales.addAll(ProductDAO.getSalesByCategoryId(3, start, end));
                sales.addAll(ProductDAO.getSalesByCategoryId(4, start, end));
                sales.addAll(ProductDAO.getTopDrinksSales(start, end));
            }
            case "Drink" -> sales = ProductDAO.getTopDrinksSales(start, end);
            default -> {
                Integer categoryId = mapFilterToCategoryId(filter);
                sales = (categoryId != null)
                        ? ProductDAO.getSalesByCategoryId(categoryId, start, end)
                        : FXCollections.observableArrayList();
            }
        }

        // ---------------- BAR CHART ----------------
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Product Sales");

        for (ProductSales ps : sales) {
            if (ps.getTotalSold() > 0) {
                series.getData().add(new XYChart.Data<>(ps.getName(), ps.getTotalSold()));
            }
        }
        if (!series.getData().isEmpty()) productBarChart.getData().add(series);

        // ---------------- PIE CHART (MERGE DUPLICATES) ----------------
        LinkedHashMap<String, Integer> merged = new LinkedHashMap<>();
        for (ProductSales ps : sales) {
            merged.merge(ps.getName(), ps.getTotalSold(), Integer::sum);
        }
        merged.forEach((name, qty) -> {
            if (qty > 0) productPieChart.getData().add(new PieChart.Data(name, qty));
        });

        Platform.runLater(() -> Platform.runLater(() -> {
            ChartColorFixer.fixBarChartColors(productBarChart);
            ChartColorFixer.fixPieChartColors(productPieChart);
        }));
    }
}
