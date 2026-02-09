package com.cakeshopsystem.controllers;

import com.cakeshopsystem.utils.AuthResult;
import com.cakeshopsystem.utils.ChangeScene;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.UserDAO;
import com.cakeshopsystem.utils.validators.Validator;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginFormController {

    // =====================================
    // STATE & CONSTANTS
    // =====================================
    private int emptyAttemptCount = 0;
    private PauseTransition loginDisableTimer;
    private final int DISABLE_SECONDS = 15;

    private boolean isPasswordVisible = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("login-worker");
        return t;
    });

    // =====================================
    // FXML UI COMPONENTS
    // =====================================
    @FXML private StackPane loginStackPane;

    @FXML private FontIcon eyeSlashToggleButton;
    @FXML private Hyperlink forgotPasswordLink;
    @FXML private Button loginButton;

    @FXML private PasswordField pswPasswordField;
    @FXML private TextField txtPasswordField;
    @FXML private TextField usernameTextField;

    @FXML private Label errorMessage;
    @FXML private Label usernameMessage;
    @FXML private Label passwordMessage;

    // =====================================
    // LIFECYCLE
    // =====================================
    @FXML
    private void initialize() {
        usernameTextField.requestFocus();

        pswPasswordField.managedProperty().bind(pswPasswordField.visibleProperty());
        txtPasswordField.managedProperty().bind(txtPasswordField.visibleProperty());

        pswPasswordField.setVisible(true);
        txtPasswordField.setVisible(false);

        errorMessage.managedProperty().bind(errorMessage.visibleProperty());
        errorMessage.visibleProperty().bind(errorMessage.textProperty().isNotEmpty());

        pswPasswordField.textProperty().addListener((obs, o, n) -> syncPasswordFields(n));
        txtPasswordField.textProperty().addListener((obs, o, n) -> syncPasswordFields(n));

        usernameTextField.textProperty().addListener((o, ov, nv) -> resetEmptyAttemptsIfAnyInput());
        pswPasswordField.textProperty().addListener((o, ov, nv) -> resetEmptyAttemptsIfAnyInput());
        txtPasswordField.textProperty().addListener((o, ov, nv) -> resetEmptyAttemptsIfAnyInput());

        eyeSlashToggleButton.setOnMouseClicked(event -> togglePasswordVisibility());

        loginButton.setDefaultButton(true);

        usernameTextField.setOnAction(e -> attemptLogin());
        pswPasswordField.setOnAction(e -> attemptLogin());
        txtPasswordField.setOnAction(e -> attemptLogin());

        loginButton.setOnAction(e -> attemptLogin());
        forgotPasswordLink.setOnAction(this::handleForgotPassword);
    }

    // =====================================
    // LOGIN FLOW
    // =====================================
    private void attemptLogin() {
        if (isBothFieldsEmpty()) {
            handleEmptySubmitAttempt();
            return;
        }

        emptyAttemptCount = 0;
        handleLogin();
    }

    private void handleLogin() {

        clearLoginFeedback();

        String usernameValue = safeTrim(usernameTextField.getText());
        String passwordValue = safeTrim(getPasswordValue());

        if (!validateLoginInputs(usernameValue, passwordValue)) return;

        setLoginUiBusy(true);
        startLoginAsync(usernameValue, passwordValue);
    }

    @FXML
    private void handleForgotPassword(ActionEvent event) {
        ChangeScene.switchScene("ForgotPassword.fxml", "Forgot Password");
    }

    // =====================================
    // EMPTY ATTEMPT HANDLING
    // =====================================
    private boolean isBothFieldsEmpty() {
        String user = usernameTextField.getText() == null ? "" : usernameTextField.getText().trim();
        String pass = getPasswordValue() == null ? "" : getPasswordValue().trim();
        return user.isEmpty() && pass.isEmpty();
    }

    private void handleEmptySubmitAttempt() {
        emptyAttemptCount++;

        if (emptyAttemptCount >= 3) {
            SnackBar.show(
                    SnackBarType.WARNING,
                    "Last Warning",
                    "Please fill in the required fields before trying again. Login is disabled for " + DISABLE_SECONDS + " seconds.",
                    Duration.seconds(3)
            );
            disableLoginForSeconds(DISABLE_SECONDS);
            emptyAttemptCount = 0;
        } else {
            SnackBar.show(
                    SnackBarType.ERROR,
                    "Missing Information",
                    "Please enter your username and password. (" + emptyAttemptCount + "/3)",
                    Duration.seconds(2)
            );
        }

        usernameTextField.requestFocus();
    }

    private void disableLoginForSeconds(int seconds) {
        loginButton.setDisable(true);

        if (loginDisableTimer != null) {
            loginDisableTimer.stop();
        }

        loginDisableTimer = new PauseTransition(Duration.seconds(seconds));
        loginDisableTimer.setOnFinished(e -> loginButton.setDisable(false));
        loginDisableTimer.play();
    }

    private void resetEmptyAttemptsIfAnyInput() {
        String user = usernameTextField.getText() == null ? "" : usernameTextField.getText().trim();
        String pass = getPasswordValue() == null ? "" : getPasswordValue().trim();

        if (!user.isEmpty() || !pass.isEmpty()) {
            emptyAttemptCount = 0;
        }
    }

    // =====================================
    // PASSWORD VISIBILITY
    // =====================================
    @FXML
    private void togglePasswordVisibility() {

        final boolean currentlyVisible = isPasswordVisible;
        final String currentText = getPasswordValue();
        final int caret = currentlyVisible
                ? txtPasswordField.getCaretPosition()
                : pswPasswordField.getCaretPosition();

        isPasswordVisible = !isPasswordVisible;

        pswPasswordField.setVisible(!isPasswordVisible);
        txtPasswordField.setVisible(isPasswordVisible);

        if (isPasswordVisible) {
            txtPasswordField.setText(currentText);
            txtPasswordField.requestFocus();
            txtPasswordField.positionCaret(Math.min(caret, currentText.length()));
        } else {
            pswPasswordField.setText(currentText);
            pswPasswordField.requestFocus();
            pswPasswordField.positionCaret(Math.min(caret, currentText.length()));
        }

        eyeSlashToggleButton.setIconLiteral(isPasswordVisible ? "fas-eye" : "fas-eye-slash");
    }

    private String getPasswordValue() {
        return isPasswordVisible ? txtPasswordField.getText() : pswPasswordField.getText();
    }

    private void syncPasswordFields(String newValue) {
        if (!Objects.equals(pswPasswordField.getText(), newValue)) {
            pswPasswordField.setText(newValue);
        }
        if (!Objects.equals(txtPasswordField.getText(), newValue)) {
            txtPasswordField.setText(newValue);
        }
    }

    // =====================================
    // ASYNC AUTHENTICATION
    // =====================================
    private void startLoginAsync(String usernameValue, String passwordValue) {

        Task<AuthResult> loginTask = createLoginTask(usernameValue, passwordValue);
        wireBusyState(loginTask);

        loginTask.setOnSucceeded(event ->
                handleLoginSucceeded(usernameValue, loginTask.getValue())
        );

        loginTask.setOnFailed(event ->
                handleLoginFailed(loginTask.getException())
        );

        executor.submit(loginTask);
    }

    private Task<AuthResult> createLoginTask(String usernameValue, String passwordValue) {
        return new Task<>() {
            @Override
            protected AuthResult call() throws Exception {
                return UserDAO.authenticateUser(usernameValue, passwordValue);
            }
        };
    }

    private void wireBusyState(Task<?> task) {
        task.stateProperty().addListener((obs, oldState, newState) -> {
            switch (newState) {
                case SUCCEEDED, FAILED, CANCELLED -> setLoginUiBusy(false);
                default -> {}
            }
        });
    }

    // =====================================
    // AUTH RESULT HANDLING
    // =====================================
    private void handleLoginSucceeded(String usernameValue, AuthResult authResult) {

        if (authResult != null && Boolean.TRUE.equals(authResult.success())) {
            ChangeScene.switchScene("Main.fxml", "Main Page");
            SnackBar.show(SnackBarType.SUCCESS, "Welcome, " + usernameValue, "", Duration.seconds(3));
            return;
        }

        String msg = (authResult == null || authResult.message() == null)
                ? "Login failed."
                : authResult.message();

        errorMessage.setText(msg);
        errorMessage.getStyleClass().setAll("error-message");

        SnackBar.show(SnackBarType.ERROR, msg, "", Duration.seconds(3));
    }

    private void handleLoginFailed(Throwable ex) {
        if (ex != null) ex.printStackTrace();

        String msg = "Something went wrong. Please try again.";
        errorMessage.setText(msg);
        errorMessage.getStyleClass().setAll("error-message");

        SnackBar.show(SnackBarType.ERROR, msg, "", Duration.seconds(3));
    }

    // =====================================
    // VALIDATION & UI HELPERS
    // =====================================
    private boolean validateLoginInputs(String usernameValue, String passwordValue) {

        String usernameError = Validator.validateUsername(usernameValue);
        String passwordError = Validator.validateLoginPassword(passwordValue);

        applyValidation(usernameMessage, usernameError);
        applyValidation(passwordMessage, passwordError);

        if (usernameValue.isBlank() && passwordValue.isBlank()) {
            SnackBar.show(
                    SnackBarType.ERROR,
                    "Missing Information",
                    "Please enter both your username and password",
                    Duration.seconds(3)
            );
            return false;
        }

        return usernameError == null && passwordError == null;
    }

    private void applyValidation(Label messageLabel, String error) {
        messageLabel.setText(error == null ? "" : error);
        messageLabel.getStyleClass().remove("error-message");
        if (error != null) messageLabel.getStyleClass().add("error-message");
    }

    private void clearLoginFeedback() {
        clearLabel(errorMessage);
        clearLabel(usernameMessage);
        clearLabel(passwordMessage);
    }

    private void clearLabel(Label label) {
        label.setText("");
        label.getStyleClass().remove("error-message");
    }

    private void setLoginUiBusy(boolean busy) {
        loginButton.setDisable(busy);
        loginButton.setText(busy ? "Logging in..." : "Login");
        usernameTextField.setDisable(busy);
        pswPasswordField.setDisable(busy);
        txtPasswordField.setDisable(busy);
        forgotPasswordLink.setDisable(busy);
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }
}
