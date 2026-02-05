
package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.utils.dao.OrderDAO;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;

import java.net.URL;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.scene.layout.HBox;

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

    @FXML
    void clickRefreshChartBtn(ActionEvent event) {
        refreshChart();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        filterCategory.getItems().setAll("All", "Cake", "Drink", "Accessory");
        filterCategory.setValue("All");

        filterDate.getItems().setAll("Daily", "Weekly", "Monthly", "Yearly");
        filterDate.setValue("Daily");

        xAxis.setTickLabelRotation(-45);
        revenueLineChart.setAnimated(false);
        revenueLineChart.setCreateSymbols(true); // set false if you want no dots

        customDatePicker.setValue(LocalDate.now());

        // Auto refresh on changes
        filterDate.valueProperty().addListener((obs, o, n) -> refreshChart());
        filterCategory.valueProperty().addListener((obs, o, n) -> refreshChart());
        customDatePicker.valueProperty().addListener((obs, o, n) -> refreshChart());
        refreshBtn.setOnAction(e -> refreshChart());

        refreshChart();
    }

    private void refreshChart() {
        String period = safe(filterDate.getValue(), "Daily");
        String category = safe(filterCategory.getValue(), "All");
        LocalDate selectedDate = (customDatePicker.getValue() != null) ? customDatePicker.getValue() : LocalDate.now();

        // Choose date range (history view like dashboard)
        LocalDate startDate;
        LocalDate endDate = selectedDate;

        switch (period) {
            case "Daily" -> startDate = selectedDate;
            case "Weekly" -> startDate = selectedDate.minusWeeks(6).with(DayOfWeek.MONDAY);
            case "Monthly" -> startDate = YearMonth.from(selectedDate.minusMonths(11)).atDay(1);
            case "Yearly" -> startDate = LocalDate.of(selectedDate.getYear() - 3, 1, 1);
            default -> startDate = selectedDate.minusDays(10);
        }

        LinkedHashMap<String, String> labels = generateLabels(period, startDate, endDate);
        loadRevenueDataAsync(period, category, startDate, endDate, labels);
    }

    private LinkedHashMap<String, String> generateLabels(String period, LocalDate startDate, LocalDate endDate) {
        LinkedHashMap<String, String> labels = new LinkedHashMap<>();

        switch (period) {
            case "Daily" -> {
                LocalDate d = startDate;
                while (!d.isAfter(endDate)) {
                    String key = d.toString(); // yyyy-MM-dd
                    labels.put(key, key);
                    d = d.plusDays(1);
                }
            }
            case "Weekly" -> {
                LocalDate d = startDate; // assumed Monday
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
                    String key = ym.toString(); // yyyy-MM
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

        // holders for background -> UI
        final XYChart.Series<String, Number>[] seriesHolder = new XYChart.Series[1];
        final int[] membersHolder = new int[1];
        final int[] ordersHolder = new int[1];
        final double[] salesHolder = new double[1];
        final int[] cancelHolder = new int[1];

        activeTask = new Task<>() {
            @Override
            protected Void call() {
                if (isCancelled()) return null;

                // bucket map (x-axis labels -> revenue)
                LinkedHashMap<String, Double> bucketRevenue = new LinkedHashMap<>();
                labels.keySet().forEach(k -> bucketRevenue.put(k, 0.0));

                Integer categoryId = mapCategoryToId(category);

                // fetch daily revenue points from DAO
                LinkedHashMap<LocalDate, Double> dailyRevenue =
                        OrderDAO.getRevenueByDateRange(startDate, endDate, categoryId);

                // bucket daily revenue into period buckets
                for (Map.Entry<LocalDate, Double> e : dailyRevenue.entrySet()) {
                    if (isCancelled()) return null;

                    String bucketKey = toBucketKey(period, e.getKey());
                    if (bucketRevenue.containsKey(bucketKey)) {
                        bucketRevenue.put(bucketKey, bucketRevenue.get(bucketKey) + e.getValue());
                    }
                }

                // build series
                XYChart.Series<String, Number> s = new XYChart.Series<>();
                s.setName("Revenue (" + category + ")");
                for (Map.Entry<String, Double> e : bucketRevenue.entrySet()) {
                    s.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
                }
                seriesHolder[0] = s;

                // cards
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
            case "Daily" -> d.toString(); // yyyy-MM-dd
            case "Weekly" -> {
                int week = d.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                int year = d.get(IsoFields.WEEK_BASED_YEAR);
                yield String.format("%d-W%02d", year, week);
            }
            case "Monthly" -> YearMonth.from(d).toString(); // yyyy-MM
            case "Yearly" -> String.valueOf(d.getYear());
            default -> d.toString();
        };
    }

    private String safe(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }
}
