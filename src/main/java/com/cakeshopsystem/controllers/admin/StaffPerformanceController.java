
package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.controllers.MainController;
import com.cakeshopsystem.models.ActivityItem;
import com.cakeshopsystem.models.Order;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.OrderDAO;
import com.cakeshopsystem.utils.dao.StaffPerformanceDAO;
import com.cakeshopsystem.utils.databaseconnection.DB;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.controlsfx.control.BreadCrumbBar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import com.cakeshopsystem.utils.cache.UserCache;
import com.cakeshopsystem.utils.cache.RoleCache;


public class StaffPerformanceController {

    @FXML
    private ComboBox<String> periodCombo;
    @FXML
    private DatePicker datePicker;
    @FXML
    private FlowPane cardContainer;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private BreadCrumbBar<String> breadCrumbBar;

    @FXML
    private AnchorPane rootPane;

    private TreeItem<String> rootCrumb;
    private BreadCrumbBar<String> breadcrumbBar;

    // ===== TableView (CREATED IN CODE) =====
    private TableView<ActivityItem> tableView;
    private TableColumn<ActivityItem, String> typeCol;
    private TableColumn<ActivityItem, String> detailsCol;
    private TableColumn<ActivityItem, String> dateCol;

    public void initialize() {
        periodCombo.getItems().addAll("Daily", "Weekly", "Monthly", "Yearly");
        periodCombo.setValue("Daily");
        datePicker.setValue(LocalDate.now());

        // breadCrumbBar.setVisible(false);

        scrollPane.setContent(cardContainer);

        cardContainer.prefWidthProperty()
                .bind(scrollPane.widthProperty().subtract(20));

        cardContainer.setAlignment(Pos.TOP_LEFT);
        cardContainer.setHgap(15);
        cardContainer.setVgap(15);


        rootCrumb = new TreeItem<>("Home");
        breadCrumbBar.setVisible(false);
        breadCrumbBar.setSelectedCrumb(rootCrumb);

        breadCrumbBar.setOnCrumbAction(event -> handleBreadcrumbClick(event.getSelectedCrumb()));

        periodCombo.setOnAction(e -> reloadCurrentView());
        datePicker.setOnAction(e -> reloadCurrentView());

        loadCashierCards();
        setupTable();
    }


    private void handleBreadcrumbClick(TreeItem<String> selected) {
        if (selected == null) return;

        if ("Home".equals(selected.getValue())) {
            scrollPane.setContent(cardContainer);
            loadCashierCards();
            breadCrumbBar.setVisible(false);
        }
    }

    private void reloadCurrentView() {
        if (!breadCrumbBar.isVisible()) {
            loadCashierCards();
            return;
        }

        String current = breadCrumbBar.getSelectedCrumb().getValue();

        if ("Home".equals(current)) {
            loadCashierCards();
            breadCrumbBar.setVisible(false);
        } else {
            int userId = getUserIdByName(current);
            if (userId != -1) {
                loadStaffTable(userId);
            }
        }
    }

    private String getDateFilter(String column) {
        LocalDate date = datePicker.getValue() != null ? datePicker.getValue() : LocalDate.now();
        switch (periodCombo.getValue()) {
            case "Daily":
                return "DATE(" + column + ") = '" + date + "'";
            case "Weekly":
                LocalDate start = date.with(DayOfWeek.MONDAY);
                LocalDate end = date.with(DayOfWeek.SUNDAY);
                return "DATE(" + column + ") BETWEEN '" + start + "' AND '" + end + "'";
            case "Monthly":
                LocalDate first = date.withDayOfMonth(1);
                LocalDate last = date.with(TemporalAdjusters.lastDayOfMonth());
                return "DATE(" + column + ") BETWEEN '" + first + "' AND '" + last + "'";
            case "Yearly":
                LocalDate firstY = date.withDayOfYear(1);
                LocalDate lastY = date.withDayOfYear(date.lengthOfYear());
                return "DATE(" + column + ") BETWEEN '" + firstY + "' AND '" + lastY + "'";
            default:
                return "1=1";
        }
    }

    public void loadCashierCards() {
        cardContainer.getChildren().clear();

        int cashierRoleId = RoleCache.getRoleIdByName("Cashier");
        ObservableList<User> cashiers = UserCache.getUsersByRole(cashierRoleId);

        if (cashiers.isEmpty()) {
            SnackBar.show(SnackBarType.ERROR, "Failed", "No Cashier found in cache", Duration.seconds(2));
            return;
        }

        String dateFilter = getDateFilter("order_date"); // use the same filter logic

        for (User user : cashiers) {
            if (!user.isActive()) continue;

            int totalOrders = getOrderCountForUser(user.getUserId(), dateFilter);
            addUserCardToFlow(user, totalOrders);
        }
    }

    private void addUserCardToFlow(User user, int orderCount) {
        try {
            FXMLLoader loader =
                    new FXMLLoader(getClass().getResource("/views/admin/UserCard.fxml"));
            VBox card = loader.load();

            UserCardController controller = loader.getController();
            controller.setUser(user, orderCount);

            card.setOnMouseClicked(e ->
                    pushBreadcrumbAndLoadDetails(user.getUserId(), user.getUserName()));

            cardContainer.getChildren().add(card);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getOrderCountForUser(int userId, String dateFilter) {
        // Apply the same date filter as the table
        // String dateFilter = getDateFilter("order_date"); // make sure 'order_date' matches your DB column
        String sql = "SELECT COUNT(*) AS total FROM orders WHERE user_id = ? AND " + dateFilter;

        try (var conn = DB.connect();
             var ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            var rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }


    private void pushBreadcrumbAndLoadDetails(int userId, String userName) {
        breadCrumbBar.setVisible(true);
        rootCrumb.getChildren().clear();

        //TreeItem<String> homeCrumb = new TreeItem<>("Home");
        TreeItem<String> userCrumb = new TreeItem<>(userName);

        rootCrumb.getChildren().add(userCrumb);
        //homeCrumb.getChildren().add(userCrumb);

        breadCrumbBar.setSelectedCrumb(userCrumb);

        loadStaffTable(userId);
    }


    private int getUserIdByName(String name) {
        try (var conn = DB.connect();
             var ps = conn.prepareStatement("""
                        SELECT U.user_id FROM users U
                        JOIN roles R ON U.role_id = R.role_id
                        WHERE U.user_name = ? AND R.role_name = 'Cashier' AND U.is_active = TRUE
                     """)) {
            ps.setString(1, name);
            var rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("user_id");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    private void loadStaffTable(int userId) {
        String filter = getDateFilter("activity_date");

        ObservableList<ActivityItem> data =
                StaffPerformanceDAO.getActivities(userId, filter);

        tableView.setItems(data);

        // ---- Table sizing / behavior ----
        tableView.setColumnResizePolicy(javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setFixedCellSize(SessionManager.TABLE_CELL_SIZE);                 // row height
        tableView.setPrefHeight(520);                   // visible height
        tableView.setMaxWidth(900);                     // prevent stretching
        tableView.setPrefWidth(900);

        // ---- Wrapper (centers the table) ----
        VBox wrapper = new VBox(tableView);
        wrapper.setAlignment(javafx.geometry.Pos.CENTER);
        wrapper.setPadding(new javafx.geometry.Insets(15));

        // If you want the table to keep its height and not expand vertically, comment this out:
        // VBox.setVgrow(tableView, Priority.ALWAYS);

        // ---- ScrollPane styling ----
        scrollPane.setFitToWidth(true);    // wrapper uses full scroll width (table stays centered due to maxWidth)
        scrollPane.setFitToHeight(true);
        scrollPane.setPannable(true);
        scrollPane.setStyle(
                "-fx-background: transparent;" +
                        "-fx-background-color: transparent;" +
                        "-fx-padding: 0;"
        );

        scrollPane.setContent(wrapper);
    }


    private void setupTable() {
        tableView = new TableView<>();

        typeCol = new TableColumn<>("Type");
        detailsCol = new TableColumn<>("Details");
        dateCol = new TableColumn<>("Date");

        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        detailsCol.setCellValueFactory(new PropertyValueFactory<>("details"));
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));


        typeCol.setPrefWidth(120);
        detailsCol.setPrefWidth(450);
        dateCol.setPrefWidth(180);

        tableView.getColumns().addAll(typeCol, detailsCol, dateCol);
        VBox.setVgrow(tableView, Priority.ALWAYS);
    }

}


















