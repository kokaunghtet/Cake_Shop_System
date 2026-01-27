package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Role;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.AuthResult;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.cache.UserCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.UserDAO;
import com.cakeshopsystem.utils.validators.Validator;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import javafx.util.StringConverter;
import org.kordamp.ikonli.javafx.FontIcon;

public class AddUserController {

    // =========================
    // UI Messages
    // =========================
    private static final String MSG_ROLE_REQUIRED = "Role cannot be empty";
    private static final String MSG_CONFIRM_REQUIRED = "Confirm password cannot be empty";

    // =========================
    // CSS Classes
    // =========================
    private static final String CSS_ERROR_MSG = "error-message";
    private static final String CSS_INPUT_ERROR = "input-error";

    // =========================
    // State
    // =========================
    private boolean attemptedSubmit = false;
    private boolean isPasswordVisible = false;

    // =========================
    // FXML: Inputs
    // =========================
    @FXML
    private TextField usernameTextField;
    @FXML
    private TextField emailTextField;
    @FXML
    private PasswordField passwordPasswordField;
    @FXML
    private PasswordField confirmPasswordPasswordField;
    @FXML
    private ComboBox<Role> roleComboBox;
    @FXML
    private TextField passwordTextField;
    @FXML
    private TextField confirmPasswordTextField;
    @FXML
    private FontIcon eyeSlashToggleButton1;
    @FXML
    private FontIcon eyeSlashToggleButton2;

    // =========================
    // FXML: Error Messages
    // =========================
    @FXML
    private Label usernameErrorMessage;
    @FXML
    private Label emailErrorMessage;
    @FXML
    private Label passwordErrorMessage;
    @FXML
    private Label confirmPasswordErrorMessage;
    @FXML
    private Label roleErrorMessage;

    // =========================
    // FXML: Actions
    // =========================
    @FXML
    private Button addUserBtn;

    // =========================
    // Form Snapshot
    // =========================
    private record FormData(String username, String email, String password, String confirmPassword, Role role) {
    }

    private final StringProperty newPasswordValue     = new SimpleStringProperty("");
    private final StringProperty confirmPasswordValue = new SimpleStringProperty("");

    // Independent visibility flags
    private final BooleanProperty newPasswordVisible     = new SimpleBooleanProperty(false);
    private final BooleanProperty confirmPasswordVisible = new SimpleBooleanProperty(false);


    // =========================
    // Lifecycle
    // =========================
    public void initialize() {
        setupPasswordToggle(
                passwordPasswordField,
                passwordTextField,
                eyeSlashToggleButton1,
                newPasswordValue,
                newPasswordVisible
        );

        setupPasswordToggle(
                confirmPasswordPasswordField,
                confirmPasswordTextField,
                eyeSlashToggleButton2,
                confirmPasswordValue,
                confirmPasswordVisible
        );
        roleComboBox.setItems(RoleCache.getRolesList());

        roleComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Role role) {
                return role == null ? "" : role.getRoleName();
            }

            @Override
            public Role fromString(String string) {
                return null;
            }
        });

        addUserBtn.setOnAction(e -> handleAddUser());

        setupLiveValidation();
    }


    // =========================
    // Event Handlers
    // =========================
    private void handleAddUser() {
        attemptedSubmit = true;

        if (!validateForm()) return;

        FormData data = readForm();
        Role selectedRole = data.role();

        int roleId = selectedRole.getRoleId();

        String defaultProfileImg = "/images/default-profile.jpg";

        User user = new User();
        user.setUserName(data.username());
        user.setEmail(data.email());
        user.setPassword(data.password());
        user.setRole(roleId);
        user.setImagePath(defaultProfileImg);

        try {
            AuthResult result = UserDAO.insertUser(user);

            if (!result.success()) {
                String msg = result.message();

                if (msg != null && msg.toLowerCase().contains("username")) {
                    applyValidation(usernameTextField, usernameErrorMessage, msg);
                } else if (msg != null && msg.toLowerCase().contains("email")) {
                    applyValidation(emailTextField, emailErrorMessage, msg);
                } else {
                    SnackBar.show(SnackBarType.ERROR, "Failed", msg, Duration.seconds(3));
                    new Alert(Alert.AlertType.ERROR, msg == null ? "Failed to add user." : msg).show();
                }
                return;
            }

            UserCache.refreshUsers();
            SnackBar.show(SnackBarType.SUCCESS, "Success", result.message(), Duration.seconds(2));

            MainController.handleClosePopupContent();
        } catch (Exception ex) {
            ex.printStackTrace();
            SnackBar.show(SnackBarType.ERROR, "Failed", "Something went wrong during User Creation", Duration.seconds(2));
        }
    }

    // =========================
    // Validation
    // =========================
    private boolean validateForm() {
        FormData data = readForm();

        String usernameError = validateUsernameField(data);
        String emailError = validateEmailField(data);
        String passwordError = validatePasswordField(data);
        String confirmError = validateConfirmPasswordField(data, passwordError);
        String roleError = validateRoleField(data);

        return usernameError == null
                && emailError == null
                && passwordError == null
                && confirmError == null
                && roleError == null;
    }

    // Single entry point for live validation (listeners call this)
    private void revalidate() {
        validateForm();
    }

    private String validateUsernameField(FormData data) {
        String username = data.username();
        String error = (!attemptedSubmit && username.isEmpty())
                ? null
                : Validator.validateUsername(username);

        applyValidation(usernameTextField, usernameErrorMessage, error);
        return error;
    }

    private String validateEmailField(FormData data) {
        String email = data.email();
        String error = (!attemptedSubmit && email.isEmpty())
                ? null
                : Validator.validateEmail(email);

        applyValidation(emailTextField, emailErrorMessage, error);
        return error;
    }

    private String validatePasswordField(FormData data) {
        String password = data.password();
        String error = (!attemptedSubmit && password.isEmpty())
                ? null
                : Validator.validatePassword(password);

        applyValidation(passwordPasswordField, passwordErrorMessage, error);
        return error;
    }

    private String validateConfirmPasswordField(FormData data, String passwordError) {
        String confirm = data.confirmPassword();

        String error = null;
        if (confirm.isEmpty()) {
            error = attemptedSubmit ? MSG_CONFIRM_REQUIRED : null;
        } else if (passwordError == null) {
            error = Validator.matchPassword(data.password(), confirm);
        }

        applyValidation(confirmPasswordTextField, confirmPasswordErrorMessage, error);
        return error;
    }

    private String validateRoleField(FormData data) {
        Role role = data.role();
        String error = (role == null && attemptedSubmit) ? MSG_ROLE_REQUIRED : null;

        applyValidation(roleComboBox, roleErrorMessage, error);
        return error;
    }

    private void setupLiveValidation() {
        usernameTextField.textProperty().addListener((obs, o, n) -> revalidate());
        emailTextField.textProperty().addListener((obs, o, n) -> revalidate());
        passwordTextField.textProperty().addListener((obs, o, n) -> revalidate());
        confirmPasswordTextField.textProperty().addListener((obs, o, n) -> revalidate());
        roleComboBox.valueProperty().addListener((obs, o, n) -> revalidate());
    }

    private FormData readForm() {
        return new FormData(
                safeTrim(usernameTextField.getText()),
                safeTrim(emailTextField.getText()),
                safeTrim(passwordTextField.getText()),
                safeTrim(confirmPasswordTextField.getText()),
                roleComboBox.getValue()
        );
    }

    // =========================
    // UI Helpers
    // =========================
    private void applyValidation(Control field, Label messageLabel, String error) {
        messageLabel.setText(error == null ? "" : error);
        messageLabel.getStyleClass().remove(CSS_ERROR_MSG);
        if (error != null) messageLabel.getStyleClass().add(CSS_ERROR_MSG);

        field.getStyleClass().remove(CSS_INPUT_ERROR);
        if (error != null) field.getStyleClass().add(CSS_INPUT_ERROR);
    }

    private String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
    private void setupPasswordToggle(
            PasswordField passwordField,
            TextField textField,
            FontIcon icon,
            StringProperty sharedValue,
            BooleanProperty visibleFlag
    ) {
        // Hidden fields should not take layout space
        passwordField.managedProperty().bind(passwordField.visibleProperty());
        textField.managedProperty().bind(textField.visibleProperty());

        // Bind both fields to the same value (no manual sync methods)
        passwordField.textProperty().bindBidirectional(sharedValue);
        textField.textProperty().bindBidirectional(sharedValue);

        // Toggle visibility
        passwordField.visibleProperty().bind(visibleFlag.not());
        textField.visibleProperty().bind(visibleFlag);

        // Icon behavior
        icon.setIconLiteral("fas-eye-slash");
        icon.setOnMouseClicked(ev -> visibleFlag.set(!visibleFlag.get()));
        visibleFlag.addListener((obs, oldV, newV) ->
                icon.setIconLiteral(newV ? "fas-eye" : "fas-eye-slash"));
    }

}


