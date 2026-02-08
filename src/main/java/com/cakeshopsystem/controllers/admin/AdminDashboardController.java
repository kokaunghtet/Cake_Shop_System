package com.cakeshopsystem.controllers.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

import java.io.IOException;

public class AdminDashboardController {

    @FXML
    private StackPane contentPane;

    @FXML
    private BorderPane dashboardPane;

    @FXML
    private Button revenueBtn;

    @FXML
    private Button salesProductBtn;

    @FXML
    private Button staffPerformanceBtn;

    @FXML
    public void initialize() throws IOException {
        clickSalesProductBtn();
    }

    @FXML
    private void clickRevenueBtn() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/Revenue.fxml"));
        Parent root = loader.load();

        // Clear existing content and add the new one
        contentPane.getChildren().clear();
        contentPane.getChildren().add(root);
    }

    @FXML
    private void clickSalesProductBtn() throws IOException {
        // Load the FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/SalesProduct.fxml"));
        Parent root = loader.load();

        // Clear existing content and add the new one
        contentPane.getChildren().clear();
        contentPane.getChildren().add(root);
    }

    @FXML
    private void clickStaffPerformanceBtn() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/StaffPerformance.fxml"));
        Parent root = loader.load();

        // Clear existing content and add the new one
        contentPane.getChildren().clear();
        contentPane.getChildren().add(root);
    }

}

