package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {

    // ----- Icons -----
    @FXML private FontIcon settingIcon;
    @FXML private FontIcon signoutIcon;
    @FXML private FontIcon moonIcon;

    // ----- Layout containers -----
    @FXML private AnchorPane contentPane;
    @FXML private BorderPane mainBorderPane;
    @FXML private StackPane mainStackPane;

    // ----- Top bar -----
    @FXML private ImageView profileImageView;
    @FXML private Label roleLabel;
    @FXML private HBox topBar;
    @FXML private Label usernameLabel;

    // ----- Navigation links -----
    @FXML private Hyperlink adminDashboardLink;
    @FXML private Hyperlink adminProductLink;
    @FXML private Hyperlink customerOrderLink;
    @FXML private Hyperlink memberLink;
    @FXML private Hyperlink staffProductLink;
    @FXML private Hyperlink staffDashboardLink;
    @FXML private Hyperlink userLink;
    @FXML private Hyperlink bookingLink;

    // Tracks which nav link is currently active for styling
    private Hyperlink activeLink;

    // Use a single background thread for FXML loading (prevents spawning many threads)
    private final ExecutorService fxmlLoaderExecutor =
            Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "fxml-loader");
                t.setDaemon(true); // Daemon so app can exit cleanly
                return t;
            });

    private static final String DEFAULT_VIEW_FXML = "/views/admin/AdminDashboard.fxml";
    private static final String DEFAULT_TITLE = "Cake Shop System";

    @FXML
    public void initialize() {
        // Show logged-in user info on the top bar
        updateUserInfo();

        // Hide nav links depending on role (Admin/Cashier rules)
        configureRoleBasedAccess();

        // Sign out action
        signoutIcon.setOnMouseClicked(e -> handleLogout());

        // Load default center view
        loadView(DEFAULT_VIEW_FXML, DEFAULT_TITLE, adminDashboardLink);

        // navigation links actions
        userLink.setOnMouseClicked(event -> loadUser());
        memberLink.setOnMouseClicked(event -> loadMember());
        adminProductLink.setOnMouseClicked(event -> loadProduct());
    }

    private void handleLogout() {
        SessionManager.forceLogout(); // Clear session state
        SnackBar.show(SnackBarType.SUCCESS, "Logout Successfully", "", Duration.seconds(3));
    }

    /**
     * Updates username and role labels from the current session user.
     */
    private void updateUserInfo() {
        User user = SessionManager.getUser();
        if (user == null) return;

        usernameLabel.setText(user.getUserName()); // Show username

        if (RoleCache.getRolesMap().containsKey(user.getRole())) {
            roleLabel.setText(RoleCache.getRolesMap().get(user.getRole()).getRoleName());
        } else {
            roleLabel.setText("Unknown Role");
        }
    }

    /* =================== CENTER CONTENT LOADING =================== */

    /**
     * Loads an FXML into the center content pane asynchronously and updates the window title.
     */
    private void loadView(String fxmlPath, String title, Hyperlink buttonToActivate) {
        // Activate nav styling immediately (fast feedback)
        setActiveButton(buttonToActivate);

        // Load content in background to avoid UI freezing
        Task<Node> loadTask = new Task<>() {
            @Override
            protected Node call() throws IOException {
                URL url = requireResource(fxmlPath);              // Validate resource exists
                FXMLLoader loader = new FXMLLoader(url);         // Prepare loader
                return loader.load();                            // Load UI tree
            }
        };

        loadTask.setOnSucceeded(e -> {
            // Swap view on FX thread
            Node view = loadTask.getValue();
            setCenterContent(view);

            // Update window title
            if (title != null && !title.isBlank()) {
                Platform.runLater(() -> SessionManager.getStage().setTitle(title));
            }
        });

        loadTask.setOnFailed(e -> {
            // Log error
            if (loadTask.getException() != null) {
                loadTask.getException().printStackTrace();
            }
            SnackBar.show(SnackBarType.ERROR, "Load Failed", "Cannot load view: " + fxmlPath, Duration.seconds(3));
        });

        fxmlLoaderExecutor.submit(loadTask); // Run task on single executor thread
    }

    /**
     * Replaces the center content pane with the given Node and anchors it to all sides.
     */
    private void setCenterContent(Node content) {
        Platform.runLater(() -> {
            contentPane.getChildren().setAll(content); // Replace existing content

            // Anchor the loaded node so it stretches to fill the pane
            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
        });
    }

    /**
     * Sets "active" style on selected nav link.
     */
    private void setActiveButton(Hyperlink btn) {
        if (btn == null) return;

        if (activeLink != null) {
            activeLink.getStyleClass().remove("active"); // Remove old active style
        }
        activeLink = btn;
        activeLink.getStyleClass().add("active");        // Add active style
    }

    /* =================== ROLE BASED ACCESS =================== */
    /**
     * Hides navigation links depending on the current user's role.
     * Uses managed=false to remove layout space as well.
     */
    private void configureRoleBasedAccess() {
        User user = SessionManager.getUser();
        if (user == null) return;

        if (!RoleCache.getRolesMap().containsKey(user.getRole())) return;

        String roleName = RoleCache.getRolesMap().get(user.getRole()).getRoleName();

        if (roleName.equalsIgnoreCase("Admin")) {
            hideLinks(customerOrderLink, staffDashboardLink, staffProductLink, bookingLink);
        } else if (roleName.equalsIgnoreCase("Cashier")) {
            hideLinks(adminDashboardLink, adminProductLink, memberLink, userLink);
        }
    }

    // =================== NAVIGATION ACTIONS ===================
    // ADMIN
    public void loadDashboard() {
        loadView("/views/admin/AdminDashboard.fxml", "Dashboard", adminDashboardLink);
    }
    public void loadUser() {
        loadView("/views/admin/UserView.fxml", "Users", userLink);
    }
    public void loadMember() {
        loadView("/views/admin/MemberView.fxml", "Members", memberLink);
    }
    public void loadProduct() {
        loadView("/views/admin/AdminProduct.fxml", "Products", adminProductLink);
    }
    // CASHIER


    // =================== HELPERS ===================

    /**
     * Hides a list of hyperlinks.
     */
    private void hideLinks(Hyperlink... links) {
        Arrays.stream(links).forEach(this::hideLink);
    }

    /**
     * Hides a hyperlink safely (null-safe).
     */
    private void hideLink(Hyperlink link) {
        if (link == null) return;
        link.setVisible(false);  // Do not show
        link.setManaged(false);  // Do not reserve layout space
    }

    /**
     * Loads a resource and throws a clear error if missing.
     */
    private URL requireResource(String path) {
        return Objects.requireNonNull(
                getClass().getResource(path),
                "Missing resource: " + path
        );
    }
}
