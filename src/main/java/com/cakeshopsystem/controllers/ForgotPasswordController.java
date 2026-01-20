package com.cakeshopsystem.controllers;

import com.cakeshopsystem.utils.AuthResult;
import com.cakeshopsystem.utils.ChangeScene;
import com.cakeshopsystem.utils.MailHelper;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.OneTimePasswordDAO;
import com.cakeshopsystem.utils.dao.UserDAO;
import com.cakeshopsystem.utils.validators.Validator;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;

public class ForgotPasswordController {

    private boolean isPasswordVisible = false;

    // ============================
    // FXML: Email + OTP Form
    // ============================
    @FXML
    private TextField emailTextField;
    @FXML
    private TextField otpTextField;
    @FXML
    private Button sendOTPBtn;
    @FXML
    private Button verifyOTPBtn;
    @FXML
    private VBox emailForm;

    // ============================
    // FXML: New Password Form
    // ============================
    @FXML
    private VBox passwordForm;
    @FXML
    private TextField newPasswordTextField;
    @FXML
    private TextField confirmNewPasswordTextField;
    @FXML
    private Button confirmBtn;
    @FXML
    private PasswordField confirmNewPasswordPasswordField;
    @FXML
    private PasswordField newPasswordPasswordField;

    // ============================
    // FXML: Navigation
    // ============================
    @FXML
    private Hyperlink backToLoginLink;

    // ============================
    // State: Current Reset User
    // ============================
    private Integer resetUserId = null;

    @FXML
    private FontIcon eyeSlashToggleButton;

    @FXML
    private FontIcon eyeSlashToggleButton1;

    // ============================
    // Controller Lifecycle
    // ============================
    public void initialize() {
        passwordForm.setVisible(false);

        otpTextField.setDisable(true);
        verifyOTPBtn.setDisable(true);

        emailForm.setVisible(false);
        passwordForm.setVisible(true);

        // --- Make hidden fields not take up layout space ---
        newPasswordPasswordField.managedProperty().bind(newPasswordPasswordField.visibleProperty()); // If not visible, don't reserve space
        newPasswordTextField.managedProperty().bind(newPasswordTextField.visibleProperty()); // If not visible, don't reserve space

        // --- Ensure initial state is consistent ---
        newPasswordPasswordField.setVisible(true);    // Start with hidden-password field visible
        newPasswordTextField.setVisible(false);   // Start with plain-text field hidden

        // --- Keep both password fields synced (single path, avoids redundant loops) ---
        newPasswordPasswordField.textProperty().addListener((obs, oldValue, newValue) -> syncPasswordFields(newValue)); // Typing in PasswordField
        newPasswordTextField.textProperty().addListener((obs, oldValue, newValue) -> syncPasswordFields(newValue)); // Typing in TextField

        eyeSlashToggleButton.setOnMouseClicked(event -> togglePasswordVisibility());
        eyeSlashToggleButton.setOnMouseClicked(event -> togglePasswordVisibility1());


//        backToLoginLink.setOnAction(this::backToLogin);
//        sendOTPBtn.setOnAction(this::handleSendOtp);
//        verifyOTPBtn.setOnAction(this::handleVerifyOtp);
//        confirmBtn.setOnAction(this::handleConfirmNewPassword);
    }


//
//    // ============================
//    // Step 1: Send OTP Email
//    // ============================
//    private void handleSendOtp(ActionEvent e) {
//        String emailInput = emailTextField.getText();
//
//        String emailError = Validator.validateEmail(emailInput);
//        if (emailError != null) {
//            SnackBar.show(SnackBarType.ERROR, "Email Required", emailError, Duration.seconds(3));
//            return;
//        }
//
//        String email = emailInput.trim();
//
//        sendOTPBtn.setDisable(true);
//
//        MailHelper.sendMail(email, "Cake Shop System password reset request.")
//                .thenAccept(result -> Platform.runLater(() -> {
//                    sendOTPBtn.setDisable(false);
//
//                    SnackBar.show(
//                            result.success() ? SnackBarType.SUCCESS : SnackBarType.ERROR,
//                            result.message(),
//                            "",
//                            Duration.seconds(3)
//                    );
//
//                    otpTextField.setDisable(false);
//                    verifyOTPBtn.setDisable(false);
//
//                    int uid = UserDAO.isEmailExists(email);
//                    resetUserId = uid > 0 ? uid : null;
//                }));
//    }
//
//    // ============================
//    // Step 2: Verify OTP Code
//    // ============================
//    private void handleVerifyOtp(ActionEvent e) {
//        String otpInput = otpTextField.getText();
//        String otpError = validateOtp(otpInput);
//
//        if (otpError != null) {
//            SnackBar.show(SnackBarType.ERROR, "Invalid OTP", otpError, Duration.seconds(3));
//            return;
//        }
//
//        String otp = otpInput.trim();
//
//        // Security: don't reveal if email exists
//        if (resetUserId == null) {
//            SnackBar.show(SnackBarType.ERROR, "Invalid OTP", "Invalid or expired OTP.", Duration.seconds(3));
//            return;
//        }
//
//        boolean ok = OneTimePasswordDAO.isOtpValid(resetUserId, otp);
//        if (!ok) {
//            SnackBar.show(SnackBarType.ERROR, "Invalid OTP", "Invalid or expired OTP.", Duration.seconds(3));
//            return;
//        }
//
//        slideAway(emailForm);
//        slideIn(passwordForm);
//    }
//
//    // ============================
//    // Step 3: Update Password
//    // ============================
//    private void handleConfirmNewPassword(ActionEvent e) {
//        if (resetUserId == null) {
//            SnackBar.show(SnackBarType.ERROR, "Error", "Please verify OTP first.", Duration.seconds(3));
//            return;
//        }
//
//        String p1 = newPasswordTextField.getText();
//        String p2 = confirmNewPasswordTextField.getText();
//
//        String passError = Validator.validatePassword(p1);
//        if (passError != null) {
//            SnackBar.show(SnackBarType.ERROR, "Weak Password", passError, Duration.seconds(3));
//            return;
//        }
//
//        String matchError = Validator.matchPassword(p1, p2);
//        if (matchError != null) {
//            SnackBar.show(SnackBarType.ERROR, "Mismatch", matchError, Duration.seconds(3));
//            return;
//        }
//
//        confirmBtn.setDisable(true);
//
//        AuthResult res = UserDAO.updatePassword(resetUserId, p1);
//
//        if (res.success()) {
//            SnackBar.show(SnackBarType.SUCCESS, "Success", res.message(), Duration.seconds(3));
//            ChangeScene.switchScene("LoginForm.fxml", "Cake Shop System | Login Form");
//        } else {
//            SnackBar.show(SnackBarType.ERROR, "Failed", res.message(), Duration.seconds(3));
//            confirmBtn.setDisable(false);
//        }
//    }
//
//    // ============================
//    // OTP Validation (6 digits only)
//    // ============================
//    private String validateOtp(String otp) {
//        otp = otp == null ? "" : otp.trim();
//        if (otp.isEmpty()) return "OTP cannot be empty";
//        if (!otp.matches("\\d{6}")) return "OTP must be exactly 6 digits";
//        return null;
//    }
//
//    // ============================
//    // Navigation
//    // ============================
//    private void backToLogin(ActionEvent e) {
//        ChangeScene.switchScene("LoginForm.fxml", "Cake Shop System | Login Form");
//    }

    // ============================
    // UI Animations
    // ============================
    private void slideIn(VBox vBox) {
        vBox.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), vBox);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        TranslateTransition slideUp = new TranslateTransition(Duration.millis(300), vBox);
        slideUp.setFromX(-100);
        slideUp.setToX(0);

        new ParallelTransition(fadeIn, slideUp).play();
    }

    private void slideAway(VBox vBox) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), vBox);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        TranslateTransition slideDown = new TranslateTransition(Duration.millis(300), vBox);
        slideDown.setFromX(0);
        slideDown.setToX(100);

        ParallelTransition transition = new ParallelTransition(fadeOut, slideDown);
        transition.setOnFinished(ev -> vBox.setVisible(false));
        transition.play();
    }


    // ============================
// Helper Method
// ============================
    @FXML
    private void togglePasswordVisibility() {
        // --- Capture current state before switching ---
        final boolean currentlyVisible = isPasswordVisible; // Snapshot current visibility state

        // Read current text + caret from whichever field is currently active
        final String currentText = getPasswordValue();      // Current password string from active field
        final int caret = currentlyVisible
                ? newPasswordTextField.getCaretPosition()       // Caret position when plain-text is active
                : newPasswordPasswordField.getCaretPosition();      // Caret position when password field is active

        // --- Flip state ---
        isPasswordVisible = !isPasswordVisible;             // Toggle visibility flag

        // --- Apply visibility (and managed binding handles layout space) ---
        newPasswordPasswordField.setVisible(!isPasswordVisible);    // Show PasswordField when NOT visible mode
        newPasswordTextField.setVisible(isPasswordVisible);     // Show TextField when visible mode

        // --- Sync text into the field that is becoming visible ---
        if (isPasswordVisible) {
            newPasswordTextField.setText(currentText);          // Ensure plain-text field matches current value
            newPasswordTextField.requestFocus();                // Move focus so user can keep typing
            newPasswordTextField.positionCaret(Math.min(caret, currentText.length())); // Restore caret safely
        } else {
            newPasswordPasswordField.setText(currentText);          // Ensure PasswordField matches current value
            newPasswordPasswordField.requestFocus();                // Move focus so user can keep typing
            newPasswordPasswordField.positionCaret(Math.min(caret, currentText.length())); // Restore caret safely
        }

        // Changes between eye & eye-slash icon
        eyeSlashToggleButton.setIconLiteral(isPasswordVisible ? "fas-eye" : "fas-eye-slash");

    }

    private String getPasswordValue() {
        return isPasswordVisible ? newPasswordTextField.getText() : newPasswordPasswordField.getText();
    }

    private String getPasswordValue1() {
        return isPasswordVisible ? confirmNewPasswordTextField.getText() : confirmNewPasswordPasswordField.getText();
    }


    @FXML
    private void togglePasswordVisibility1() {
        // --- Capture current state before switching ---
        final boolean currentlyVisible = isPasswordVisible; // Snapshot current visibility state

        // Read current text + caret from whichever field is currently active
        final String currentText = getPasswordValue1();      // Current password string from active field
        final int caret = currentlyVisible
                ? confirmNewPasswordTextField.getCaretPosition()       // Caret position when plain-text is active
                : confirmNewPasswordPasswordField.getCaretPosition();      // Caret position when password field is active

        // --- Flip state ---
        isPasswordVisible = !isPasswordVisible;             // Toggle visibility flag

        // --- Apply visibility (and managed binding handles layout space) ---
        confirmNewPasswordPasswordField.setVisible(!isPasswordVisible);    // Show PasswordField when NOT visible mode
        confirmNewPasswordTextField.setVisible(isPasswordVisible);     // Show TextField when visible mode

        // --- Sync text into the field that is becoming visible ---
        if (isPasswordVisible) {
            confirmNewPasswordTextField.setText(currentText);          // Ensure plain-text field matches current value
            confirmNewPasswordTextField.requestFocus();                // Move focus so user can keep typing
            confirmNewPasswordTextField.positionCaret(Math.min(caret, currentText.length())); // Restore caret safely
        } else {
            confirmNewPasswordPasswordField.setText(currentText);          // Ensure PasswordField matches current value
            confirmNewPasswordPasswordField.requestFocus();                // Move focus so user can keep typing
            confirmNewPasswordPasswordField.positionCaret(Math.min(caret, currentText.length())); // Restore caret safely
        }

        // Changes between eye & eye-slash icon
        eyeSlashToggleButton.setIconLiteral(isPasswordVisible ? "fas-eye" : "fas-eye-slash");

    }

    private void syncPasswordFields(String newValue) {
        // Update PasswordField if needed
        if (!Objects.equals(newPasswordPasswordField.getText(), newValue)) {
            newPasswordPasswordField.setText(newValue); // Keep hidden field in sync
        }

        // Update TextField if needed
        if (!Objects.equals(newPasswordTextField.getText(), newValue)) {
            newPasswordTextField.setText(newValue); // Keep visible field in sync
        }
    }

    private void syncPasswordFields1(String newValue) {
        // Update PasswordField if needed
        if (!Objects.equals(confirmNewPasswordPasswordField.getText(), newValue)) {
            confirmNewPasswordPasswordField.setText(newValue); // Keep hidden field in sync
        }

        // Update TextField if needed
        if (!Objects.equals(confirmNewPasswordTextField.getText(), newValue)) {
            confirmNewPasswordTextField.setText(newValue); // Keep visible field in sync


        }
    }
}


