package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.utils.dao.OrderDAO;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

public class RevenueController implements Initializable {

    @FXML private DatePicker customDatePicker;
    @FXML private ComboBox<String> filterCategory;
    @FXML private ComboBox<String> filterDate;

    @FXML private Label lblTotalMembersResult;
    @FXML private Label lblTotalOrdersResult;
    @FXML private Label lblTotalSalesResult;
    @FXML private Label lblTotalCancelResult;

    @FXML private Button refreshBtn;

    @FXML public CategoryAxis xAxis;
    @FXML public NumberAxis yAxis;
    @FXML private LineChart<String, Number> revenueLineChart;

    private volatile Task<Void> activeTask;

    // ---- Range picker state (single DatePicker) ----
    private LocalDate startDate;
    private LocalDate endDate;
    private final Set<LocalDate> selectedRange = new HashSet<>();
    private final java.time.format.DateTimeFormatter formatter =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        // -----------------
        // Default filters
        // -----------------
        filterCategory.getItems().setAll("All", "Cake", "Drink", "Accessory");
        filterDate.getItems().setAll("Daily", "Weekly", "Monthly", "Yearly");

        xAxis.setTickLabelRotation(-45);
        revenueLineChart.setAnimated(false);
        revenueLineChart.setCreateSymbols(true);

        // Enable range selection using ONE DatePicker
        configureDatePicker();

        // Set UI defaults + load
        resetFiltersToDefault();
        refreshChart();

        // Auto refresh on combobox changes (date range handled by clicks)
        filterDate.valueProperty().addListener((obs, o, n) -> refreshChart());
        filterCategory.valueProperty().addListener((obs, o, n) -> refreshChart());

        // Refresh button resets everything then reloads
        refreshBtn.setOnAction(e -> clickRefreshChartBtn(null));
    }

    // =========================================
    // Range DatePicker (1 control, 2 clicks)
    // =========================================
    private void configureDatePicker() {
        customDatePicker.setEditable(false);

        // IMPORTANT:
        // converter must return your range text (NOT ""), or JavaFX will blank the editor
        customDatePicker.setConverter(new StringConverter<>() {
            @Override
            public String toString(LocalDate date) {
                if (startDate == null) return "";
                if (endDate == null) return formatter.format(startDate);
                return formatter.format(startDate) + " → " + formatter.format(endDate);
            }

            @Override
            public LocalDate fromString(String s) {
                return null; // no typing
            }
        });

        customDatePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);

                // reset (cells reused!)
                setStyle("");
                setOnMouseClicked(null);

                if (empty || item == null) return;

                // highlight range
                if (selectedRange.contains(item)) {
                    setStyle("-fx-background-color: rgba(76,175,80,0.25);");
                }

                // highlight start / end stronger
                if (startDate != null && item.equals(startDate)) {
                    setStyle("-fx-background-color: #4caf50; -fx-text-fill: white; -fx-font-weight: bold;");
                }
                if (endDate != null && item.equals(endDate)) {
                    setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-font-weight: bold;");
                }

                final LocalDate clicked = item;
                setOnMouseClicked(e -> {
                    if (isDisable()) return;
                    handleDateSelection(clicked);
                    e.consume();
                });
            }
        });
    }

    private void handleDateSelection(LocalDate clickedDate) {
        // Keep the calendar focused on clicked month/day (good UX)
        customDatePicker.setValue(clickedDate);

        // FIRST click (or restart selection)
        if (startDate == null || endDate != null) {
            startDate = clickedDate;
            endDate = null;

            selectedRange.clear();
            selectedRange.add(startDate);

            // reopen so user can pick end date
            Platform.runLater(customDatePicker::show);

            // ❌ DO NOT refresh chart here (otherwise you query twice)
            return;
        }

        // SECOND click (set end)
        if (clickedDate.isBefore(startDate)) {
            endDate = startDate;
            startDate = clickedDate;
        } else {
            endDate = clickedDate;
        }

        fillDateRange();

        // now close after full range chosen
        customDatePicker.hide();

        // ✅ refresh only after full range is selected
        refreshChart();
    }

    private void fillDateRange() {
        selectedRange.clear();
        if (startDate == null) return;

        LocalDate end = (endDate != null) ? endDate : startDate;

        LocalDate d = startDate;
        while (!d.isAfter(end)) {
            selectedRange.add(d);
            d = d.plusDays(1);
        }
    }

    // =========================================
    // Refresh button: reset + reload
    // =========================================
    private void resetFiltersToDefault() {
        // reset combos
        filterCategory.setValue("All");
        filterDate.setValue("Daily");

        // reset range to today (single-day range)
        startDate = LocalDate.now();
        endDate = null;
        selectedRange.clear();
        selectedRange.add(startDate);

        // keep calendar anchored
        customDatePicker.setValue(startDate);

        // force editor to update now (sometimes needed)
        Platform.runLater(() -> {
            customDatePicker.getEditor().setText(customDatePicker.getConverter().toString(startDate));
        });
    }

    @FXML
    void clickRefreshChartBtn(ActionEvent event) {
        resetFiltersToDefault();
        refreshChart();
    }

    // =========================================
    // Chart logic
    // =========================================
    private void refreshChart() {
        String period = safe(filterDate.getValue(), "Daily");
        String category = safe(filterCategory.getValue(), "All");

        // use selected range
        if (startDate == null) {
            startDate = LocalDate.now();
            endDate = null;
            selectedRange.clear();
            selectedRange.add(startDate);
        }

        LocalDate start = startDate;
        LocalDate end = (endDate != null) ? endDate : startDate;

        if (end.isBefore(start)) {
            LocalDate tmp = start;
            start = end;
            end = tmp;
        }

        LinkedHashMap<String, String> labels = generateLabels(period, start, end);
        loadRevenueDataAsync(period, category, start, end, labels);
    }

    private LinkedHashMap<String, String> generateLabels(String period, LocalDate startDate, LocalDate endDate) {
        LinkedHashMap<String, String> labels = new LinkedHashMap<>();

        switch (period) {
            case "Daily" -> {
                LocalDate d = startDate;
                while (!d.isAfter(endDate)) {
                    String key = d.toString();
                    labels.put(key, key);
                    d = d.plusDays(1);
                }
            }
            case "Weekly" -> {
                LocalDate d = startDate.with(DayOfWeek.MONDAY);
                while (!d.isAfter(endDate)) {
                    int week = d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    int year = d.get(IsoFields.WEEK_BASED_YEAR);
                    String key = String.format("%d-W%02d", year, week);
                    labels.put(key, key);
                    d = d.plusWeeks(1);
                }
            }
            case "Monthly" -> {
                YearMonth ym = YearMonth.from(startDate);
                YearMonth endYm = YearMonth.from(endDate);
                while (!ym.isAfter(endYm)) {
                    String key = ym.toString();
                    labels.put(key, key);
                    ym = ym.plusMonths(1);
                }
            }
            case "Yearly" -> {
                int y = startDate.getYear();
                int endY = endDate.getYear();
                while (y <= endY) {
                    String key = String.valueOf(y);
                    labels.put(key, key);
                    y++;
                }
            }
            default -> {
                LocalDate d = startDate;
                while (!d.isAfter(endDate)) {
                    String key = d.toString();
                    labels.put(key, key);
                    d = d.plusDays(1);
                }
            }
        }

        return labels;
    }

    private void loadRevenueDataAsync(String period,
                                      String category,
                                      LocalDate startDate,
                                      LocalDate endDate,
                                      LinkedHashMap<String, String> labels) {

        if (activeTask != null) {
            activeTask.cancel();
        }

        final XYChart.Series<String, Number>[] seriesHolder = new XYChart.Series[1];
        final int[] membersHolder = new int[1];
        final int[] ordersHolder = new int[1];
        final double[] salesHolder = new double[1];
        final int[] cancelHolder = new int[1];

        activeTask = new Task<>() {
            @Override
            protected Void call() {
                if (isCancelled()) return null;

                LinkedHashMap<String, Double> bucketRevenue = new LinkedHashMap<>();
                labels.keySet().forEach(k -> bucketRevenue.put(k, 0.0));

                Integer categoryId = mapCategoryToId(category);

                LinkedHashMap<LocalDate, Double> dailyRevenue =
                        OrderDAO.getRevenueByDateRange(startDate, endDate, categoryId);

                for (Map.Entry<LocalDate, Double> e : dailyRevenue.entrySet()) {
                    if (isCancelled()) return null;

                    String bucketKey = toBucketKey(period, e.getKey());
                    if (bucketRevenue.containsKey(bucketKey)) {
                        bucketRevenue.put(bucketKey, bucketRevenue.get(bucketKey) + e.getValue());
                    }
                }

                XYChart.Series<String, Number> s = new XYChart.Series<>();
                s.setName("Revenue (" + category + ")");
                for (Map.Entry<String, Double> e : bucketRevenue.entrySet()) {
                    s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
                }
                seriesHolder[0] = s;

                membersHolder[0] = OrderDAO.getTotalMembers();
                ordersHolder[0] = OrderDAO.getTotalOrders(startDate, endDate);
                salesHolder[0] = OrderDAO.getTotalSales(startDate, endDate);
                cancelHolder[0] = OrderDAO.getTotalCancelBookings(startDate, endDate);

                return null;
            }
        };

        activeTask.setOnSucceeded(ev -> {
            lblTotalMembersResult.setText(String.valueOf(membersHolder[0]));
            lblTotalOrdersResult.setText(String.valueOf(ordersHolder[0]));
            lblTotalSalesResult.setText(String.format("%,.0f", salesHolder[0]));
            lblTotalCancelResult.setText(String.valueOf(cancelHolder[0]));

            revenueLineChart.getData().clear();
            if (seriesHolder[0] != null) revenueLineChart.getData().add(seriesHolder[0]);
        });

        activeTask.setOnFailed(ev -> {
            Throwable ex = activeTask.getException();
            System.err.println("Revenue load failed: " + (ex == null ? "unknown error" : ex.getMessage()));
            if (ex != null) ex.printStackTrace();
        });

        Thread th = new Thread(activeTask);
        th.setDaemon(true);
        th.start();
    }

    // ------------------- helpers -------------------
    private Integer mapCategoryToId(String category) {
        if (category == null || category.equalsIgnoreCase("All")) return null;
        return switch (category) {
            case "Cake" -> 1;
            case "Drink" -> 2;
            case "Accessory" -> 4;
            default -> null;
        };
    }

    private String toBucketKey(String period, LocalDate d) {
        return switch (period) {
            case "Daily" -> d.toString();
            case "Weekly" -> {
                int week = d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                int year = d.get(IsoFields.WEEK_BASED_YEAR);
                yield String.format("%d-W%02d", year, week);
            }
            case "Monthly" -> YearMonth.from(d).toString();
            case "Yearly" -> String.valueOf(d.getYear());
            default -> d.toString();
        };
    }

    private String safe(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }
}
