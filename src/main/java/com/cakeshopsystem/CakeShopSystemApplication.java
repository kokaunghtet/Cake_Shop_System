package com.cakeshopsystem;

import com.cakeshopsystem.utils.session.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CakeShopSystemApplication extends Application {

    // ----- Resource paths -----
    private static final String LOGIN_FXML_PATH = "/views/LoginForm.fxml";       // Main UI entry screen
    private static final String APP_CSS_PATH    = "/assets/css/style.css";       // Global stylesheet
    private static final String APP_TITLE       = "Cake Shop System";            // Window title

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load the first screen (Login form) from FXML.
        Parent mainContent = FXMLLoader.load(
                Objects.requireNonNull(
                        getClass().getResource(LOGIN_FXML_PATH),
                        "Missing FXML resource: " + LOGIN_FXML_PATH
                )
        );

        // Root container for the whole app scene (lets us overlay things like SnackBar on top).
        StackPane root = new StackPane(mainContent);

        // Container that will hold SnackBar notifications (positioned at the top-center).
        VBox snackBarContainer = new VBox(10);            // 10px spacing between snackbars
        snackBarContainer.setAlignment(Pos.TOP_CENTER);          // Top-center placement
        snackBarContainer.setPadding(new Insets(10, 0, 0, 0));   // Margin from the top edge
        snackBarContainer.setMouseTransparent(true);             // Allow clicks to pass through

        // Add SnackBar layer above the main content.
        root.getChildren().add(snackBarContainer);

        // Create the scene using the root StackPane.
        Scene scene = new Scene(root);

        // Store shared UI references for later use in the application.
        SessionManager.setStage(primaryStage);
        SessionManager.setSnackBarContainer(snackBarContainer);

        // Load and apply the app stylesheet.
        String css = Objects.requireNonNull(
                getClass().getResource(APP_CSS_PATH),
                "Missing CSS resource: " + APP_CSS_PATH
        ).toExternalForm();
        scene.getStylesheets().add(css);

        // Configure and show the main window.
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
