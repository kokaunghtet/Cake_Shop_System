package com.cakeshopsystem.utils.session;

import com.cakeshopsystem.controllers.MainController;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.ChangeScene;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.cache.UserCache;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SessionManager {

    // =========================
    // App Constants
    // =========================
    public static final int PAGINATION_LIMIT = 10;

    // =========================
    // Primary UI References
    // =========================
    public static Stage primaryStage;
    public static MainController mainController;
    public static VBox snackBarContainer;

    // =========================
    // Current Session State
    // =========================
    public static User user;
    public static boolean isAdmin;
    public static boolean isDarkModeOn = false;

    // Utility class (prevent instantiation)
    private SessionManager() {}

    // =========================
    // Session Setup / Update
    // =========================
    public static void setUser(User user) {
        SessionManager.user = user;
        updateAdminFlag();
    }

    public static User getUser() {
        return user;
    }

    private static void updateAdminFlag() {
        if (user == null) {
            isAdmin = false;
            return;
        }
        isAdmin = RoleCache.getRolesMap()
                .get(user.getRole())
                .getRoleName()
                .equalsIgnoreCase("Admin");
    }

    // =========================
    // UI Containers / Stage
    // =========================
    public static Stage getStage() {
        return primaryStage;
    }

    public static void setStage(Stage stage) {
        SessionManager.primaryStage = stage;
    }

    public static VBox getSnackBarContainer() {
        return snackBarContainer;
    }

    public static void setSnackBarContainer(VBox container) {
        SessionManager.snackBarContainer = container;
    }

    // =========================
    // Session Actions
    // =========================
    public static void forceLogout() {
        ChangeScene.switchScene("LoginForm.fxml", "Cake Shop System | Login Form");
        SessionManager.user = null;
        isAdmin = false;
        UserCache.clearCache();
    }
}
