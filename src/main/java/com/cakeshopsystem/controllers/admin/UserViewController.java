package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.controllers.MainController;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.ImageHelper;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.cache.UserCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.UserDAO;
import com.cakeshopsystem.utils.session.SessionManager;
import com.sun.tools.javac.Main;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class UserViewController {

    @FXML
    private Button btnAdd;

    @FXML
    private ComboBox<String> btnFilter;

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
//        ObservableList<User> userList = UserDAO.getAllUsers();
        ObservableList<User> userList = UserCache.getUsersList();

        for(User u : userList) {
            System.out.println(u.getUserName());
        }
        userTableView.setItems(userList);

        configureTable();

        btnAdd.setOnAction(e -> handleAddUser());

        configureRowDoubleClick();
    }

    private void editUserStatus(int userId) {
        User user = UserCache.getUserById(userId);
        Label name = new Label(user.getUserName());
        name.getStyleClass().add("sub-title");
        Button saveBtn = new Button("Save");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("ACTIVE", "INACTIVE");
        statusBox.setValue(user.isActive() ? "ACTIVE" : "INACTIVE");

//        saveBtn.setOnAction(e -> {
//            boolean newActive = "ACTIVE".equals(statusBox.getValue());
//            user.setActive(newActive);
//            UserDAO.updateUser(user);
//            userTableView.refresh();
//            MainController.handleClosePopupContent();
//        });

        saveBtn.setOnAction(e -> {
            boolean newActive = "ACTIVE".equals(statusBox.getValue());
            user.setActive(newActive);

            Task<Void> task = new Task<>() {
                @Override protected Void call() {
                    UserDAO.updateUser(user);
                    return null;
                }
            };

            task.setOnSucceeded(ev -> {
                userTableView.refresh();
                MainController.handleClosePopupContent();
            });

            task.setOnFailed(ev -> {
                MainController.handleClosePopupContent();
            });

            new Thread(task, "update-user-status").start();
        });


        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("cancel-btn");
        cancelBtn.setOnAction(e -> MainController.handleClosePopupContent());

        HBox btnRow = new HBox(20, cancelBtn, saveBtn);
        VBox box = new VBox(20, name, statusBox, btnRow);
        box.setPadding(new Insets(25));
        box.setAlignment(Pos.CENTER_LEFT);

        MainController.togglePopupContent(box);
    }

    // ====================== Row Double Click ======================
    private void configureRowDoubleClick() {
        userTableView.setRowFactory(_ -> {
            TableRow<User> row = new TableRow<>();
            row.setOnMouseEntered(event->row.setCursor(Cursor.HAND));
            row.setOnMouseExited(event->row.setCursor(Cursor.DEFAULT));
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 2) {
                    User selectedUser = row.getItem();
                    if(SessionManager.getUser().getUserId() == selectedUser.getUserId()) {
                        SnackBar.show(SnackBarType.ERROR, "Failed", "Can't update the own status", Duration.seconds(2));
                        return;
                    }
                    editUserStatus(selectedUser.getUserId());
                }
            });
            return row;
        });
    }

    private void configureTable() {
        userTableView.setFixedCellSize(SessionManager.TABLE_CELL_SIZE);

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
                setText(null);
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setGraphic(null);
                    return;
                }

                statusLabel.setText(item);
                statusLabel.getStyleClass().setAll("status-" + item.replace(" ", "-").toLowerCase() + "-pill");
                setGraphic(box);
            }
        });
    }

    private void handleAddUser() {
        MainController.togglePopupContent("/views/admin/AddUser.fxml");
    }
}
