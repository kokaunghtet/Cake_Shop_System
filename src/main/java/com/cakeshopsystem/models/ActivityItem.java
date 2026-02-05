package com.cakeshopsystem.models;

import javafx.beans.property.SimpleStringProperty;

public class ActivityItem {
    private final SimpleStringProperty type;
    private final SimpleStringProperty details;
    private final SimpleStringProperty date;

    public ActivityItem(String type, String details, String date) {
        this.type = new SimpleStringProperty(type);
        this.details = new SimpleStringProperty(details);
        this.date = new SimpleStringProperty(date);
    }

    public String getType() { return type.get(); }
    public String getDetails() { return details.get(); }
    public String getDate() { return date.get(); }
}


