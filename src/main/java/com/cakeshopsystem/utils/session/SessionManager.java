package com.cakeshopsystem.utils.session;

import com.cakeshopsystem.controllers.MainController;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.ChangeScene;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.cache.UserCache;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class SessionManager {

    // =========================
    // App Constants
    // =========================
    public static final int TABLE_CELL_SIZE = 44;
//    public static final int PAGINATION_LIMIT = 10;
    private static final List<Consumer<User>> USER_LISTENERS = new CopyOnWriteArrayList<>();

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
    public static double discountRate = 0.5; // 50% discount rate for expired soon products

    // Utility class (prevent instantiation)
    private SessionManager() {}

    // =========================
    // Session Setup / Update
    // =========================
    public static void setUser(User user) {
        SessionManager.user = user;
        updateAdminFlag();

        for (Consumer<User> l : USER_LISTENERS) {
            User snapshot = user;
            Platform.runLater(() -> l.accept(snapshot));
        }
    }

    public static User getUser() {
        return user;
    }

    public static void addUserListener(Consumer<User> listener) {
        if (listener != null) USER_LISTENERS.add(listener);
    }

    public static void removeUserListener(Consumer<User> listener) {
        USER_LISTENERS.remove(listener);
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
