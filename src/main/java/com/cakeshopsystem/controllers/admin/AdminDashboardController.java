
package com.cakeshopsystem.controllers.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

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
    void clickRevenueBtn(ActionEvent event) {


    }

    @FXML
    void clickSalesProductBtn(ActionEvent event) throws IOException {
        // Load the FXML
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/SalesProduct.fxml"));
        Parent root = loader.load();

        // Clear existing content and add the new one
        contentPane.getChildren().clear();
        contentPane.getChildren().add(root);
    }

    @FXML
    void clickStaffPerformanceBtn(ActionEvent event) {

    }

}

