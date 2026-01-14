package com.cakeshopsystem.utils.components;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class BreadcrumbBar extends HBox {

    private final ObservableList<BreadcrumbItem> items = FXCollections.observableArrayList();

    public BreadcrumbBar() {
        getStyleClass().add("breadcrumbs");
        setSpacing(8);
    }

    public ObservableList<BreadcrumbItem> getItems() {
        return items;
    }

    public void bindToGlobal() {
        items.setAll(BreadcrumbManager.getBreadcrumbs());
        // Listen to global breadcrumbs
        BreadcrumbManager.getBreadcrumbs().addListener((javafx.collections.ListChangeListener<BreadcrumbItem>) change -> {
            items.setAll(BreadcrumbManager.getBreadcrumbs());
            rebuild();
        });
        rebuild();
    }

    private void rebuild() {
        getChildren().clear();

        for (int i = 0; i < items.size(); i++) {
            BreadcrumbItem item = items.get(i);

            // Separator
            if (i > 0) {
                Label separator = new Label(">");
                separator.getStyleClass().add("breadcrumb-separator");
                getChildren().add(separator);
            }

            Hyperlink link = new Hyperlink(item.getText());
            link.getStyleClass().add("breadcrumb-link");

            int currentIndex = i; // Capture the index for use in lambda
            link.setOnAction(event -> {
                System.out.println("Clicked index: " + currentIndex);

                // Trim breadcrumbs to the clicked index
                BreadcrumbManager.navigateTo(currentIndex);

                // Execute original action
                if (item.getOnAction() != null) {
                    item.getOnAction().handle(event);
                }
            });

            // Disable last item (active page)
            if (i == items.size() - 1) {
                link.setDisable(true);
                link.getStyleClass().add("breadcrumb-active");
            }

            getChildren().add(link);
        }
    }


    public static class BreadcrumbItem {
        private final StringProperty text = new SimpleStringProperty();
        private final javafx.event.EventHandler<javafx.event.ActionEvent> onAction;

        public BreadcrumbItem(String text, javafx.event.EventHandler<javafx.event.ActionEvent> onAction) {
            this.text.set(text);
            this.onAction = onAction;
        }

        public String getText() {
            return text.get();
        }

        public javafx.event.EventHandler<javafx.event.ActionEvent> getOnAction() {
            return onAction;
        }
    }
}

