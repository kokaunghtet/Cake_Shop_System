package com.cakeshopsystem.controllers;

import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

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
    private TextField pswTextField;

    @FXML
    private TextField usernameTextField;

    private boolean isPasswordVisible = false;

    @FXML
    private void initialize() {
        usernameTextField.requestFocus();

        pswPasswordField.textProperty().addListener((obs, oldText, newText) -> {
            if (!isPasswordVisible) {
                pswTextField.setText(newText);
            }
        });

        pswTextField.textProperty().addListener((obs, oldText, newText) -> {
            if (isPasswordVisible) {
                pswPasswordField.setText(newText);
            }
        });

        eyeSlashToggleButton.setOnMouseClicked(event -> togglePasswordVisibility());

        loginButton.setOnAction(e -> handleLogin());
    }

    private void handleLogin() {
        String usernameValue = usernameTextField.getText().trim();
        String passwordValue = pswPasswordField.getText().trim();

        loginButton.setDisable(true);
        loginButton.setText("Logging in...");
    }

    @FXML
    private void togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        pswPasswordField.setVisible(!isPasswordVisible);
        pswTextField.setVisible(isPasswordVisible);

        // Change the icon between EYE & EYE_SLASH
        eyeSlashToggleButton.setGlyphName(isPasswordVisible ? FontAwesomeIcon.EYE.name() : FontAwesomeIcon.EYE_SLASH.name());
    }
}
