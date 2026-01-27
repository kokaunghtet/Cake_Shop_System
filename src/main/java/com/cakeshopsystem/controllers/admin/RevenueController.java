package com.cakeshopsystem.controllers.admin;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class RevenueController {

    @FXML
    private DatePicker customDatePicker;

    @FXML
    private ComboBox<?> filterCategory;

    @FXML
    private ComboBox<?> filterDate;

    @FXML
    private Label lblTotalCancel;

    @FXML
    private Label lblTotalCancelResult;

    @FXML
    private Label lblTotalMembers;

    @FXML
    private Label lblTotalMembersResult;

    @FXML
    private Label lblTotalOrders;

    @FXML
    private Label lblTotalOrdersResult;

    @FXML
    private Label lblTotalSales;

    @FXML
    private Label lblTotalSalesResult;

    @FXML
    private Button refreshBtn;

    @FXML
    private HBox totalCancelCard;

    @FXML
    private HBox totalMembersCard;

    @FXML
    private HBox totalOrdersCard;

    @FXML
    private HBox totalSalesCard;

    @FXML
    void clickRefreshChartBtn(ActionEvent event) {

    }

}
