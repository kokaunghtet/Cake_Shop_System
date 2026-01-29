package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.controllers.ProductCardController;
import com.cakeshopsystem.models.Cake;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.models.ProductSales;
import com.cakeshopsystem.utils.constants.CakeType;
import com.cakeshopsystem.utils.dao.CakeDAO;
import com.cakeshopsystem.utils.dao.ProductDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import javax.swing.text.html.HTMLDocument;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Timer;

import static com.cakeshopsystem.controllers.ProductViewController.PRODUCT_CARD_FXML;

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
    private BorderPane salesProductPane;

    @FXML
    private DatePicker specificDatePicker;

    @FXML
    private HBox topProductsContainer;

    private volatile Task<Void> activeTask;

    @FXML
    public void initialize() {

        dateFilter.getItems().addAll("Daily", "Weekly", "Monthly", "Yearly");
        dateFilter.getSelectionModel().selectFirst();
        dateFilter.setValue("Daily");

        specificDatePicker.setValue(LocalDate.now());

        productFilter.getItems().addAll(
                "All","Cake", "Drink", "Baked Goods",
                "Accessory", "Custom cake", "Diy Cake"
        );
        productFilter.getSelectionModel().selectFirst();
        productFilter.setValue("All");

        dateFilter.setOnAction(e -> {
            refreshChart();
            loadTopProduct();
        });

        productFilter.setOnAction(e -> {
            refreshChart();
            loadTopProduct();
        });

        specificDatePicker.setOnAction(e -> {
            refreshChart();
            loadTopProduct();
        });


    }

    private void loadProductCards(ObservableList<Product> products) {

        for (Product product : products) {
            try {
                FXMLLoader loader =
                        new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setProductData(product);

                topProductsContainer.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


    public void loadTopProduct() {

        topProductsContainer.getChildren().clear();
        String filter = productFilter.getValue();

        switch (filter) {

            case "Cake", "Baked Goods", "Accessory" ->
                    loadTopNormalProducts(filter);

            case "Drink" ->
                    loadTopDrinks();

            case "Diy Cake" ->
                    loadTopDiyCakes();

            case "Custom Cake" ->
                    loadTopCustomCakes();
        }
    }
    private void loadTopNormalProducts(String filter) {

        ObservableList<Product> products =
                ProductDAO.getTopSellingProductsByType(mapFilter(filter));

        loadProductCards(products);
    }

    private String mapFilter(String uiValue) {
        return switch (uiValue) {
            case "Cake" -> "CAKE";
            case "Drink" -> "DRINK";
            case "Baked Goods" -> "BAKED GOODS";
            case "Accessory" -> "ACCESSORY";
            default -> null; // For "All" or unsupported types
        };
    }

    private void loadTopDrinks() {
        ObservableList<Product> products = ProductDAO.getTopDrinks();
        loadProductCards(products);
    }


    public void loadTopDiyCakes(){
        ObservableList<Product> products = ProductDAO.getTopDiyCakes();

        loadProductCards(products);
    }
    public void loadTopCustomCakes(){
        ObservableList<Product> products = ProductDAO.getTopCustomCakes();

        loadProductCards(products);
    }



    private void refreshChart(){
        loadBarAndPieChart();

    }

    private void loadBarAndPieChart() {
        productBarChart.getData().clear();
        productPieChart.getData().clear();

        String filter = productFilter.getValue();
        ObservableList<ProductSales> sales = FXCollections.observableArrayList();

        // --- Fetch sales depending on the filter ---
        switch (filter) {
            case "Cake", "Baked Goods", "Accessory" ->
                    sales = ProductDAO.getSalesByProductType(mapFilter(filter));
            case "Diy Cake" ->
                    sales = ProductDAO.getDiyCakeSales();
            case "Custom Cake" ->
                    sales = ProductDAO.getCustomCakeSales();
            case "Drink" ->
                    sales = ProductDAO.getTopDrinksSales(); // you may need to add this method returning ProductSales
            case "All" ->
                    sales = ProductDAO.getAllProductSales(); // add a DAO method to fetch all
        }

        // --- Populate BarChart ---
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Product Sales");

        for (ProductSales ps : sales) {
            series.getData().add(new XYChart.Data<>(ps.getName(), ps.getTotalSold()));
        }

        if (!series.getData().isEmpty()) {
            productBarChart.getData().add(series);
        }

        // --- Populate PieChart ---
        for (ProductSales ps : sales) {
            productPieChart.getData().add(new PieChart.Data(ps.getName(), ps.getTotalSold()));
        }
    }




}
