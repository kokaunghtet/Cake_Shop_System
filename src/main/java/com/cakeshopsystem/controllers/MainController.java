package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Arrays;

public class MainController {
    @FXML
    private FontIcon settingIcon;

    @FXML
    private FontIcon signoutIcon;

    @FXML
    private FontIcon moonIcon;

    @FXML
    private AnchorPane centerAnchorPane;

    @FXML
    private BorderPane mainBorderPane;

    @FXML
    private StackPane mainStackPane;
    
    @FXML
    private ImageView profileImageView;

    @FXML
    private Label roleLabel;

    @FXML
    private HBox topBar;

    @FXML
    private Label usenameLabel;

    @FXML
    private Hyperlink adminDashboardLink, adminProductLink, customerOrderLink, memberLink, staffProductLink, staffDashboardLink, userLink, bookingLink;

    @FXML
    public void initialize() {
        configureRoleBasedAccess();

        signoutIcon.setOnMouseClicked(event -> {
            SessionManager.forceLogout();
            SnackBar.show(
                    SnackBarType.SUCCESS,
                    "Logout Successfully",
                    "",
                    Duration.seconds(3)
            );
        });
    }

    private void configureRoleBasedAccess() {
        User user = SessionManager.getUser();
        if (user != null && RoleCache.getRolesMap().containsKey(user.getRole())) {
            String role = RoleCache.getRolesMap().get(user.getRole()).getRoleName();
            if (role.equalsIgnoreCase("Admin")) {
                hideLinks(customerOrderLink, staffDashboardLink, staffProductLink, bookingLink);
            } else if (role.equalsIgnoreCase("Cashier")) {
                hideLinks(adminDashboardLink, adminProductLink, memberLink, userLink);
            }
        }
    }

    private void hideLinks(Hyperlink... links) {
        Arrays.stream(links).forEach(this::hideLink);
    }

    private void hideLink(Hyperlink link) {
        if (link == null) return;
        link.setVisible(false);
        link.setManaged(false);
    }

}
