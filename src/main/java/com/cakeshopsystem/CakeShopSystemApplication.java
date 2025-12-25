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
    @Override
    public void start(Stage primaryStage) throws IOException {
        Parent mainContent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/views/LoginForm.fxml")));
        StackPane root = new StackPane(mainContent);

        VBox snackBarContainer = new VBox(10);
        snackBarContainer.setAlignment(Pos.TOP_CENTER);
        snackBarContainer.setPadding(new Insets(10, 0, 0, 0));
        snackBarContainer.setMouseTransparent(true);

        root.getChildren().add(snackBarContainer);

        Scene scene = new Scene(root);

        SessionManager.setStage(primaryStage);
        SessionManager.setSnackBarContainer(snackBarContainer);

        String css = Objects.requireNonNull(getClass().getResource("/assets/css/style.css")).toExternalForm();
        scene.getStylesheets().add(css);

        primaryStage.setTitle("Cake Shop System");
        primaryStage.setScene(scene);

        primaryStage.show();
    }
}