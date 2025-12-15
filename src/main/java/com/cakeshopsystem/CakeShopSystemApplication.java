package com.cakeshopsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class CakeShopSystemApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(CakeShopSystemApplication.class.getResource("/views/LoginForm.fxml"));
        Scene scene = new Scene(loader.load());
        String css = Objects.requireNonNull(getClass().getResource("/assets/css/style.css")).toExternalForm();
        scene.getStylesheets().add(css);
        primaryStage.setTitle("Cake Shop System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}