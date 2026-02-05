package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.ActivityItem;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class StaffPerformanceDAO {

    public static ObservableList<ActivityItem> getActivities(
            int staffId,
            String dateFilter
    ) {
        ObservableList<ActivityItem> list = FXCollections.observableArrayList();

        String sql = """
            SELECT type, details, activity_date
            FROM staff_performance_view
            WHERE staff_id = ?
              AND %s
            ORDER BY activity_date DESC
        """.formatted(dateFilter);

        try (var conn = DB.connect();
             var ps = conn.prepareStatement(sql)) {

            ps.setInt(1, staffId);

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ActivityItem(
                            rs.getString("type"),
                            rs.getString("details"),
                            rs.getString("activity_date") // ✅ STRING
                    ));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
