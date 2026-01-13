package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.cache.UserCache;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class UserViewController {

    @FXML
    private Button btnAdd;

    @FXML
    private ComboBox<?> btnFilter;

    @FXML
    private TableView<User> userTableView;

    @FXML
    private TableColumn<User, String> colEmail;

    @FXML
    private TableColumn<User, String> colName;

    @FXML
    private TableColumn<User, String> colStatus;

    @FXML
    private TextField txtSearch;

    public void initialize() {
        ObservableList<User> userList = UserCache.getUsersList();
        userTableView.setItems(userList);

        configureTable();
    }

    private void configureTable() {
        userTableView.setFixedCellSize(40);

        colName.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getUserName()));
        colEmail.setCellValueFactory(d->new SimpleStringProperty(d.getValue().getEmail()));
        colStatus.setCellValueFactory(d->{
            String status = d.getValue().isActive()? "Active": "Inactive";
            return new SimpleStringProperty(status);
        });
    }
}
