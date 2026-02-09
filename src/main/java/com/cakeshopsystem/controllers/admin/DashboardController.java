package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.controllers.ProductCardController;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.cache.UserCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.DashboardDAO;
import com.cakeshopsystem.utils.dao.ProductDAO;
import com.cakeshopsystem.utils.databaseconnection.DB;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import javafx.beans.property.*;
import javafx.collections.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;


public class DashboardController {

    // =====================================
    // FXML UI COMPONENTS
    // =====================================
    @FXML
    private HBox hbStaffCard;
    @FXML
    private Label lblTotalMembers;
    @FXML
    private Label lblTotalOrders;
    @FXML
    private Label lblTotalSales;
    @FXML
    private Label lblTotalCancel;
    @FXML
    private ComboBox<CategoryOption> cbProductFilter;
    @FXML
    private ComboBox<String> cbPeriodFilter;
    @FXML
    private DatePicker dpDateFilter;
    @FXML
    private Button btnRefresh;

    @FXML
    private BarChart<String, Number> bcProduct;
    @FXML
    private PieChart pcProduct;
    @FXML
    private LineChart<String, Number> lcRevenue;

    @FXML
    private HBox hbTopSalesProduct;

    @FXML
    private VBox vbStaffDetails;
    @FXML
    private Label lblStaffDetailsTitle;
    @FXML
    private Button btnCloseStaffDetails;

    @FXML
    private TableView<StaffOrderRow> tblStaffDetails;
    @FXML
    private TableColumn<StaffOrderRow, Integer> colOrderId;
    @FXML
    private TableColumn<StaffOrderRow, String> colOrderDate;
    @FXML
    private TableColumn<StaffOrderRow, Integer> colItems;
    @FXML
    private TableColumn<StaffOrderRow, String> colTotal;

    // =====================================
    // STATE & CONSTANTS
    // =====================================
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private Task<DashboardData> runningTask;
    private static final int TOP_N = 8;
    private boolean isInitializing = true;

    // =====================================
    // DATA MODELS & RECORDS
    // =====================================
    public static class StaffOrderRow {
        private final IntegerProperty orderId = new SimpleIntegerProperty();
        private final StringProperty orderDate = new SimpleStringProperty();
        private final IntegerProperty items = new SimpleIntegerProperty();
        private final StringProperty total = new SimpleStringProperty();

        public StaffOrderRow(int orderId, String orderDate, int items, String total) {
            this.orderId.set(orderId);
            this.orderDate.set(orderDate);
            this.items.set(items);
            this.total.set(total);
        }

        public int getOrderId() { return orderId.get(); }
        public String getOrderDate() { return orderDate.get(); }
        public int getItems() { return items.get(); }
        public String getTotal() { return total.get(); }
    }

    public record CategoryOption(Integer categoryId, String label) {
        @Override
        public String toString() { return label; }
    }

    private record DateRange(LocalDate startInclusive, LocalDate endExclusive) {}

    private static class DashboardData {
        final List<DashboardDAO.NameValue> topProducts;
        final long totalQtyAll;
        final List<DashboardDAO.BucketValue> revenueTrend;
        final DashboardDAO.KpiData kpis;
        final List<Product> topCardProducts;

        DashboardData(
                List<DashboardDAO.NameValue> topProducts,
                long totalQtyAll,
                List<DashboardDAO.BucketValue> revenueTrend,
                List<Product> topCardProducts,
                DashboardDAO.KpiData kpis
        ) {
            this.topProducts = topProducts;
            this.totalQtyAll = totalQtyAll;
            this.revenueTrend = revenueTrend;
            this.kpis = kpis;
            this.topCardProducts = topCardProducts;
        }
    }

    private record StaffOrder(User user, int orderCount) {}
    private record DateTimeRange(LocalDateTime startInclusive, LocalDateTime endExclusive) {}

    // =====================================
    // LIFECYCLE / INITIALIZATION
    // =====================================
    @FXML
    private void initialize() {
        tblStaffDetails.setFixedCellSize(SessionManager.TABLE_CELL_SIZE);
        tblStaffDetails.setPlaceholder(new Label("No orders in this period."));
        tblStaffDetails.setMaxWidth(500);

        cbPeriodFilter.setItems(FXCollections.observableArrayList("Daily", "Weekly", "Monthly", "Yearly"));
        cbPeriodFilter.getSelectionModel().select("Monthly");

        dpDateFilter.setValue(LocalDate.now());

        // Listeners (guarded by isInitializing)
        cbPeriodFilter.valueProperty().addListener((obs, o, n) -> {
            if (!isInitializing) refreshDashboard();
        });
        dpDateFilter.valueProperty().addListener((obs, o, n) -> {
            if (!isInitializing) refreshDashboard();
        });
        cbProductFilter.valueProperty().addListener((obs, o, n) -> {
            if (!isInitializing) refreshDashboard();
        });

        btnRefresh.setOnAction(e -> refreshDashboard());

        loadCategoriesAndLatestDate();
        loadCashierCards();

        if (vbStaffDetails != null) {
            vbStaffDetails.setVisible(false);
            vbStaffDetails.setManaged(false);
        }

        if (tblStaffDetails != null) {
            colOrderId.setCellValueFactory(new PropertyValueFactory<>("orderId"));
            colOrderDate.setCellValueFactory(new PropertyValueFactory<>("orderDate"));
            colItems.setCellValueFactory(new PropertyValueFactory<>("items"));
            colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        }

        if (btnCloseStaffDetails != null) {
            btnCloseStaffDetails.setOnAction(e -> hideStaffDetails());
        }
    }

    // =====================================
    // DATA LOADING (ASYNC)
    // =====================================
    private void loadCategoriesAndLatestDate() {
        Task<List<DashboardDAO.IdName>> tCats = new Task<>() {
            @Override
            protected List<DashboardDAO.IdName> call() throws Exception {
                return DashboardDAO.fetchCategories();
            }
        };

        tCats.setOnSucceeded(e -> {
            var items = FXCollections.<CategoryOption>observableArrayList();
            items.add(new CategoryOption(null, "All Categories"));

            for (DashboardDAO.IdName c : tCats.getValue()) {
                items.add(new CategoryOption(c.id(), c.name()));
            }

            cbProductFilter.setItems(items);
            cbProductFilter.getSelectionModel().selectFirst();
            loadLatestOrderDateThenRefresh();
        });

        tCats.setOnFailed(e -> {
            tCats.getException().printStackTrace();
            isInitializing = false;
            refreshDashboard();
        });

        new Thread(tCats, "dashboard-category-loader").start();
    }

    private void loadLatestOrderDateThenRefresh() {
        Task<LocalDate> tDate = new Task<>() {
            @Override
            protected LocalDate call() throws Exception {
                return DashboardDAO.fetchLatestOrderDate();
            }
        };

        tDate.setOnSucceeded(ev -> {
            LocalDate latest = tDate.getValue();
            if (latest != null) dpDateFilter.setValue(latest);
            isInitializing = false;
            refreshDashboard();
        });

        tDate.setOnFailed(ev -> {
            tDate.getException().printStackTrace();
            isInitializing = false;
            refreshDashboard();
        });

        new Thread(tDate, "dashboard-latest-order-date-loader").start();
    }

    private void refreshDashboard() {
        if (runningTask != null && runningTask.isRunning()) {
            runningTask.cancel();
        }

        final String periodSnap = cbPeriodFilter.getValue() == null ? "Monthly" : cbPeriodFilter.getValue();
        final LocalDate dateSnap = dpDateFilter.getValue() == null ? LocalDate.now() : dpDateFilter.getValue();
        final CategoryOption catSnap = cbProductFilter.getValue() == null
                ? new CategoryOption(null, "All Categories")
                : cbProductFilter.getValue();

        final DateRange range = computeRange(dateSnap, periodSnap);
        final LocalDateTime start = range.startInclusive().atStartOfDay();
        final LocalDateTime end = range.endExclusive().atStartOfDay();
        final Integer categoryId = catSnap.categoryId();

        final Integer cakeCategoryId = findCategoryIdByName("Cake");
        final Integer drinkCategoryId = findCategoryIdByName("Drink");
        final Integer bakedCategoryId = findCategoryIdByName("Baked Goods");

        setBusy(true);

        runningTask = new Task<>() {
            @Override
            protected DashboardData call() throws Exception {
                var top = DashboardDAO.fetchTopProductsByQty(start, end, categoryId, TOP_N);
                long totalQty = DashboardDAO.fetchTotalQtyAllProducts(start, end, categoryId);
                var revenue = DashboardDAO.fetchRevenueTrend(start, end, periodSnap, categoryId);
                var kpis = DashboardDAO.fetchKpis(start, end, categoryId);
                List<Product> topCards = fetchTopCardProducts(start, end, categoryId, cakeCategoryId, drinkCategoryId, bakedCategoryId);

                if (isCancelled()) return null;
                return new DashboardData(top, totalQty, revenue, topCards, kpis);
            }
        };

        runningTask.setOnSucceeded(e -> {
            setBusy(false);
            DashboardData data = runningTask.getValue();
            if (data == null) return;

            renderTopMode(data);
            renderTopProductCards(data.topCardProducts);
            renderKpis(data.kpis);
            loadCashierCards();
        });

        runningTask.setOnFailed(e -> {
            setBusy(false);
            runningTask.getException().printStackTrace();
        });

        new Thread(runningTask, "dashboard-data-loader").start();
    }

    // =====================================
    // CHART & KPI RENDERING
    // =====================================
    private void renderKpis(DashboardDAO.KpiData kpis) {
        lblTotalMembers.setText(formatNumber(kpis.members()));
        lblTotalOrders.setText(formatNumber(kpis.orders()));
        lblTotalSales.setText(formatMmk(kpis.sales()));
        lblTotalCancel.setText(formatNumber(kpis.cancels()));
    }

    private void renderTopMode(DashboardData data) {
        bcProduct.getData().clear();
        pcProduct.getData().clear();
        lcRevenue.getData().clear();

        XYChart.Series<String, Number> bar = new XYChart.Series<>();
        bar.setName("Sales Count");

        long topSum = 0;
        for (DashboardDAO.NameValue nv : data.topProducts) {
            topSum += nv.value();
            bar.getData().add(new XYChart.Data<>(nv.name(), nv.value()));
        }
        bcProduct.getData().add(bar);

        for (DashboardDAO.NameValue nv : data.topProducts) {
            pcProduct.getData().add(new PieChart.Data(nv.name(), nv.value()));
        }
        long others = Math.max(0, data.totalQtyAll - topSum);
        if (others > 0) pcProduct.getData().add(new PieChart.Data("Others", others));

        XYChart.Series<String, Number> line = new XYChart.Series<>();
        line.setName("Revenue (MMK)");

        for (DashboardDAO.BucketValue bv : data.revenueTrend) {
            line.getData().add(new XYChart.Data<>(bv.bucket(), bv.value()));
        }
        lcRevenue.getData().add(line);
    }

    private void renderTopProductCards(List<Product> products) {
        hbTopSalesProduct.getChildren().clear();
        if (products == null || products.isEmpty()) return;

        for (Product product : products) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/ProductCard.fxml"));
                Parent card = loader.load();
                ProductCardController controller = loader.getController();
                controller.setProductData(product);
                hbTopSalesProduct.getChildren().add(card);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    // =====================================
    // STAFF (CASHIER) LOGIC
    // =====================================
    public void loadCashierCards() {
        if (hbStaffCard == null) return;
        hbStaffCard.getChildren().clear();

        int cashierRoleId = RoleCache.getRoleIdByName("Cashier");
        ObservableList<User> cashiers = UserCache.getUsersByRole(cashierRoleId);

        if (cashiers == null || cashiers.isEmpty()) {
            SnackBar.show(SnackBarType.ERROR, "Failed", "No Cashier found in cache", Duration.seconds(2));
            return;
        }

        DateTimeRange range = getOrderDateTimeRangeSnapshot();

        Task<List<StaffOrder>> task = new Task<>() {
            @Override
            protected List<StaffOrder> call() throws Exception {
                List<StaffOrder> result = new ArrayList<>();
                final String sql = "SELECT COUNT(*) AS total FROM orders WHERE user_id = ? AND order_date >= ? AND order_date < ?";

                try (var conn = DB.connect(); var ps = conn.prepareStatement(sql)) {
                    ps.setTimestamp(2, java.sql.Timestamp.valueOf(range.startInclusive()));
                    ps.setTimestamp(3, java.sql.Timestamp.valueOf(range.endExclusive()));

                    for (User user : cashiers) {
                        if (isCancelled()) return result;
                        if (user == null || !user.isActive()) continue;
                        ps.setInt(1, user.getUserId());
                        try (var rs = ps.executeQuery()) {
                            int count = 0;
                            if (rs.next()) count = rs.getInt("total");
                            result.add(new StaffOrder(user, count));
                        }
                    }
                }
                return result;
            }
        };

        task.setOnSucceeded(e -> {
            hbStaffCard.getChildren().clear();
            List<StaffOrder> staffOrders = task.getValue();
            if (staffOrders == null || staffOrders.isEmpty()) return;
            for (StaffOrder so : staffOrders) {
                addUserCardToFlow(so.user(), so.orderCount());
            }
        });

        task.setOnFailed(e -> {
            if (task.getException() != null) task.getException().printStackTrace();
            SnackBar.show(SnackBarType.ERROR, "Failed", "Could not load cashier cards", Duration.seconds(2));
        });

        new Thread(task, "dashboard-cashier-cards-loader").start();
    }

    private void loadStaffDetailsTable(User user) {
        if (user == null) return;
        showStaffDetails();
        lblStaffDetailsTitle.setText("Orders by " + user.getUserName());

        LocalDate date = dpDateFilter.getValue() != null ? dpDateFilter.getValue() : LocalDate.now();
        String period = cbPeriodFilter.getValue() != null ? cbPeriodFilter.getValue() : "Monthly";
        DateRange r = computeRange(date, period);
        LocalDateTime start = r.startInclusive().atStartOfDay();
        LocalDateTime end = r.endExclusive().atStartOfDay();

        Task<ObservableList<StaffOrderRow>> t = new Task<>() {
            @Override
            protected ObservableList<StaffOrderRow> call() throws Exception {
                String sql = "SELECT o.order_id, o.order_date, COALESCE(SUM(oi.quantity),0) AS items, o.grand_total " +
                        "FROM orders o LEFT JOIN order_items oi ON oi.order_id = o.order_id " +
                        "WHERE o.user_id = ? AND o.order_date >= ? AND o.order_date < ? " +
                        "GROUP BY o.order_id, o.order_date, o.grand_total ORDER BY o.order_date DESC";

                ObservableList<StaffOrderRow> rows = FXCollections.observableArrayList();
                try (var con = DB.connect(); var ps = con.prepareStatement(sql)) {
                    ps.setInt(1, user.getUserId());
                    ps.setTimestamp(2, Timestamp.valueOf(start));
                    ps.setTimestamp(3, Timestamp.valueOf(end));
                    try (var rs = ps.executeQuery()) {
                        while (rs.next()) {
                            int orderId = rs.getInt("order_id");
                            var ts = rs.getTimestamp("order_date");
                            String dateStr = ts == null ? "" : ts.toLocalDateTime().format(dtf);
                            int items = rs.getInt("items");
                            BigDecimal total = rs.getBigDecimal("grand_total");
                            rows.add(new StaffOrderRow(orderId, dateStr, items, formatMmk(total)));
                        }
                    }
                }
                return rows;
            }
        };

        t.setOnSucceeded(e -> {
            tblStaffDetails.setItems(t.getValue());
            if (t.getValue() == null || t.getValue().isEmpty()) {
                SnackBar.show(SnackBarType.INFO, "No Data", "No orders found for this staff in selected period.", Duration.seconds(2));
            }
        });

        t.setOnFailed(e -> {
            if (t.getException() != null) t.getException().printStackTrace();
            SnackBar.show(SnackBarType.ERROR, "Failed", "Could not load staff details.", Duration.seconds(2));
        });

        new Thread(t, "staff-details-loader").start();
    }

    // =====================================
    // UI UTILITIES & HELPERS
    // =====================================
    private void showStaffDetails() {
        vbStaffDetails.setVisible(true);
        vbStaffDetails.setManaged(true);
    }

    private void hideStaffDetails() {
        vbStaffDetails.setVisible(false);
        vbStaffDetails.setManaged(false);
        tblStaffDetails.getItems().clear();
        lblStaffDetailsTitle.setText("Staff Details");
    }

    private void addUserCardToFlow(User user, int orderCount) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/admin/UserCard.fxml"));
            VBox card = loader.load();
            UserCardController controller = loader.getController();
            controller.setUser(user, orderCount);
            card.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) {
                    e.consume();
                    loadStaffDetailsTable(user);
                }
            });
            hbStaffCard.getChildren().add(card);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setBusy(boolean busy) {
        btnRefresh.setDisable(busy);
        cbPeriodFilter.setDisable(busy);
        cbProductFilter.setDisable(busy);
        dpDateFilter.setDisable(busy);
    }

    private String formatNumber(long n) {
        return java.text.NumberFormat.getInstance().format(n);
    }

    private String formatMmk(java.math.BigDecimal amount) {
        if (amount == null) amount = java.math.BigDecimal.ZERO;
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0");
        return df.format(amount) + " MMK";
    }

    private Integer findCategoryIdByName(String categoryName) {
        if (cbProductFilter.getItems() == null) return null;
        for (CategoryOption opt : cbProductFilter.getItems()) {
            if (opt.categoryId() != null && opt.label() != null && opt.label().equalsIgnoreCase(categoryName)) {
                return opt.categoryId();
            }
        }
        return null;
    }

    private DateTimeRange getOrderDateTimeRangeSnapshot() {
        LocalDate date = dpDateFilter.getValue() != null ? dpDateFilter.getValue() : LocalDate.now();
        String period = cbPeriodFilter.getValue() != null ? cbPeriodFilter.getValue() : "Monthly";
        DateRange r = computeRange(date, period);
        return new DateTimeRange(r.startInclusive().atStartOfDay(), r.endExclusive().atStartOfDay());
    }

    private static DateRange computeRange(LocalDate anchor, String period) {
        return switch (period) {
            case "Daily" -> new DateRange(anchor, anchor.plusDays(1));
            case "Weekly" -> {
                LocalDate start = anchor.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                yield new DateRange(start, start.plusWeeks(1));
            }
            case "Monthly" -> {
                LocalDate start = anchor.withDayOfMonth(1);
                yield new DateRange(start, start.plusMonths(1));
            }
            case "Yearly" -> {
                LocalDate start = anchor.withDayOfYear(1);
                yield new DateRange(start, start.plusYears(1));
            }
            default -> new DateRange(anchor, anchor.plusDays(1));
        };
    }

    private List<Product> fetchTopCardProducts(
            LocalDateTime start,
            LocalDateTime end,
            Integer selectedCategoryId,
            Integer cakeCategoryId,
            Integer drinkCategoryId,
            Integer bakedCategoryId
    ) throws Exception {
        List<Integer> productIds = new ArrayList<>();
        if (selectedCategoryId == null) {
            if (cakeCategoryId != null) productIds.addAll(DashboardDAO.fetchTopProductIdsByQtyInCategory(start, end, cakeCategoryId, 1));
            if (drinkCategoryId != null) productIds.addAll(DashboardDAO.fetchTopProductIdsByQtyInCategory(start, end, drinkCategoryId, 1));
            if (bakedCategoryId != null) productIds.addAll(DashboardDAO.fetchTopProductIdsByQtyInCategory(start, end, bakedCategoryId, 1));
        } else {
            productIds.addAll(DashboardDAO.fetchTopProductIdsByQtyInCategory(start, end, selectedCategoryId, 3));
        }

        List<Product> products = new ArrayList<>();
        for (Integer id : productIds) {
            if (id == null) continue;
            Product p = ProductDAO.getProductById(id);
            if (p != null) products.add(p);
        }
        return products;
    }
}