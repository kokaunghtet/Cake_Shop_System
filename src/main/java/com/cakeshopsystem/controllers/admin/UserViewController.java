package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.controllers.MainController;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.ImageHelper;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.cache.UserCache;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
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
    private TableColumn<User, String> colRole;

    @FXML
    private TableColumn<User, String> colAvatar;

    @FXML
    private TextField txtSearch;

    public void initialize() {
        ObservableList<User> userList = UserCache.getUsersList();
        userTableView.setItems(userList);

        configureTable();

        btnAdd.setOnAction(e -> handleAddUser());
    }

    private void configureTable() {
        userTableView.setFixedCellSize(50);

        colAvatar.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getImagePath()));

        colAvatar.setCellFactory(col -> new TableCell<>() {
            private final ImageView avatar = new ImageView();

            {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                ImageHelper.applyCircularAvatar(avatar, 32);
            }

            @Override
            protected void updateItem(String path, boolean empty) {
                super.updateItem(path, empty);

                if (empty || path == null || path.isBlank()) {
                    setGraphic(null);
                    avatar.setImage(null);
                    avatar.setViewport(null);
                    return;
                }

                avatar.setImage(ImageHelper.load(path));
                ImageHelper.applyCircularAvatar(avatar, 32);
                setGraphic(avatar);
            }
        });
        colName.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUserName()));
        colEmail.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmail()));
        colRole.setCellValueFactory(d-> new SimpleStringProperty(String.valueOf(RoleCache.getRoleById(d.getValue().getRole()))));
        colStatus.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActive() ? "Active" : "Inactive"));
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
                statusLabel.getStyleClass().setAll("status-" + item.replace(" ", "-").toLowerCase() + "-pill");
                setGraphic(box);
            }
        });
    }

    private void handleAddUser() {
        MainController.togglePopupContent("/views/admin/AddUser.fxml");
    }
}
