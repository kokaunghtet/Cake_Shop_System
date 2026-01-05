package com.cakeshopsystem.utils.session;

import com.cakeshopsystem.controllers.MainController;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.ChangeScene;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.cache.UserCache;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SessionManager {
    public static final int PAGINATION_LIMIT = 10;
    public static Stage primaryStage;
    public static User user;
    public static boolean isAdmin;
    public static boolean isDarkModeOn = false;
    public static MainController mainController;
    public static VBox snackBarContainer;

    public SessionManager() {}

    public SessionManager(User user) {
        SessionManager.user = user;
        if(user == null){
            isAdmin = false;
        } else {
            isAdmin = RoleCache.getRolesMap().get(user.getRole()).getRoleName().equalsIgnoreCase("Admin");
        }
    }

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        SessionManager.user = user;
        if (user != null) {
            isAdmin = RoleCache.getRolesMap().get(user.getRole()).getRoleName().equalsIgnoreCase("Admin");
        } else {
            isAdmin = false;
        }
    }

    public static VBox getSnackBarContainer() {
        return snackBarContainer;
    }

    public static void setSnackBarContainer(VBox snackBarContainer) {
        SessionManager.snackBarContainer = snackBarContainer;
    }

    public static Stage getStage() {
        return primaryStage;
    }

    public static void setStage(Stage primaryStage) {
        SessionManager.primaryStage = primaryStage;
    }

    public static void forceLogout() {
        ChangeScene.switchScene("LoginForm.fxml", "Cake Shop System");
        SessionManager.user = null;

        UserCache.clearCache();
    }
}
