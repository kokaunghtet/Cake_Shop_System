package com.cakeshopsystem.utils.components;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class BreadcrumbManager {

    private static final ObservableList<BreadcrumbBar.BreadcrumbItem> breadcrumbs =
            FXCollections.observableArrayList();

    private BreadcrumbManager() {}

    public static ObservableList<BreadcrumbBar.BreadcrumbItem> getBreadcrumbs() {
        return breadcrumbs;
    }

    public static void setBreadcrumbs(BreadcrumbBar.BreadcrumbItem... items) {
        breadcrumbs.setAll(items);
    }

    public static void addBreadcrumb(BreadcrumbBar.BreadcrumbItem item) {
        breadcrumbs.add(item);
    }

    public static void navigateTo(int index) {
        if (index >= 0 && index < breadcrumbs.size()) {
            breadcrumbs.remove(index + 1, breadcrumbs.size());
        }
    }
}

