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
import javafx.collections.ListChangeListener;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.input.KeyCode;

import java.util.Set;
import java.util.TreeSet;


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

    private FilteredList<User> filteredUsers;
    private SortedList<User> sortedUsers;


    public void initialize() {
        ObservableList<User> userList = UserCache.getUsersList();

        filteredUsers = new FilteredList<>(userList, u -> true);
        sortedUsers = new SortedList<>(filteredUsers);
        sortedUsers.comparatorProperty().bind(userTableView.comparatorProperty());

        userTableView.setItems(sortedUsers);

        configureTable();

        btnAdd.setOnAction(e -> handleAddUser());

        configureRowDoubleClick();
        configureSearch();
        configureFilter();

        applyFilters();
    }

    private void editUserStatus(int userId) {
        User user = UserCache.getUserById(userId);
        Label name = new Label(user.getUserName());
        name.getStyleClass().add("sub-title");
        Button saveBtn = new Button("Save");

        ComboBox<String> statusBox = new ComboBox<>();
        statusBox.getItems().addAll("ACTIVE", "INACTIVE");
        statusBox.setValue(user.isActive() ? "ACTIVE" : "INACTIVE");

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

    private void configureSearch() {
        txtSearch.textProperty().addListener((obs, old, val) -> applyFilters());

        // Optional: ESC clears search quickly
        txtSearch.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) txtSearch.clear();
        });
    }

    private void configureFilter() {
        rebuildFilterOptions();

        btnFilter.valueProperty().addListener((obs, old, val) -> applyFilters());

        // If users list changes (added/removed), update role options
        filteredUsers.getSource().addListener((ListChangeListener<User>) change -> rebuildFilterOptions());
    }

    private void rebuildFilterOptions() {
        String keep = btnFilter.getValue();

        btnFilter.getItems().clear();
        btnFilter.getItems().addAll("All", "Active", "Inactive");

        // Build unique role names from current users using existing RoleCache.getRoleById(...)
        Set<String> roleNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (User u : filteredUsers.getSource()) {
            roleNames.add(roleNameOf(u));
        }
        btnFilter.getItems().addAll(roleNames);

        if (keep != null && btnFilter.getItems().contains(keep)) {
            btnFilter.setValue(keep);
        } else {
            btnFilter.setValue("All");
        }
    }

    private void applyFilters() {
        final String q = safeLower(txtSearch.getText()).trim();
        final String selected = btnFilter.getValue(); // "All" / "Active" / "Inactive" / roleName

        filteredUsers.setPredicate(user -> {
            if (user == null) return false;

            // -------- search match --------
            boolean matchesSearch = q.isEmpty()
                    || safeLower(user.getUserName()).contains(q)
                    || safeLower(user.getEmail()).contains(q)
                    || safeLower(roleNameOf(user)).contains(q)
                    || safeLower(user.isActive() ? "active" : "inactive").contains(q);

            if (!matchesSearch) return false;

            // -------- filter match --------
            if (selected == null || selected.equalsIgnoreCase("All")) return true;

            if (selected.equalsIgnoreCase("Active")) return user.isActive();
            if (selected.equalsIgnoreCase("Inactive")) return !user.isActive();

            // Otherwise treat selection as role name
            return roleNameOf(user).equalsIgnoreCase(selected);
        });
    }

    private String roleNameOf(User user) {
        // You already use String.valueOf(RoleCache.getRoleById(...)) in the table
        // Keep consistent and safe.
        Object role = RoleCache.getRoleById(user.getRole());
        return role == null ? "Unknown" : String.valueOf(role);
    }

    private String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

}
