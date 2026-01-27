package com.cakeshopsystem.controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

import javax.swing.text.html.HTMLDocument;
import java.time.LocalDate;

public class SalesProductController {

    @FXML
    private ComboBox<String> dateFilter;

    @FXML
    private BarChart<String,Number> productBarChart;

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

    @FXML
    public void initialize(){

        dateFilter.getItems().addAll("Daily","Weekly","Monthly","Yearly");
        dateFilter.getSelectionModel().selectFirst();
        specificDatePicker.setValue(LocalDate.now());
        productFilter.getItems().addAll("Cake","Drink","Dessert","Accessories","Custom cake","Diy Cake");
        productFilter.getSelectionModel().selectFirst();
        loadPieChart();
        loadBarChartAsync();
    }
    private void loadBarChartAsync() {

        // Clear old data
        productBarChart.getData().clear();

        // X-axis = product name, Y-axis = quantity sold
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Product Sales");

        // Dummy data (replace with DB later)
        series.getData().add(new XYChart.Data<>("Chocolate Cake", 45));
        series.getData().add(new XYChart.Data<>("Vanilla Cake", 30));
        series.getData().add(new XYChart.Data<>("Coffee", 60));
        series.getData().add(new XYChart.Data<>("Cupcake", 25));
        series.getData().add(new XYChart.Data<>("Custom Cake", 15));

        productBarChart.getData().add(series);
    }

    private void loadPieChart() {

        productPieChart.getData().clear();

        PieChart.Data cake = new PieChart.Data("Cake", 40);
        PieChart.Data drink = new PieChart.Data("Drink", 30);
        PieChart.Data dessert = new PieChart.Data("Dessert", 20);
        PieChart.Data accessories = new PieChart.Data("Accessories", 10);

        productPieChart.getData().addAll(cake, drink, dessert, accessories);
    }


}
