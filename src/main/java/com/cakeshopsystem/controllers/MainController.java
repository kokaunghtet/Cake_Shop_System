package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.ImageHelper;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.components.BreadcrumbBar;
import com.cakeshopsystem.utils.components.BreadcrumbManager;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.Popup;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {

    /* =========================================================
     * Constants / State
     * ========================================================= */

    private static final String DEFAULT_VIEW_FXML = "/views/admin/AdminDashboard.fxml";
    private static final String DEFAULT_TITLE_PREFIX = "Cake Shop System | ";
    private static final double POPUP_CONTENT_WIDTH = 600;

    // Tracks which nav link is currently active for styling
    private Hyperlink activeLink;

    // Popup state
    private static boolean isPopupContentOpen = false;

    // Use a single background thread for FXML loading (prevents spawning many threads)
    private final ExecutorService fxmlLoaderExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "fxml-loader");
        t.setDaemon(true); // Daemon so app can exit cleanly
        return t;
    });

    /* =========================================================
     * FXML: Icons
     * ========================================================= */

    @FXML
    private FontIcon settingIcon;
    @FXML
    private FontIcon signOutIcon;
    @FXML
    private FontIcon moonIcon;

    /* =========================================================
     * FXML: Layout Containers
     * ========================================================= */

    @FXML
    private AnchorPane contentPane;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private StackPane mainStackPane;
    @FXML
    private AnchorPane overlayPane;
    @FXML
    private AnchorPane popupPane;

    /* =========================================================
     * FXML: Top Bar
     * ========================================================= */

    @FXML
    private ImageView profileImageView;
    @FXML
    private Label roleLabel;
    @FXML
    private HBox topBar;
    @FXML
    private Label usernameLabel;

    /* =========================================================
     * FXML: Navigation Links
     * ========================================================= */

    @FXML
    private Hyperlink adminDashboardLink;
    @FXML
    private Hyperlink adminProductLink;
    @FXML
    private Hyperlink customerOrderLink;
    @FXML
    private Hyperlink memberLink;
    @FXML
    private Hyperlink staffProductLink;
    @FXML
    private Hyperlink staffDashboardLink;
    @FXML
    private Hyperlink userLink;
    @FXML
    private Hyperlink bookingLink;

    /* =========================================================
     * FXML: Breadcrumbs
     * ========================================================= */

    @FXML
    private BreadcrumbBar breadcrumbBar;

    /* =========================================================
     * Static References for Popup System
     * ========================================================= */

    // Loading overlay (added to mainStackPane)
    private static StackPane loadingOverlay;

    // Static references to allow popup control from other controllers
    private static BorderPane staticBorderPane;
    private static AnchorPane staticPopupContentPane;
    private static AnchorPane staticOverlayPane;

    private Popup settingsPopup;

    /* =========================================================
     * Lifecycle
     * ========================================================= */

    @FXML
    public void initialize() {
        // Make controller state accessible from static popup helpers
        staticOverlayPane = overlayPane;
        staticBorderPane = mainBorderPane;
        staticPopupContentPane = popupPane;

        staticOverlayPane.setVisible(false);
        staticPopupContentPane.setTranslateX(POPUP_CONTENT_WIDTH);

        updateUserInfo();
        configureRoleBasedAccess();

        if (signOutIcon != null) {
            signOutIcon.setOnMouseClicked(e -> handleLogout());
        }

        // Loading overlay
        loadingOverlay = createLoadingOverlay();
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);

        if (mainStackPane != null && !mainStackPane.getChildren().contains(loadingOverlay)) {
            mainStackPane.getChildren().add(loadingOverlay);
        }

        // Breadcrumb binding
        if (breadcrumbBar != null) {
            breadcrumbBar.bindToGlobal();
        }

//        settingIcon.setOnMouseClicked(e -> setupSettingsPopup());
        if (signOutIcon != null) signOutIcon.setOnMouseClicked(e -> handleLogout());

        // Initialize the popup logic once
        initSettingsPopup();
        settingIcon.setOnMouseClicked(e -> showSettingsPopup());

        // Default navigation
        if (SessionManager.isAdmin) {
            BreadcrumbManager.setBreadcrumbs();
            navigate(DEFAULT_VIEW_FXML, adminDashboardLink, "Dashboard");
        }

        // Navigation handlers
        if (adminDashboardLink != null) adminDashboardLink.setOnMouseClicked(e -> loadAdminDashboard());
        if (userLink != null) userLink.setOnMouseClicked(e -> loadUser());
        if (memberLink != null) memberLink.setOnMouseClicked(e -> loadMember());
        if (adminProductLink != null) adminProductLink.setOnMouseClicked(e -> loadAdminProduct());
    }

    /* =========================================================
     * Settings Popup Optimization
     * ========================================================= */

    /**
     * Build the popup once in memory.
     */
    private void initSettingsPopup() {
        Hyperlink link1 = new Hyperlink("User Configuration");
        Hyperlink link2 = new Hyperlink("Payment Configuration");

        VBox box = new VBox(10, link1, link2);
        box.getStyleClass().add("container"); // Ensure your CSS has this class or use inline style
//        box.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 0);");

        settingsPopup = new Popup();
        settingsPopup.getContent().add(box);
        settingsPopup.setAutoHide(true);

        // Close popup when links are clicked
        link1.setOnAction(e -> settingsPopup.hide());
        link2.setOnAction(e -> settingsPopup.hide());
    }

    /**
     * Show the pre-built popup.
     */
    private void showSettingsPopup() {
        if (settingsPopup == null) return;

        if (settingsPopup.isShowing()) {
            settingsPopup.hide();
            return;
        }

        // Calculate position relative to the icon
        var bounds = settingIcon.localToScreen(settingIcon.getBoundsInLocal());
        if (bounds != null) {
            settingsPopup.show(settingIcon,
                    bounds.getMaxX() - 140, // Adjusted X to align better
                    bounds.getMaxY() + 20
            );
        }
    }

    /* =========================================================
     * User / Session
     * ========================================================= */

    private void handleLogout() {
        SessionManager.forceLogout(); // Clear session state
        SnackBar.show(SnackBarType.SUCCESS, "Logged out successfully", "", Duration.seconds(3));
    }

    /**
     * Updates username and role labels from the current session user.
     */
    private void updateUserInfo() {
        User user = SessionManager.getUser();
        if (user == null) return;

        String path = user.getImagePath();
        if (path == null || path.isBlank()) return;
        Image userProfileImage;
        if (path.matches("^(https?|file|jar):.*")) {
            userProfileImage = new Image(path, true);
        } else {
            userProfileImage = new Image(new java.io.File(path).toURI().toString(), true);
        }
        ImageHelper.setCircularAvatar(profileImageView, userProfileImage, 60);

        if (usernameLabel != null) {
            usernameLabel.setText(user.getUserName());
        }

        if (roleLabel != null && RoleCache.getRolesMap().containsKey(user.getRole())) {
            roleLabel.setText(RoleCache.getRolesMap().get(user.getRole()).getRoleName());
        }
    }

    /* =========================================================
     * Role-Based Access Control
     * ========================================================= */

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

    /* =========================================================
     * Navigation (Public Handlers)
     * ========================================================= */

    private void navigate(String fxmlPath, Hyperlink navLink, String pageName) {
        loadView(fxmlPath, navLink, DEFAULT_TITLE_PREFIX + pageName, pageName);
    }

    // ADMIN
    public void loadAdminDashboard() {
        navigate("/views/admin/AdminDashboard.fxml", adminDashboardLink, "Dashboard");
    }

    public void loadUser() {
        navigate("/views/admin/UserView.fxml", userLink, "Users");
    }

    public void loadMember() {
        navigate("/views/admin/MemberView.fxml", memberLink, "Members");
    }

    public void loadAdminProduct() {
        navigate("/views/ProductView.fxml", adminProductLink, "Products");
    }

    /* =========================================================
     * Breadcrumb + View Loader
     * ========================================================= */

    /**
     * Loads an FXML into the center content pane asynchronously and updates the window title.
     */
    private void loadView(String fxmlPath, Hyperlink navLink, String windowTitle, String breadcrumbTitle) {
        setCenterContent(fxmlPath, windowTitle);
        setActiveLink(navLink);

        BreadcrumbManager.setBreadcrumbs(
                new BreadcrumbBar.BreadcrumbItem(
                        breadcrumbTitle,
                        ev -> loadView(fxmlPath, navLink, windowTitle, breadcrumbTitle)
                )
        );
    }

    private void setCenterContent(String fxmlFile, String title) {
        setCenterContent(fxmlFile);
        if (title != null && !title.isBlank()) {
            Platform.runLater(() -> {
                if (SessionManager.getStage() != null) {
                    SessionManager.getStage().setTitle(title);
                }
            });
        }
    }

    private void setCenterContent(String fxmlPath) {
        Task<Node> loadTask = new Task<>() {
            @Override
            protected Node call() throws IOException {
                URL url = requireResource(fxmlPath);
                FXMLLoader loader = new FXMLLoader(url);
                return loader.load();
            }
        };

        loadTask.setOnRunning(e -> showLoading(true));

        loadTask.setOnSucceeded(e -> {
            Node view = loadTask.getValue();
            setCenterContent(view);
            showLoading(false);
        });

        loadTask.setOnFailed(e -> {
            showLoading(false);
            if (loadTask.getException() != null) loadTask.getException().printStackTrace();
            SnackBar.show(SnackBarType.ERROR, "Load failed", "Cannot load view: " + fxmlPath, Duration.seconds(3));
        });

        fxmlLoaderExecutor.submit(loadTask);
    }

    /**
     * Replaces the center content pane with the given Node and anchors it to all sides.
     */
    private void setCenterContent(Node content) {
        if (contentPane == null) return;

        Platform.runLater(() -> {
            contentPane.getChildren().setAll(content);

            AnchorPane.setTopAnchor(content, 0.0);
            AnchorPane.setBottomAnchor(content, 0.0);
            AnchorPane.setLeftAnchor(content, 0.0);
            AnchorPane.setRightAnchor(content, 0.0);
        });
    }

    /**
     * Sets "active" style on selected nav link.
     */
    private void setActiveLink(Hyperlink link) {
        if (link == null) return;

        if (activeLink != null) {
            activeLink.getStyleClass().remove("active");
        }
        activeLink = link;
        activeLink.getStyleClass().add("active");
    }

    /* =========================================================
     * Popup Content (Static API)
     * ========================================================= */

    public static void togglePopupContent(String fxmlPath) {
        try {
            URL url = Objects.requireNonNull(MainController.class.getResource(fxmlPath), "Missing resource: " + fxmlPath);
            Parent content = FXMLLoader.load(url);
            togglePopupContent(content);
        } catch (IOException e) {
            System.err.println("Error loading popup content: " + fxmlPath);
            e.printStackTrace();
            SnackBar.show(SnackBarType.ERROR, "Load failed", "Cannot load popup: " + fxmlPath, Duration.seconds(3));
        }
    }

    public static void togglePopupContent(Parent content) {
        if (!isPopupContentOpen) {
            loadPopupContent(content);

            Platform.runLater(() -> {
                openPopupContentAnimation();
                applyBlur(true);
                isPopupContentOpen = true;
            });
        } else {
            handleClosePopupContent();
        }
    }

    private static void loadPopupContent(Parent content) {
        // Ensure close button exists once
        Button existingCloseBtn = (Button) staticPopupContentPane.lookup("#closeBtn");
        if (existingCloseBtn == null) {
            Button closeBtn = createCloseButton();
            staticPopupContentPane.getChildren().add(closeBtn);
        }

        setPopupContent(content);
    }

//    private static void setPopupContent(Parent content) {
//        AnchorPane container = new AnchorPane(content);
//        AnchorPane.setTopAnchor(container, 50.0);
//        AnchorPane.setRightAnchor(container, 50.0);
//        AnchorPane.setBottomAnchor(container, 50.0);
//        AnchorPane.setLeftAnchor(container, 50.0);
//
//        container.setOpacity(0);
//        Timeline fadeIn = new Timeline(
//                new KeyFrame(
//                        Duration.millis(200),
//                        new KeyValue(container.opacityProperty(), 1, Interpolator.EASE_BOTH)
//                )
//        );
//
//        // Keep close button (if exists), clear the rest, then add new content
//        Node closeBtn = staticPopupContentPane.lookup("#closeBtn");
//        staticPopupContentPane.getChildren().clear();
//        if (closeBtn != null) staticPopupContentPane.getChildren().add(closeBtn);
//        staticPopupContentPane.getChildren().add(container);
//
//        fadeIn.play();
//    }

    private static void setPopupContent(Parent content) {

        if (content instanceof Region r) {
            r.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        }

        Button closeBtn = (Button) staticPopupContentPane.lookup("#closeBtn");
        if (closeBtn == null) closeBtn = createCloseButton();

        StackPane wrapper = new StackPane();
        wrapper.setPadding(new Insets(30));

        wrapper.getChildren().add(content);
        wrapper.getChildren().add(closeBtn);

        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(closeBtn, new Insets(14));

        AnchorPane.setTopAnchor(wrapper, 0.0);
        AnchorPane.setRightAnchor(wrapper, 0.0);
        AnchorPane.setBottomAnchor(wrapper, 0.0);
        AnchorPane.setLeftAnchor(wrapper, 0.0);

        staticPopupContentPane.getChildren().setAll(wrapper);

        wrapper.setOpacity(0);
        new Timeline(new KeyFrame(Duration.millis(200),
                new KeyValue(wrapper.opacityProperty(), 1, Interpolator.EASE_BOTH)
        )).play();
    }

    /* =========================================================
     * Popup UI Controls
     * ========================================================= */

    public static Button createCloseButton() {
        Button btn = new Button("✕");
        btn.setId("closeBtn");
        btn.getStyleClass().add("popup-box-close-btn");

        btn.setOnAction(e -> handleClosePopupContent());

        AnchorPane.setRightAnchor(btn, 14.0);
        AnchorPane.setTopAnchor(btn, 14.0);
        return btn;
    }

    private static void openPopupContentAnimation() {
        staticOverlayPane.setVisible(true);
        staticOverlayPane.setTranslateX(0);
        staticPopupContentPane.setTranslateX(POPUP_CONTENT_WIDTH);

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(100),
                        new KeyValue(loadingOverlay.opacityProperty(), 1, Interpolator.EASE_BOTH),
                        new KeyValue(staticPopupContentPane.translateXProperty(), 0, Interpolator.EASE_BOTH)
                )
        );
        timeline.play();
    }

    public static void handleClosePopupContent() {
        if (!isPopupContentOpen) return;

        Timeline timeline = new Timeline(
                new KeyFrame(
                        Duration.millis(100),
                        new KeyValue(loadingOverlay.opacityProperty(), 0, Interpolator.EASE_BOTH),
                        new KeyValue(staticPopupContentPane.opacityProperty(), 0, Interpolator.EASE_BOTH)
                )
        );

        timeline.setOnFinished(e -> {
            staticOverlayPane.setVisible(false);
            staticOverlayPane.setManaged(false);

            staticPopupContentPane.getChildren().clear();
            staticPopupContentPane.setDisable(false);

            staticPopupContentPane.setOpacity(1);
            loadingOverlay.setOpacity(1);

            isPopupContentOpen = false;
            applyBlur(false);
        });

        timeline.play();
    }

    private static void applyBlur(boolean enable) {
        staticBorderPane.setEffect(enable ? new GaussianBlur(10) : null);
    }

    /* =========================================================
     * Loading Overlay
     * ========================================================= */

    private StackPane createLoadingOverlay() {
        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxSize(60, 60);

        StackPane overlay = new StackPane(pi);
        overlay.setPickOnBounds(true); // blocks clicks behind it
        overlay.setStyle("""
                -fx-background-color: rgba(0,0,0,0.25);
                -fx-padding: 20;
                """);
        return overlay;
    }

    private void showLoading(boolean show) {
        if (loadingOverlay == null) return;
        loadingOverlay.setVisible(show);
        loadingOverlay.setManaged(show);
    }

    /* =========================================================
     * Helpers
     * ========================================================= */

    private void hideLinks(Hyperlink... links) {
        Arrays.stream(links).forEach(this::hideLink);
    }

    private void hideLink(Hyperlink link) {
        if (link == null) return;
        link.setVisible(false);
        link.setManaged(false);
    }

    private URL requireResource(String path) {
        return Objects.requireNonNull(getClass().getResource(path), "Missing resource: " + path);
    }

    /* =========================================================
     * Overlay Click Handler
     * ========================================================= */

    @FXML
    private void handleOverlayClick(MouseEvent e) {
        if (staticOverlayPane != null && e.getTarget() == staticOverlayPane) {
            handleClosePopupContent();
        }
    }

//    private void setupSettingsPopup() {
//        Hyperlink link1 = new Hyperlink("User Configuration");
//
//        Hyperlink link2 = new Hyperlink("Payment Configuration");
//
//        VBox box = new VBox(10, link1, link2);
//        box.getStyleClass().add("container");
//
//        settingsPopup = new Popup();
//        settingsPopup.getContent().add(box);
//        settingsPopup.setAutoHide(true);
//
//        link1.setOnAction(e -> settingsPopup.hide());
//        link2.setOnAction(e -> settingsPopup.hide());
//
//        settingIcon.setOnMouseClicked(e -> {
//            if (settingsPopup.isShowing()) {
//                settingsPopup.hide();
//                return;
//            }
//
//            // place popup under icon (screen cords)
//            var bounds = settingIcon.localToScreen(settingIcon.getBoundsInLocal());
//            settingsPopup.show(settingIcon,
//                    bounds.getMaxX() + 100,
//                    bounds.getMaxY() + 20
//            );
//        });
//    }

}
