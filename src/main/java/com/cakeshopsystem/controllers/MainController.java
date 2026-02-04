package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.ImageHelper;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.components.BreadcrumbBar;
import com.cakeshopsystem.utils.components.BreadcrumbManager;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.services.CartService;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
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
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainController {

    /* =========================================================
     * 1. CONSTANTS & STATE
     * ========================================================= */
    private static final String DEFAULT_VIEW_FXML = "/views/admin/AdminDashboard.fxml";
    private static final String DEFAULT_CASHIER_VIEW_FXML = "/views/ProductView.fxml";
    private static final String DEFAULT_TITLE_PREFIX = "Cake Shop System | ";

    private Hyperlink activeLink;
    private static boolean isPopupContentOpen = false;
    private Popup settingsPopup;

    private final ExecutorService fxmlLoaderExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "fxml-loader");
        t.setDaemon(true);
        return t;
    });

    /* =========================================================
     * 2. FXML INJECTIONS
     * ========================================================= */
    @FXML
    private FontIcon settingIcon;
    @FXML
    private FontIcon signOutIcon;
    @FXML
    private FontIcon moonIcon;

    @FXML
    private AnchorPane contentPane;
    @FXML
    private BorderPane mainBorderPane;
    @FXML
    private StackPane mainStackPane;
    @FXML
    private AnchorPane overlayPane;
    @FXML
//    private AnchorPane popupPane;
    private StackPane popupPane;
    //    @FXML
    //    public StackPane popupHost;
    @FXML
    private StackPane badgePane;


    @FXML
    private ImageView profileImageView;
    @FXML
    private Label roleLabel;
    @FXML
    private HBox topBar;
    @FXML
    private Label usernameLabel;

    @FXML
    private Hyperlink adminDashboardLink;
    @FXML
    private Hyperlink adminProductLink;
    @FXML
    private Hyperlink customOrderLink;
    @FXML
    private Hyperlink memberLink;
    @FXML
    private Hyperlink staffProductLink;
    @FXML
    private Hyperlink userLink;
    @FXML
    private Hyperlink diyOrderLink;
    @FXML
    private Hyperlink bookingLink;

    @FXML
    private BreadcrumbBar breadcrumbBar;

    @FXML
    private Button cartBtn;
    @FXML
    private Label badgeLabel;


    /* =========================================================
     * 3. STATIC REFERENCES (For Popup/Focus System)
     * ========================================================= */
    private static StackPane loadingOverlay;
    private static BorderPane staticBorderPane;
    //    private static AnchorPane staticPopupContentPane;
    private static StackPane staticPopupContentPane;
    private static AnchorPane staticOverlayPane;

    private static EventHandler<KeyEvent> focusTrapHandler;
    private static Parent focusTrapRoot;

    private final CartService cartService = CartService.getInstance();

    /* =========================================================
     * 4. INITIALIZATION
     * ========================================================= */
    @FXML
    public void initialize() {
        staticOverlayPane = overlayPane;
        staticBorderPane = mainBorderPane;
        staticPopupContentPane = popupPane;

        staticOverlayPane.setVisible(false);

        updateUserInfo();
        configureRoleBasedAccess();

        if (signOutIcon != null) {
            signOutIcon.setOnMouseClicked(e -> handleLogout());
        }

        loadingOverlay = createLoadingOverlay();
        loadingOverlay.setVisible(false);
        loadingOverlay.setManaged(false);

        if (mainStackPane != null && !mainStackPane.getChildren().contains(loadingOverlay)) {
            mainStackPane.getChildren().add(loadingOverlay);
        }

        if (breadcrumbBar != null) {
            breadcrumbBar.bindToGlobal();
        }

        initSettingsPopup();
        if (settingIcon != null) settingIcon.setOnMouseClicked(e -> showSettingsPopup());


        if (SessionManager.isAdmin) {
            BreadcrumbManager.setBreadcrumbs();
            navigate(DEFAULT_VIEW_FXML, adminDashboardLink, "Dashboard");

            hideCartUI();
        } else {
            BreadcrumbManager.setBreadcrumbs();
            navigate(DEFAULT_CASHIER_VIEW_FXML, staffProductLink, "Products");

            initCartUI();
        }

        if (adminDashboardLink != null) adminDashboardLink.setOnMouseClicked(e -> loadAdminDashboard());
        if (userLink != null) userLink.setOnMouseClicked(e -> loadUser());
        if (memberLink != null) memberLink.setOnMouseClicked(e -> loadMember());
        if (adminProductLink != null) adminProductLink.setOnMouseClicked(e -> loadAdminProduct());
        if (staffProductLink != null) staffProductLink.setOnMouseClicked(e -> loadStaffProduct());
        if (customOrderLink != null) customOrderLink.setOnMouseClicked(e -> loadCustomOrder());
        if (diyOrderLink != null) diyOrderLink.setOnMouseClicked(e -> loadDiyOrder());
        if (bookingLink != null) bookingLink.setOnMouseClicked(e -> loadBooking());
    }

    /* =========================================================
     * 5. NAVIGATION & VIEW LOADING
     * ========================================================= */
    private void navigate(String fxmlPath, Hyperlink navLink, String pageName) {
        loadView(fxmlPath, navLink, DEFAULT_TITLE_PREFIX + pageName, pageName);
    }

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

    public void loadStaffProduct() {
        navigate("/views/ProductView.fxml", staffProductLink, "Products");
    }

    public void loadCustomOrder() {
        navigate("/views/CustomOrder.fxml", customOrderLink, "Custom Orders");
    }

    public void loadDiyOrder() {
        navigate("/views/DiyOrder.fxml", diyOrderLink, "DIY");
    }

    public void loadBooking() {
        navigate("/views/Booking.fxml", bookingLink, "Bookings");
    }

    private void openCartPopup() {
        togglePopupContent("/views/Cart.fxml");
    }

    private void loadView(String fxmlPath, Hyperlink navLink, String windowTitle, String breadcrumbTitle) {
        setCenterContent(fxmlPath, windowTitle);
        setActiveLink(navLink);

        BreadcrumbManager.setBreadcrumbs(new BreadcrumbBar.BreadcrumbItem(breadcrumbTitle, ev -> loadView(fxmlPath, navLink, windowTitle, breadcrumbTitle)));
    }

    private void initCartUI() {
        if (cartBtn == null || badgePane == null || badgeLabel == null) return;

        cartBtn.setOnAction(e -> openCartPopup());

        badgePane.visibleProperty().unbind();
        badgeLabel.textProperty().unbind();

        badgePane.visibleProperty().bind(cartService.distinctProductCountProperty().greaterThan(0));

        badgeLabel.textProperty().bind(
                Bindings.createStringBinding(() -> {
                    int n = cartService.getDistinctProductCount();
                    return n > 99 ? "99+" : String.valueOf(n);
                }, cartService.distinctProductCountProperty())
        );
    }


    private void hideCartUI() {
        if (cartBtn != null) {
            cartBtn.setVisible(false);
            cartBtn.setManaged(false);
        }
        if (badgePane != null) {
            badgePane.visibleProperty().unbind();
            badgePane.setVisible(false);
            badgePane.setManaged(false);
        }
        if (badgeLabel != null) {
            badgeLabel.textProperty().unbind();
        }
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

    private void setActiveLink(Hyperlink link) {
        if (link == null) return;
        if (activeLink != null) {
            activeLink.getStyleClass().remove("active");
        }
        activeLink = link;
        activeLink.getStyleClass().add("active");
    }

    /* =========================================================
     * 6. POPUP SYSTEM (Settings & Modal Popups)
     * ========================================================= */
    private void initSettingsPopup() {
        Hyperlink link1 = new Hyperlink("User Configuration");
        Hyperlink link2 = new Hyperlink("Payment Configuration");

        VBox box;
        if (SessionManager.isAdmin) {
            box = new VBox(10, link1, link2);
        } else {
            box = new VBox(link1);
        }
        box.getStyleClass().add("container");

        settingsPopup = new Popup();
        settingsPopup.getContent().add(box);
        settingsPopup.setAutoHide(true);

        link1.setOnAction(e -> {
            togglePopupContent("/views/EditUserConfig.fxml");
            settingsPopup.hide();
        });
        link2.setOnAction(e -> {
            togglePopupContent("/views/EditPaymentConfig.fxml");
            settingsPopup.hide();
        });
    }

    private void showSettingsPopup() {
        if (settingsPopup == null) return;
        if (settingsPopup.isShowing()) {
            settingsPopup.hide();
            return;
        }
        var bounds = settingIcon.localToScreen(settingIcon.getBoundsInLocal());
        if (bounds != null) {
            settingsPopup.show(settingIcon, bounds.getMaxX() - 140, bounds.getMaxY() + 20);
        }
    }

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
        Button existingCloseBtn = (Button) staticPopupContentPane.lookup("#closeBtn");
        if (existingCloseBtn == null) {
            Button closeBtn = createCloseButton();
            staticPopupContentPane.getChildren().add(closeBtn);
        }
        setPopupContent(content);
    }

//    private static void setPopupContent(Parent content) {
//        if (content instanceof Region r) {
//            r.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
//        }
//
//        Button closeBtn = (Button) staticPopupContentPane.lookup("#closeBtn");
//        if (closeBtn == null) closeBtn = createCloseButton();
//
//        StackPane wrapper = new StackPane();
//        wrapper.setPadding(new Insets(15));
//        wrapper.getChildren().addAll(content, closeBtn);
//        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);
//
//        AnchorPane.setTopAnchor(wrapper, 0.0);
//        AnchorPane.setRightAnchor(wrapper, 0.0);
//        AnchorPane.setBottomAnchor(wrapper, 0.0);
//        AnchorPane.setLeftAnchor(wrapper, 0.0);
//
//        staticPopupContentPane.getChildren().setAll(wrapper);
//        Platform.runLater(() -> installFocusTrap(wrapper));
//
//        wrapper.setOpacity(0);
//        new Timeline(new KeyFrame(Duration.millis(200), new KeyValue(wrapper.opacityProperty(), 1, Interpolator.EASE_BOTH))).play();
//    }

    private static void setPopupContent(Parent content) {
        if (content instanceof Region r) {
            r.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
            r.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        }

        Button closeBtn = createCloseButton();

        StackPane card = new StackPane(content, closeBtn);
        card.getStyleClass().add("container");
        card.setPadding(new Insets(20));

        StackPane.setAlignment(closeBtn, Pos.TOP_RIGHT);

        card.setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        card.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);

        staticPopupContentPane.getChildren().setAll(card);
        StackPane.setAlignment(card, Pos.CENTER);
    }

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
        staticOverlayPane.setManaged(true);
        staticPopupContentPane.setOpacity(0);
        staticPopupContentPane.setScaleX(0.96);
        staticPopupContentPane.setScaleY(0.96);

        new Timeline(new KeyFrame(Duration.millis(160), new KeyValue(staticPopupContentPane.opacityProperty(), 1, Interpolator.EASE_BOTH), new KeyValue(staticPopupContentPane.scaleXProperty(), 1, Interpolator.EASE_BOTH), new KeyValue(staticPopupContentPane.scaleYProperty(), 1, Interpolator.EASE_BOTH))).play();
    }

    public static void handleClosePopupContent() {
        if (!isPopupContentOpen) return;
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(140), new KeyValue(staticPopupContentPane.opacityProperty(), 0, Interpolator.EASE_BOTH), new KeyValue(staticPopupContentPane.scaleXProperty(), 0.96, Interpolator.EASE_BOTH), new KeyValue(staticPopupContentPane.scaleYProperty(), 0.96, Interpolator.EASE_BOTH)));
        tl.setOnFinished(e -> {
            staticOverlayPane.setVisible(false);
            staticOverlayPane.setManaged(false);
            staticPopupContentPane.getChildren().clear();
            staticPopupContentPane.setOpacity(1);
            staticPopupContentPane.setScaleX(1);
            staticPopupContentPane.setScaleY(1);
            isPopupContentOpen = false;
            applyBlur(false);
            uninstallFocusTrap();
        });
        tl.play();
    }

    @FXML
    private void handleOverlayClick(MouseEvent e) {
        if (staticOverlayPane != null && e.getTarget() == staticOverlayPane) {
            handleClosePopupContent();
        }
    }

    /* =========================================================
     * 7. SESSION & USER UI HELPERS
     * ========================================================= */
    private void handleLogout() {
        SessionManager.forceLogout();
        SnackBar.show(SnackBarType.SUCCESS, "Logged out successfully", "", Duration.seconds(3));
    }

    private void updateUserInfo() {
        User user = SessionManager.getUser();
        if (user == null) return;

        String path = user.getImagePath();
        if (path == null || path.isBlank()) path = "/images/default-profile.jpg";

        Image userProfileImage = loadImageSmart(path);
        if (userProfileImage == null || userProfileImage.isError()) {
            userProfileImage = loadImageSmart("/images/default-profile.jpg");
        }

        if (userProfileImage != null && !userProfileImage.isError()) {
            ImageHelper.setCircularAvatar(profileImageView, userProfileImage, 60);
        }

        if (usernameLabel != null) usernameLabel.setText(user.getUserName());
        if (roleLabel != null && RoleCache.getRolesMap().containsKey(user.getRole())) {
            roleLabel.setText(RoleCache.getRolesMap().get(user.getRole()).getRoleName());
        }
    }

    private void configureRoleBasedAccess() {
        User user = SessionManager.getUser();
        if (user == null) return;
        if (!RoleCache.getRolesMap().containsKey(user.getRole())) return;

        String roleName = RoleCache.getRolesMap().get(user.getRole()).getRoleName();
        if (roleName.equalsIgnoreCase("Admin")) {
            hideLinks(customOrderLink, staffProductLink, bookingLink, diyOrderLink);
        } else if (roleName.equalsIgnoreCase("Cashier")) {
            hideLinks(adminDashboardLink, adminProductLink, memberLink, userLink);
        }
    }

    /* =========================================================
     * 8. UTILITY & VISUAL HELPERS
     * ========================================================= */
    private StackPane createLoadingOverlay() {
        ProgressIndicator pi = new ProgressIndicator();
        pi.setMaxSize(60, 60);
        StackPane overlay = new StackPane(pi);
        overlay.setPickOnBounds(true);
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.25); -fx-padding: 20;");
        return overlay;
    }

    private void showLoading(boolean show) {
        if (loadingOverlay == null) return;
        loadingOverlay.setVisible(show);
        loadingOverlay.setManaged(show);
    }

    private static void applyBlur(boolean enable) {
        staticBorderPane.setEffect(enable ? new GaussianBlur(10) : null);
    }

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

    private Image loadImageSmart(String path) {
        try {
            if (path.matches("^(https?|file|jar):.*")) return new Image(path, true);
            String resourcePath = path.startsWith("/") ? path : "/" + path;
            var url = getClass().getResource(resourcePath);
            if (url != null) return new Image(url.toExternalForm(), true);
            var f = new java.io.File(path);
            if (f.exists()) return new Image(f.toURI().toString(), true);
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /* =========================================================
     * 9. FOCUS TRAP ENGINE
     * ========================================================= */
    private static void installFocusTrap(Parent trapRoot) {
        if (staticOverlayPane == null) return;
        Scene scene = staticOverlayPane.getScene();
        if (scene == null) return;

        uninstallFocusTrap();
        focusTrapRoot = trapRoot;

        if (staticBorderPane != null) staticBorderPane.setDisable(true);

        focusTrapHandler = e -> {
            if (staticOverlayPane == null || !staticOverlayPane.isVisible()) return;
            if (e.getCode() != KeyCode.TAB) return;
            if (focusTrapRoot == null) return;

            List<Node> focusables = getFocusableNodes(focusTrapRoot);
            if (focusables.isEmpty()) return;

            Node owner = scene.getFocusOwner();
            int idx = focusables.indexOf(owner);
            boolean backward = e.isShiftDown();
            int nextIdx;

            if (idx < 0) {
                nextIdx = backward ? focusables.size() - 1 : 0;
            } else {
                nextIdx = backward ? (idx - 1 + focusables.size()) % focusables.size() : (idx + 1) % focusables.size();
            }

            focusables.get(nextIdx).requestFocus();
            e.consume();
        };

        scene.addEventFilter(KeyEvent.KEY_PRESSED, focusTrapHandler);
        List<Node> focusables = getFocusableNodes(focusTrapRoot);
        if (!focusables.isEmpty()) focusables.get(0).requestFocus();
    }

    private static void uninstallFocusTrap() {
        if (staticOverlayPane == null) return;
        Scene scene = staticOverlayPane.getScene();
        if (scene != null && focusTrapHandler != null) {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, focusTrapHandler);
        }
        focusTrapHandler = null;
        focusTrapRoot = null;
        if (staticBorderPane != null) staticBorderPane.setDisable(false);
    }

    private static List<Node> getFocusableNodes(Parent root) {
        List<Node> nodes = new ArrayList<>();
        collectFocusable(root, nodes);
        nodes.sort(Comparator.comparingDouble((Node n) -> n.localToScene(0, 0).getY()).thenComparingDouble(n -> n.localToScene(0, 0).getX()));
        return nodes;
    }

    private static void collectFocusable(Node node, List<Node> out) {
        if (node == null) return;
        if (node.isVisible() && node.isManaged() && !node.isDisabled() && node.isFocusTraversable()) {
            out.add(node);
        }
        if (node instanceof Parent p) {
            for (Node child : p.getChildrenUnmodifiable()) {
                collectFocusable(child, out);
            }
        }
    }
}