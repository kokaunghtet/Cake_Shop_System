package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.AuthResult;
import com.cakeshopsystem.utils.ImageHelper;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.UserDAO;
import com.cakeshopsystem.utils.session.SessionManager;
import com.cakeshopsystem.utils.validators.Validator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.ResourceBundle;

public class EditUserConfigController implements Initializable {

    // =====================================
    // FXML UI COMPONENTS
    // =====================================

    // Profile
    @FXML private ImageView ivUserProfile;
    @FXML private Button btnEditImage;

    // User info
    @FXML private TextField tfUsername;
    @FXML private TextField tfEmail;
    @FXML private Label lblRole;

    // Password toggle: Current
    @FXML private PasswordField pfCurrentPassword;
    @FXML private TextField tfCurrentPassword;
    @FXML private FontIcon icToggleCurrent;

    // Password toggle: New
    @FXML private PasswordField pfNewPassword;
    @FXML private TextField tfNewPassword;
    @FXML private FontIcon icToggleNew;

    // Password toggle: Confirm
    @FXML private PasswordField pfConfirmPassword;
    @FXML private TextField tfConfirmPassword;
    @FXML private FontIcon icToggleConfirm;

    // Password errors (under fields)
    @FXML private Label lblCurrentPasswordError;
    @FXML private Label lblNewPasswordError;
    @FXML private Label lblConfirmPasswordError;
    @FXML private Label lblUsernameError;

    // Buttons
    @FXML private Button btnCancel;
    @FXML private Button btnSave;

    // Avatar layout
    @FXML private StackPane avatarHost;
    @FXML private Circle avatarClip;

    // =====================================
    // STATE & CONSTANTS
    // =====================================
    private User currentUser = SessionManager.getUser();
    private String selectedImagePath;

    private boolean isCurrentPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    private static final Duration MSG_DURATION = Duration.seconds(2);

    private static final String MSG_CONFIRM_REQUIRED = "Confirm password cannot be empty";

    private static final String CSS_ERROR_MSG  = "error-message";
    private static final String CSS_INPUT_ERR  = "input-error";

    // =====================================
    // LIFECYCLE
    // =====================================
    @Override
    public void initialize(URL location, ResourceBundle resources) {

        if (avatarHost != null && avatarClip != null) {
            avatarClip.centerXProperty().bind(avatarHost.widthProperty().divide(2));
            avatarClip.centerYProperty().bind(avatarHost.heightProperty().divide(2));
            avatarClip.radiusProperty().bind(avatarHost.widthProperty().divide(2));
        }

        setUser();

        bindManagedToVisible(pfCurrentPassword, tfCurrentPassword);
        bindManagedToVisible(pfNewPassword, tfNewPassword);
        bindManagedToVisible(pfConfirmPassword, tfConfirmPassword);

        // initial state
        tfCurrentPassword.setVisible(false);
        tfNewPassword.setVisible(false);
        tfConfirmPassword.setVisible(false);

        // keep both fields in sync
        pfCurrentPassword.textProperty().addListener((obs, o, n) -> syncCurrentPasswordFields(n));
        tfCurrentPassword.textProperty().addListener((obs, o, n) -> syncCurrentPasswordFields(n));

        pfNewPassword.textProperty().addListener((obs, o, n) -> syncNewPasswordFields(n));
        tfNewPassword.textProperty().addListener((obs, o, n) -> syncNewPasswordFields(n));

        pfConfirmPassword.textProperty().addListener((obs, o, n) -> syncConfirmPasswordFields(n));
        tfConfirmPassword.textProperty().addListener((obs, o, n) -> syncConfirmPasswordFields(n));

        // toggle icon clicks
        if (icToggleCurrent != null) icToggleCurrent.setOnMouseClicked(e -> toggleCurrentPasswordVisibility());
        if (icToggleNew != null) icToggleNew.setOnMouseClicked(e -> toggleNewPasswordVisibility());
        if (icToggleConfirm != null) icToggleConfirm.setOnMouseClicked(e -> toggleConfirmPasswordVisibility());

        // actions
        if (btnCancel != null) btnCancel.setOnAction(e -> handleCancel());
        if (btnSave != null) btnSave.setOnAction(e -> handleSave());
        if (btnEditImage != null) btnEditImage.setOnAction(e -> handleEditImage());

        // live validation
        setupLiveValidation();

        clearPasswordErrors();
    }

    // =====================================
    // LIVE VALIDATION
    // =====================================
    private void setupLiveValidation() {
        tfUsername.textProperty().addListener((obs, o, n) -> validateUsernameField());

        // use text fields since they mirror values
        if (tfCurrentPassword != null) tfCurrentPassword.textProperty().addListener((obs, o, n) -> revalidate());
        if (tfNewPassword != null) tfNewPassword.textProperty().addListener((obs, o, n) -> revalidate());
        if (tfConfirmPassword != null) tfConfirmPassword.textProperty().addListener((obs, o, n) -> revalidate());
    }

    private void validateUsernameField() {
        String username = tfUsername.getText();

        if (isBlank(username)) {
            applyValidation(tfUsername, tfUsername, lblUsernameError, null);
            return;
        }

        String err = Validator.validateUsername(username);
        applyValidation(tfUsername, tfUsername, lblUsernameError, err);
    }

    private void revalidate() {
        validatePasswordSection(false);
    }

    private boolean validatePasswordSection(boolean forSubmit) {
        String currentPw = getCurrentPasswordValue();
        String newPw     = getNewPasswordValue();
        String confirmPw = getConfirmPasswordValue();

        boolean wantsPasswordChange = !isBlank(newPw) || !isBlank(confirmPw);

        if (!wantsPasswordChange) {
            clearPasswordErrors();
            return true;
        }

        String currentErr = (!forSubmit && isBlank(currentPw))
                ? null
                : Validator.validateLoginPassword(currentPw);

        String newErr = (!forSubmit && isBlank(newPw))
                ? null
                : Validator.validatePassword(newPw);

        String confirmErr;
        if (!forSubmit && isBlank(confirmPw)) {
            confirmErr = null;
        } else if (isBlank(confirmPw)) {
            confirmErr = MSG_CONFIRM_REQUIRED;
        } else if (newErr == null) {
            confirmErr = Validator.matchPassword(newPw, confirmPw);
        } else {
            confirmErr = null;
        }

        applyValidation(pfCurrentPassword, tfCurrentPassword, lblCurrentPasswordError, currentErr);
        applyValidation(pfNewPassword, tfNewPassword, lblNewPasswordError, newErr);
        applyValidation(pfConfirmPassword, tfConfirmPassword, lblConfirmPasswordError, confirmErr);

        return currentErr == null && newErr == null && confirmErr == null;
    }

    // =====================================
    // VISIBILITY BINDING
    // =====================================
    private void bindManagedToVisible(Control pf, Control tf) {
        if (pf != null) pf.managedProperty().bind(pf.visibleProperty());
        if (tf != null) tf.managedProperty().bind(tf.visibleProperty());
    }

    // =====================================
    // PASSWORD VISIBILITY TOGGLES
    // =====================================
    @FXML
    private void toggleCurrentPasswordVisibility() {
        final boolean currentlyVisible = isCurrentPasswordVisible;
        final String currentText = getCurrentPasswordValue();
        final int caret = currentlyVisible
                ? tfCurrentPassword.getCaretPosition()
                : pfCurrentPassword.getCaretPosition();

        isCurrentPasswordVisible = !isCurrentPasswordVisible;

        pfCurrentPassword.setVisible(!isCurrentPasswordVisible);
        tfCurrentPassword.setVisible(isCurrentPasswordVisible);

        if (isCurrentPasswordVisible) {
            tfCurrentPassword.setText(currentText);
            tfCurrentPassword.requestFocus();
            tfCurrentPassword.positionCaret(Math.min(caret, currentText.length()));
        } else {
            pfCurrentPassword.setText(currentText);
            pfCurrentPassword.requestFocus();
            pfCurrentPassword.positionCaret(Math.min(caret, currentText.length()));
        }

        if (icToggleCurrent != null) {
            icToggleCurrent.setIconLiteral(isCurrentPasswordVisible ? "fas-eye" : "fas-eye-slash");
        }
    }

    @FXML
    private void toggleNewPasswordVisibility() {
        final boolean currentlyVisible = isNewPasswordVisible;
        final String currentText = getNewPasswordValue();
        final int caret = currentlyVisible
                ? tfNewPassword.getCaretPosition()
                : pfNewPassword.getCaretPosition();

        isNewPasswordVisible = !isNewPasswordVisible;

        pfNewPassword.setVisible(!isNewPasswordVisible);
        tfNewPassword.setVisible(isNewPasswordVisible);

        if (isNewPasswordVisible) {
            tfNewPassword.setText(currentText);
            tfNewPassword.requestFocus();
            tfNewPassword.positionCaret(Math.min(caret, currentText.length()));
        } else {
            pfNewPassword.setText(currentText);
            pfNewPassword.requestFocus();
            pfNewPassword.positionCaret(Math.min(caret, currentText.length()));
        }

        if (icToggleNew != null) {
            icToggleNew.setIconLiteral(isNewPasswordVisible ? "fas-eye" : "fas-eye-slash");
        }
    }

    @FXML
    private void toggleConfirmPasswordVisibility() {
        final boolean currentlyVisible = isConfirmPasswordVisible;
        final String currentText = getConfirmPasswordValue();
        final int caret = currentlyVisible
                ? tfConfirmPassword.getCaretPosition()
                : pfConfirmPassword.getCaretPosition();

        isConfirmPasswordVisible = !isConfirmPasswordVisible;

        pfConfirmPassword.setVisible(!isConfirmPasswordVisible);
        tfConfirmPassword.setVisible(isConfirmPasswordVisible);

        if (isConfirmPasswordVisible) {
            tfConfirmPassword.setText(currentText);
            tfConfirmPassword.requestFocus();
            tfConfirmPassword.positionCaret(Math.min(caret, currentText.length()));
        } else {
            pfConfirmPassword.setText(currentText);
            pfConfirmPassword.requestFocus();
            pfConfirmPassword.positionCaret(Math.min(caret, currentText.length()));
        }

        if (icToggleConfirm != null) {
            icToggleConfirm.setIconLiteral(isConfirmPasswordVisible ? "fas-eye" : "fas-eye-slash");
        }
    }

    // =====================================
    // PASSWORD VALUE HELPERS
    // =====================================
    private String getCurrentPasswordValue() {
        return isCurrentPasswordVisible ? tfCurrentPassword.getText() : pfCurrentPassword.getText();
    }

    private String getNewPasswordValue() {
        return isNewPasswordVisible ? tfNewPassword.getText() : pfNewPassword.getText();
    }

    private String getConfirmPasswordValue() {
        return isConfirmPasswordVisible ? tfConfirmPassword.getText() : pfConfirmPassword.getText();
    }

    // =====================================
    // PASSWORD SYNC HELPERS
    // =====================================
    private void syncCurrentPasswordFields(String newValue) {
        if (!Objects.equals(pfCurrentPassword.getText(), newValue)) pfCurrentPassword.setText(newValue);
        if (!Objects.equals(tfCurrentPassword.getText(), newValue)) tfCurrentPassword.setText(newValue);
    }

    private void syncNewPasswordFields(String newValue) {
        if (!Objects.equals(pfNewPassword.getText(), newValue)) pfNewPassword.setText(newValue);
        if (!Objects.equals(tfNewPassword.getText(), newValue)) tfNewPassword.setText(newValue);
    }

    private void syncConfirmPasswordFields(String newValue) {
        if (!Objects.equals(pfConfirmPassword.getText(), newValue)) pfConfirmPassword.setText(newValue);
        if (!Objects.equals(tfConfirmPassword.getText(), newValue)) tfConfirmPassword.setText(newValue);
    }

    // =====================================
    // LOAD USER DATA
    // =====================================
    public void setUser() {
        currentUser = SessionManager.getUser();
        if (currentUser == null) return;

        tfUsername.setText(currentUser.getUserName());
        tfEmail.setText(currentUser.getEmail());

        if (currentUser.getRole() == 1) lblRole.setText("Admin");
        else if (currentUser.getRole() == 2) lblRole.setText("Cashier");

        String path = currentUser.getImagePath();
        if (path == null || path.isBlank()) path = "/images/default-profile.jpg";

        Image img = loadImageSmart(path);
        if (img == null || img.isError()) img = loadImageSmart("/images/default-profile.jpg");

        if (img != null && !img.isError()) {
            ImageHelper.setCircularAvatar(ivUserProfile, img, 240);
        }
    }

    // =====================================
    // BUTTON HANDLERS
    // =====================================
    @FXML
    private void handleCancel() {
        selectedImagePath = null;
        setUser();
        clearPasswordInputs();
        clearPasswordErrors();
    }

    @FXML
    private void handleSave() {
        if (currentUser == null) return;

        String newUsername = tfUsername.getText() == null ? "" : tfUsername.getText().trim();
        if (isBlank(newUsername)) {
            snack(SnackBarType.ERROR, "Invalid Input", "Username cannot be empty.");
            tfUsername.requestFocus();
            return;
        }

        String email = currentUser.getEmail(); // unchanged

        clearPasswordErrors();
        boolean okPwUI = validatePasswordSection(true);
        if (!okPwUI) {
            snack(SnackBarType.ERROR, "Fix Password", "Please correct the password fields.");
            return;
        }

        String currentPw = getCurrentPasswordValue();
        String newPw     = getNewPasswordValue();
        String confirmPw = getConfirmPasswordValue();

        boolean wantsPasswordChange = !isBlank(newPw) || !isBlank(confirmPw);

        if (wantsPasswordChange) {
            AuthResult check = UserDAO.checkPassword(currentUser.getUserId(), currentPw);
            if (!check.success()) {
                applyValidation(pfCurrentPassword, tfCurrentPassword, lblCurrentPasswordError, check.message());
                snack(SnackBarType.ERROR, "Incorrect Password", check.message());
                return;
            }
        }

        String finalImagePath = (selectedImagePath != null && !selectedImagePath.isBlank())
                ? selectedImagePath
                : currentUser.getImagePath();

        // preserve active (since no checkbox in UI)
        boolean finalActive = currentUser.isActive();

        User updated = new User(currentUser.getUserId(), newUsername, email, currentUser.getRole());
        updated.setImagePath(finalImagePath);
        updated.setActive(finalActive);

        boolean okProfile = UserDAO.updateUser(updated);
        if (!okProfile) {
            snack(SnackBarType.ERROR, "Save Failed", "Could not update profile.");
            return;
        }

        if (wantsPasswordChange) {
            AuthResult upd = UserDAO.updatePassword(currentUser.getUserId(), newPw);
            if (!upd.success()) {
                snack(SnackBarType.ERROR, "Password Update Failed", upd.message());
                return;
            }
        }

        // update session user (MainController listener will refresh UI if you added it)
        currentUser.setUserName(newUsername);
        currentUser.setEmail(email);
        currentUser.setImagePath(finalImagePath);
        currentUser.setActive(finalActive);
        SessionManager.setUser(currentUser);

        selectedImagePath = null;
        clearPasswordInputs();
        clearPasswordErrors();

        snack(SnackBarType.SUCCESS, "Saved", "Profile updated successfully.");
    }

    @FXML
    private void handleEditImage() {
        if (btnEditImage == null || btnEditImage.getScene() == null || currentUser == null) return;

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choose Profile Photo");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp")
        );

        File file = chooser.showOpenDialog(btnEditImage.getScene().getWindow());
        if (file == null) return;

        if (file.length() > 5L * 1024 * 1024) {
            snack(SnackBarType.WARNING, "Image Too Large", "Max 5MB allowed.");
            return;
        }

        try {
            Path dir = Paths.get(System.getProperty("user.home"), ".cake-shop-system", "avatars");
            Files.createDirectories(dir);

            String ext = getFileExtension(file.getName());
            String filename = "user_" + currentUser.getUserId() + "_" + System.currentTimeMillis() + ext;

            Path dest = dir.resolve(filename);
            Files.copy(file.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);

            selectedImagePath = dest.toAbsolutePath().toString();

            Image img = ImageHelper.load(selectedImagePath);
            if (img == null || img.isError()) {
                snack(SnackBarType.ERROR, "Image Error", "Failed to load selected image.");
                selectedImagePath = null;
                return;
            }

            ImageHelper.setCircularAvatar(ivUserProfile, img, 240);

        } catch (Exception e) {
            snack(SnackBarType.ERROR, "Image Error", "Could not set profile image.");
        }
    }

    // =====================================
    // UI HELPERS
    // =====================================
    private void snack(SnackBarType type, String title, String msg) {
        SnackBar.show(type, title, msg, MSG_DURATION);
    }

    private void clearPasswordInputs() {
        pfCurrentPassword.clear();
        tfCurrentPassword.clear();
        pfNewPassword.clear();
        tfNewPassword.clear();
        pfConfirmPassword.clear();
        tfConfirmPassword.clear();
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    private void applyValidation(Control pf, Control tf, Label msgLabel, String error) {
        if (msgLabel != null) {
            msgLabel.setText(error == null ? "" : error);

            boolean show = error != null && !error.isBlank();
            msgLabel.setVisible(show);
            msgLabel.setManaged(show);

            msgLabel.getStyleClass().remove(CSS_ERROR_MSG);
            if (show) msgLabel.getStyleClass().add(CSS_ERROR_MSG);
        }

        if (pf != null) {
            pf.getStyleClass().remove(CSS_INPUT_ERR);
            if (error != null) pf.getStyleClass().add(CSS_INPUT_ERR);
        }
        if (tf != null) {
            tf.getStyleClass().remove(CSS_INPUT_ERR);
            if (error != null) tf.getStyleClass().add(CSS_INPUT_ERR);
        }
    }

    private void clearPasswordErrors() {
        applyValidation(pfCurrentPassword, tfCurrentPassword, lblCurrentPasswordError, null);
        applyValidation(pfNewPassword, tfNewPassword, lblNewPasswordError, null);
        applyValidation(pfConfirmPassword, tfConfirmPassword, lblConfirmPasswordError, null);
    }

    // =====================================
    // IMAGE HELPERS
    // =====================================
    private String getFileExtension(String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot).toLowerCase();
    }

    private Image loadImageSmart(String path) {
        try {
            if (path.matches("^(https?|file|jar):.*")) return new Image(path, true);
            String res = path.startsWith("/") ? path : "/" + path;
            var url = getClass().getResource(res);
            if (url != null) return new Image(url.toExternalForm(), true);
            var f = new File(path);
            if (f.exists()) return new Image(f.toURI().toString(), true);
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
