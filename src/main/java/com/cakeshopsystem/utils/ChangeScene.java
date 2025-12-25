package com.cakeshopsystem.utils;

import com.cakeshopsystem.utils.session.SessionManager;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.Objects;

public class ChangeScene {
    public static void switchScene(String fxmlFile, String title) {
        Stage stage = SessionManager.getStage();
        if (stage == null)
            return;

        boolean wasMaximized = stage.isMaximized(); // Save the current maximized state
        System.out.println("Stage was maximized: " + wasMaximized); // Debugging log

        try {
            URL fxmlURL = ChangeScene.class.getResource("/views/" + fxmlFile);
            if (fxmlURL == null)
                throw new IllegalArgumentException("FXML file not found: " + fxmlFile);

            Parent rootNode = FXMLLoader.load(fxmlURL);

            // Wrap root in StackPane with existing snack bar container
            StackPane rootWithSnackBar = new StackPane(rootNode, SessionManager.getSnackBarContainer());

            rootWithSnackBar.setOpacity(1);

            Scene scene = new Scene(rootWithSnackBar);

            // Load CSS
            String css = Objects.requireNonNull(ChangeScene.class.getResource("/assets/css/style.css"))
                    .toExternalForm();
            scene.getStylesheets().add(css);

            stage.setScene(scene);
            if (title != null)
                stage.setTitle(title);
            stage.show();

            // Restore maximized state after the stage is shown
            Platform.runLater(() -> {
                if (wasMaximized) {
                    tryManualSizing(stage); // Call the new method for manual sizing
                    stage.setMaximized(true); // Force the stage to maximize
                    System.out.println("Restoring maximized state");
                    System.out.println("Stage is now maximized: " + stage.isMaximized());

                    // Fallback: Try again after a short delay if not maximized
                    if (!stage.isMaximized()) {
                        PauseTransition pause = new PauseTransition(Duration.millis(100));
                        pause.setOnFinished(_ -> {
                            tryManualSizing(stage); // Try manual sizing again
                            stage.setMaximized(true);
                            System.out.println("Fallback: Restoring maximized state again");
                            System.out.println("Stage is now maximized after fallback: " + stage.isMaximized());
                        });
                        pause.play();
                    }
                }
            });

        } catch (Exception e) {
            System.err.println("Failed to switch scene to " + fxmlFile + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // New method for manual sizing (optional workaround)
    private static void tryManualSizing(Stage stage) {
        try {
            Screen screen = Screen.getPrimary(); // Get the primary screen
            double screenWidth = screen.getVisualBounds().getWidth();
            double screenHeight = screen.getVisualBounds().getHeight();

            // Set the stage size to the screen size (or a percentage for safety)
            stage.setWidth(screenWidth); // Use 90% to avoid overfilling
            stage.setHeight(screenHeight);
            stage.setX(screen.getVisualBounds().getMinX()); // Position at top-left
            stage.setY(screen.getVisualBounds().getMinY());
            System.out
                    .println("Manual sizing applied: Width = " + stage.getWidth() + ", Height = " + stage.getHeight());
        } catch (Exception e) {
            System.err.println("Failed to apply manual sizing: " + e.getMessage());
        }
    }
}
