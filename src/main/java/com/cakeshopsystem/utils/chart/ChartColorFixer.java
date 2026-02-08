package com.cakeshopsystem.utils.chart;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;

public final class ChartColorFixer {

    private static final String[] COLORS = {
            "#4F46E5", "#22C55E", "#F59E0B", "#EF4444",
            "#06B6D4", "#A855F7", "#84CC16", "#F97316"
    };

    private ChartColorFixer() {}

    public static void fixBarChartColors(BarChart<?, ?> chart) {
        if (chart.getScene() == null) {
            chart.sceneProperty().addListener((obs, oldS, newS) -> {
                if (newS != null) Platform.runLater(() -> fixBarChartColors(chart));
            });
            return;
        }

        chart.applyCss();
        chart.layout();

        int i = 0;
        int styled = 0;
        for (XYChart.Series<?, ?> series : chart.getData()) {
            for (XYChart.Data<?, ?> data : series.getData()) {
                Node node = data.getNode();
                if (node != null) {
                    node.setStyle("-fx-bar-fill: " + COLORS[i % COLORS.length] + ";");
                    i++;
                    styled++;
                }
            }
        }
        // Nodes are created lazily; retry once if none existed yet
        if (styled == 0 && !chart.getData().isEmpty()) {
            Platform.runLater(() -> fixBarChartColors(chart));
        }
    }

    public static void fixPieChartColors(PieChart chart) {
        if (chart.getScene() == null) {
            chart.sceneProperty().addListener((obs, oldS, newS) -> {
                if (newS != null) Platform.runLater(() -> fixPieChartColors(chart));
            });
            return;
        }

        chart.applyCss();
        chart.layout();

        int i = 0;
        int styled = 0;
        for (PieChart.Data data : chart.getData()) {
            Node node = data.getNode();
            if (node != null) {
                node.setStyle("-fx-pie-color: " + COLORS[i % COLORS.length] + ";");
                i++;
                styled++;
            }
        }
        // Nodes are created lazily; retry once if none existed yet
        if (styled == 0 && !chart.getData().isEmpty()) {
            Platform.runLater(() -> fixPieChartColors(chart));
        }
    }
}
