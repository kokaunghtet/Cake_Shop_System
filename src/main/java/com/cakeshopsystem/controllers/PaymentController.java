package com.cakeshopsystem.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;

public class PaymentController {

    @FXML
    private TableColumn<?, ?> colNumber;

    @FXML
    private TableColumn<?, ?> colPaymentType;

    @FXML
    private TableColumn<?, ?> colStatus;

    @FXML
    private VBox paymentPane;

    @FXML
    private TableView<?> tblPayment;

}
