package com.cakeshopsystem.controllers;

import com.cakeshopsystem.utils.AuthResult;
import com.cakeshopsystem.utils.ChangeScene;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.UserDAO;
import com.cakeshopsystem.utils.session.SessionManager;
import com.cakeshopsystem.utils.validators.Validator;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginFormController {
    @FXML
    private StackPane loginStackPane;

    @FXML
    private FontAwesomeIconView eyeSlashToggleButton;

    @FXML
    private Hyperlink forgetPasswordLink;

    @FXML
    private Button loginButton;

    @FXML
    private PasswordField pswPasswordField;

    @FXML
    private TextField txtPasswordField;

    @FXML
    private TextField usernameTextField;

    @FXML
    private Label errorMessage, usernameMessage, passwordMessage;

    private boolean isPasswordVisible = false;

    @FXML
    private void initialize() {
        usernameTextField.requestFocus();

        errorMessage.managedProperty().bind(errorMessage.visibleProperty());
        errorMessage.visibleProperty().bind(errorMessage.textProperty().isNotEmpty());

        pswPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            if (!isPasswordVisible) {
                txtPasswordField.setText(newText);
            }
        });

        txtPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            if (isPasswordVisible) {
                pswPasswordField.setText(newText);
            }
        });

        eyeSlashToggleButton.setOnMouseClicked(event -> togglePasswordVisibility());

        loginButton.setOnAction(e -> handleLogin());
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("login-worker");
        return t;
    });

    private void handleLogin() {
        // --- Clear previous messages ---
        errorMessage.setText("");
        usernameMessage.setText("");
        passwordMessage.setText("");

        String usernameValue = safeTrim(usernameTextField.getText());
        String passwordValue = safeTrim(pswPasswordField.getText());

        // --- Validate on FX thread (before background work) ---
        String usernameError = Validator.validateUsername(usernameValue);
        String passwordError = Validator.validateLoginPassword(passwordValue);

        applyValidation(usernameMessage, usernameError);
        applyValidation(passwordMessage, passwordError);

        boolean usernameEmpty = usernameValue.isBlank();
        boolean passwordEmpty = passwordValue.isBlank();

        if (usernameEmpty && passwordEmpty) {
            SnackBar.show(
                    SnackBarType.ERROR,
                    "Missing Information",
                    "Please enter both your username and password",
                    Duration.seconds(3)
            );
            return;
        }

        if (usernameError != null || passwordError != null) {
            return;
        }

        setLoginUiBusy(true);

        Task<AuthResult> loginTask = new Task<>() {
            @Override
            protected AuthResult call() throws Exception {
                return UserDAO.authenticateUser(usernameValue, passwordValue);
            }
        };

        loginTask.setOnSucceeded(event -> {
            setLoginUiBusy(false);

            AuthResult authResult = loginTask.getValue();
            if (authResult != null && Boolean.TRUE.equals(authResult.success())) {
                ChangeScene.switchScene("Main.fxml", "Main Page");
                SnackBar.show(SnackBarType.SUCCESS, "Welcome, " + usernameValue, "", Duration.seconds(3));
            } else {
                String msg = (authResult == null || authResult.message() == null)
                        ? "Login failed."
                        : authResult.message();

                errorMessage.setText(msg);
                errorMessage.getStyleClass().setAll("error-message");
                SnackBar.show(SnackBarType.ERROR, msg, "", Duration.seconds(3));
            }
        });

        loginTask.setOnFailed(event -> {
            setLoginUiBusy(false);

            Throwable ex = loginTask.getException();
            ex.printStackTrace();

            errorMessage.setText("Something went wrong. Please try again.");
            errorMessage.getStyleClass().setAll("error-message");
            SnackBar.show(SnackBarType.ERROR, "Something went wrong. Please try again.", "", Duration.seconds(3));
        });

        executor.submit(loginTask);
    }

    private void applyValidation(Label messageLabel, String error) {
        messageLabel.setText(error == null ? "" : error);

        messageLabel.getStyleClass().remove("error-message");
        if (error != null) {
            messageLabel.getStyleClass().add("error-message");
        }
    }


    private void setLoginUiBusy(boolean busy) {
        loginButton.setDisable(busy);
        loginButton.setText(busy ? "Logging in..." : "Login");
        usernameTextField.setDisable(busy);
        pswPasswordField.setDisable(busy);
        txtPasswordField.setDisable(busy);
        forgetPasswordLink.setDisable(busy);
    }

    private String getPasswordValue() {
        return isPasswordVisible ? txtPasswordField.getText() : pswPasswordField.getText();
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        pswPasswordField.setVisible(!isPasswordVisible);
        txtPasswordField.setVisible(isPasswordVisible);

        // Change the icon between EYE & EYE_SLASH
        eyeSlashToggleButton.setGlyphName(isPasswordVisible ? FontAwesomeIcon.EYE.name() : FontAwesomeIcon.EYE_SLASH.name());
    }
}
