package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.controllers.MainController;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.cache.UserCache;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

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

        btnAdd.setOnAction(e-> handleAddUser());
    }

    private void configureTable() {
        userTableView.setFixedCellSize(50);

        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUserName()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colStatus.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().isActive() ? "Active" : "Inactive")
        );
        colStatus.setCellFactory(_ -> new TableCell<User, String>() {

            private final Label statusLabel = new Label();
            private final HBox box = new HBox(statusLabel);

            {
                box.setAlignment(Pos.CENTER);
                setText(null); // this cell uses graphic only
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                statusLabel.setText(item); // "Active" or "Inactive"
                statusLabel.getStyleClass().setAll(
                        "status-" + item.replace(" ", "-").toLowerCase() + "-pill"
                );
                setGraphic(box);
            }
        });
    }

    private void handleAddUser() {
        MainController.togglePopupContent("/views/admin/AddUser.fxml");
    }
}
