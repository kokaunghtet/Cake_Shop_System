package com.cakeshopsystem.utils.constants;

public enum SnackBarType {
    INFO("snack-info"),
    SUCCESS("snack-success"),
    WARNING("snack-warning"),
    ERROR("snack-error");

    private final String styleClass;

    SnackBarType(String styleClass) {
        this.styleClass = styleClass;
    }

    public String getStyleClass() {
        return styleClass;
    }
}
