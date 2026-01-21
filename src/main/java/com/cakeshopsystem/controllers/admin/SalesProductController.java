package com.cakeshopsystem.controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class SalesProductController {

    @FXML
    private ComboBox<?> dateFilter;

    @FXML
    private BarChart<?, ?> productBarChart;

    @FXML
    private ComboBox<?> productFilter;

    @FXML
    private PieChart productPieChart;

    @FXML
    private BorderPane salesProductPane;

    @FXML
    private DatePicker specificDatePicker;

    @FXML
    private HBox topProductsContainer;

}
