package com.cakeshopsystem.utils.components;

import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class SnackBar {
    private static final int WIDTH = 300;
    private static final Duration SLIDE_DURATION = Duration.millis(200);
    private static final List<VBox> activeSnackBars = new ArrayList<>();

    public static void show(SnackBarType type, String title, String message, Duration duration) {
        VBox container = SessionManager.getSnackBarContainer();
        if(container == null) {
            return;
        }
        container.setPickOnBounds(false);
        container.setMouseTransparent(false);

        // --- SnackBar Root ---
        VBox root = new VBox();
        root.setPadding(new Insets(10));
        root.setMinWidth(WIDTH);
        root.setMaxWidth(400);
        root.getStyleClass().addAll("snack-bar", type.getStyleClass());

        // --- Title Label ---
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("snack-title");
        titleLabel.setWrapText(true);

        // --- Message Label ---
        Label messageLabel = null;
        if (message != null && !message.isBlank()) {
            messageLabel = new Label(message);
            messageLabel.getStyleClass().add("snack-message");
            messageLabel.setWrapText(true);
        }

        // --- Text VBox (Title + Optional Message) ---
        VBox textBox;
        if (messageLabel != null) {
            textBox = new VBox(5, titleLabel, messageLabel);
        } else {
            textBox = new VBox(titleLabel);
        }
        textBox.setAlignment(Pos.CENTER_LEFT);

        // --- Close Button ---
        Button closeButton = new Button("x");
        closeButton.getStyleClass().add("snack-close");
        closeButton.setFocusTraversable(false);
        closeButton.setOnAction(e -> dismiss(root));

        // --- Push Close Button to the Right ---
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // --- HBox Container: [Text Left][Spacer][Close Button Right]
        HBox content = new HBox(10, textBox, spacer, closeButton);
        content.setAlignment(Pos.CENTER_LEFT);

        // If message is blank, only add titleLabel
        if (messageLabel == null || message.isBlank()) {
            root.setAlignment(Pos.CENTER);
            content.setAlignment(Pos.CENTER);
        } else {
            root.setAlignment(Pos.CENTER_LEFT);
        }

        // --- Add Active SnackBar to List
        root.getChildren().add(content);
        container.getChildren().addFirst(root);
        activeSnackBars.add(root);

        // --- Slide-In Animation ---
        double startY = -root.getHeight();
        root.setTranslateY(startY);
        root.setOpacity(0);
        Timeline slideIn = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(root.translateYProperty(), 0),
                        new KeyValue(root.opacityProperty(), 0)),
                new KeyFrame(SLIDE_DURATION,
                        new KeyValue(root.translateYProperty(), startY),
                        new KeyValue(root.opacityProperty(), 1))
        );
        slideIn.play();

        // Automatic Dismiss after Duration
        if (duration != null && !duration.isIndefinite()) {
            Timeline delay = new Timeline(new KeyFrame(duration, e -> dismiss(root)));
            delay.play();
        }
    }

    private static void dismiss(VBox root) {
        VBox container = SessionManager.getSnackBarContainer();
        double startY = -root.getHeight();

        root.setMinHeight(root.getHeight());
        root.setMinWidth(root.getWidth());

        Timeline slideOut = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(root.translateYProperty(), 0),
                        new KeyValue(root.opacityProperty(), 1)),
                new KeyFrame(SLIDE_DURATION,
                        new KeyValue(root.translateYProperty(), startY),
                        new KeyValue(root.opacityProperty(), 0))
        );

        slideOut.setOnFinished(e -> {
            if (container != null) container.getChildren().remove(root);
            activeSnackBars.remove(root);

            root.setMinHeight(Region.USE_COMPUTED_SIZE);
            root.setMinWidth(Region.USE_COMPUTED_SIZE);
        });

        slideOut.play();
    }
}
