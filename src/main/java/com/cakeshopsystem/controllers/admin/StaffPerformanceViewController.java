package com.cakeshopsystem.controllers.admin;

import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;

public class
StaffPerformanceViewController {

    @FXML
    private AnchorPane staffPerformanceViewPane;

    @FXML
    private TableColumn<?, ?> tblColDate;

    @FXML
    private TableColumn<?, ?> tblColDetail;

    @FXML
    private TableColumn<?, ?> tblColNo;

    @FXML
    private TableColumn<?, ?> tblColType;

    @FXML
    private TableView<?> tblStaffPerformanceView;

}

