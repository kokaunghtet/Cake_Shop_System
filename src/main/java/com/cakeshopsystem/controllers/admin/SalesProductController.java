package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.controllers.ProductCardController;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.models.ProductSales;
import com.cakeshopsystem.utils.dao.ProductDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SalesProductController {

    @FXML
    private ComboBox<String> dateFilter;

    @FXML
    private BarChart<String, Number> productBarChart;

    @FXML
    private ComboBox<String> productFilter;

    @FXML
    private PieChart productPieChart;

    @FXML
    private DatePicker specificDatePicker;

    @FXML
    private HBox topProductsContainer;

    private static final String PRODUCT_CARD_FXML = "/views/ProductCard.fxml";

    @FXML
    public void initialize() {

        dateFilter.getItems().addAll("Daily", "Weekly", "Monthly", "Yearly");
        dateFilter.getSelectionModel().selectFirst();
        dateFilter.setValue("Daily");

        specificDatePicker.setValue(java.time.LocalDate.now());

        productFilter.getItems().addAll(
                "All", "Cake", "Drink", "Baked Goods",
                "Accessory", "Custom Cake", "Diy Cake"
        );
        productFilter.getSelectionModel().selectFirst();
        productFilter.setValue("All");

        // --- Event handlers ---
        dateFilter.setOnAction(e -> refreshAll());
        productFilter.setOnAction(e -> refreshAll());
        specificDatePicker.setOnAction(e -> refreshAll());

        refreshChart();
    }

    private java.time.LocalDate[] getDateRange() {
        java.time.LocalDate endDate = specificDatePicker.getValue() != null
                ? specificDatePicker.getValue()
                : java.time.LocalDate.now();
        java.time.LocalDate startDate;

        String filter = dateFilter.getValue();
        if (filter == null) filter = "Daily";

        switch (filter) {
            case "Weekly" -> startDate = endDate.minusWeeks(1);
            case "Monthly" -> startDate = endDate.minusMonths(1);
            case "Yearly" -> startDate = endDate.minusYears(1);
            default -> startDate = endDate; // Daily
        }

        return new java.time.LocalDate[]{startDate, endDate};
    }

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
            case "Cake" -> 1;         // category_id for Cake
            case "Drink" -> 2;        // category_id for Drink
            case "Baked Goods" -> 3;  // category_id for Baked Goods
            case "Accessory" -> 4;    // category_id for Accessory
            default -> null;          // For "All", Custom Cake, Diy Cake
        };
    }

    public void loadTopProduct() {
        topProductsContainer.getChildren().clear();
        String filter = productFilter.getValue();

        java.time.LocalDate[] range = getDateRange();
        java.time.LocalDate start = range[0];
        java.time.LocalDate end = range[1];
        if (filter.equals("All")) return; // don't show top products for "All"

        ObservableList<Product> products = FXCollections.observableArrayList();


        switch (filter) {
            case "Drink" -> products = ProductDAO.getTopDrinks(start,end);
            case "Diy Cake" -> products = ProductDAO.getTopDiyCakes(start,end);
            case "Custom Cake" -> products = ProductDAO.getTopCustomCakes(start,end);
            default -> { // Cake, Baked Goods, Accessory
                products = ProductDAO.getTopSellingProductsByType(filter,start,end);
            }
        }

        if (!products.isEmpty()) {
            loadProductCards(products);
        }
    }


    private void refreshChart() {
        productBarChart.getData().clear();
        productPieChart.getData().clear();

        String filter = productFilter.getValue();
        if (filter == null || filter.isEmpty()) filter = "All";

        java.time.LocalDate[] range = getDateRange();
        java.time.LocalDate start = range[0];
        java.time.LocalDate end = range[1];

        ObservableList<ProductSales> sales;

        switch (filter) {
            case "All" -> {
                sales = FXCollections.observableArrayList();
                sales.addAll(ProductDAO.getSalesByCategoryId(1,start,end)); // Cake
                sales.addAll(ProductDAO.getSalesByCategoryId(3,start,end)); // Baked Goods
                sales.addAll(ProductDAO.getSalesByCategoryId(4,start,end)); // Accessory
                sales.addAll(ProductDAO.getTopDrinksSales(start,end));          // Drink
                sales.addAll(ProductDAO.getCustomCakeSales(start,end));
                sales.addAll(ProductDAO.getDiyCakeSales(start,end));
            }
            case "Drink" -> sales = ProductDAO.getTopDrinksSales(start,end);
            case "Diy Cake" -> sales = ProductDAO.getDiyCakeSales(start,end);
            case "Custom Cake" -> sales = ProductDAO.getCustomCakeSales(start,end);
            default -> {
                Integer categoryId = mapFilterToCategoryId(filter);
                sales = (categoryId != null) ? ProductDAO.getSalesByCategoryId(categoryId,start,end)
                        : FXCollections.observableArrayList();
            }
        }

        // --- Populate BarChart ---
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Product Sales");
        for (ProductSales ps : sales) {
            if (ps.getTotalSold() > 0) {
                series.getData().add(new XYChart.Data<>(ps.getName(), ps.getTotalSold()));
            }
        }
        if (!series.getData().isEmpty()) productBarChart.getData().add(series);

        // --- Populate PieChart ---
        for (ProductSales ps : sales) {
            if (ps.getTotalSold() > 0) {
                productPieChart.getData().add(new PieChart.Data(ps.getName(), ps.getTotalSold()));
            }
        }
    }

}