package com.cakeshopsystem.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;

public class UserViewController {

    @FXML
    private Button btnAdd;

    @FXML
    private ComboBox<?> btnFilter;

    @FXML
    private TableColumn<?, ?> colEmail;

    @FXML
    private TableColumn<?, ?> colName;

    @FXML
    private TableColumn<?, ?> colStatue;

    @FXML
    private TextField txtSearch;

}
