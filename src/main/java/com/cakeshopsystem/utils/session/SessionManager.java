package com.cakeshopsystem.utils.session;

import com.cakeshopsystem.models.User;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SessionManager {
    public static Stage primaryStage;
    public static User user;
    public static boolean isAdmin;
    public static VBox snackBarContainer;

    public static VBox getSnackBarContainer() {
        return snackBarContainer;
    }

    public static void setSnackBarContainer(VBox snackBarContainer) {
        SessionManager.snackBarContainer = snackBarContainer;
    }
}
