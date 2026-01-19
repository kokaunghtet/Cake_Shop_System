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
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ForgotPasswordController {

    // ============================
    // FXML: Email + OTP Form
    // ============================
    @FXML private TextField emailTextField;
    @FXML private TextField otpTextField;
    @FXML private Button sendOTPBtn;
    @FXML private Button verifyOTPBtn;
    @FXML private VBox emailForm;

    // ============================
    // FXML: New Password Form
    // ============================
    @FXML private VBox passwordForm;
    @FXML private TextField newPasswordTextField;
    @FXML private TextField confirmNewPasswordTextField;
    @FXML private Button confirmBtn;

    // ============================
    // FXML: Navigation
    // ============================
    @FXML private Hyperlink backToLoginLink;

    // ============================
    // State: Current Reset User
    // ============================
    private Integer resetUserId = null;

    // ============================
    // Controller Lifecycle
    // ============================
    public void initialize() {
        passwordForm.setVisible(false);

        otpTextField.setDisable(true);
        verifyOTPBtn.setDisable(true);

        backToLoginLink.setOnAction(this::backToLogin);
        sendOTPBtn.setOnAction(this::handleSendOtp);
        verifyOTPBtn.setOnAction(this::handleVerifyOtp);
        confirmBtn.setOnAction(this::handleConfirmNewPassword);
    }

    // ============================
    // Step 1: Send OTP Email
    // ============================
    private void handleSendOtp(ActionEvent e) {
        String emailInput = emailTextField.getText();

        String emailError = Validator.validateEmail(emailInput);
        if (emailError != null) {
            SnackBar.show(SnackBarType.ERROR, "Email Required", emailError, Duration.seconds(3));
            return;
        }

        String email = emailInput.trim();

        sendOTPBtn.setDisable(true);

        MailHelper.sendMail(email, "Cake Shop System password reset request.")
                .thenAccept(result -> Platform.runLater(() -> {
                    sendOTPBtn.setDisable(false);

                    SnackBar.show(
                            result.success() ? SnackBarType.SUCCESS : SnackBarType.ERROR,
                            result.message(),
                            "",
                            Duration.seconds(3)
                    );

                    otpTextField.setDisable(false);
                    verifyOTPBtn.setDisable(false);

                    int uid = UserDAO.isEmailExists(email);
                    resetUserId = uid > 0 ? uid : null;
                }));
    }

    // ============================
    // Step 2: Verify OTP Code
    // ============================
    private void handleVerifyOtp(ActionEvent e) {
        String otpInput = otpTextField.getText();
        String otpError = validateOtp(otpInput);

        if (otpError != null) {
            SnackBar.show(SnackBarType.ERROR, "Invalid OTP", otpError, Duration.seconds(3));
            return;
        }

        String otp = otpInput.trim();

        // Security: don't reveal if email exists
        if (resetUserId == null) {
            SnackBar.show(SnackBarType.ERROR, "Invalid OTP", "Invalid or expired OTP.", Duration.seconds(3));
            return;
        }

        boolean ok = OneTimePasswordDAO.isOtpValid(resetUserId, otp);
        if (!ok) {
            SnackBar.show(SnackBarType.ERROR, "Invalid OTP", "Invalid or expired OTP.", Duration.seconds(3));
            return;
        }

        slideAway(emailForm);
        slideIn(passwordForm);
    }

    // ============================
    // Step 3: Update Password
    // ============================
    private void handleConfirmNewPassword(ActionEvent e) {
        if (resetUserId == null) {
            SnackBar.show(SnackBarType.ERROR, "Error", "Please verify OTP first.", Duration.seconds(3));
            return;
        }

        String p1 = newPasswordTextField.getText();
        String p2 = confirmNewPasswordTextField.getText();

        String passError = Validator.validatePassword(p1);
        if (passError != null) {
            SnackBar.show(SnackBarType.ERROR, "Weak Password", passError, Duration.seconds(3));
            return;
        }

        String matchError = Validator.matchPassword(p1, p2);
        if (matchError != null) {
            SnackBar.show(SnackBarType.ERROR, "Mismatch", matchError, Duration.seconds(3));
            return;
        }

        confirmBtn.setDisable(true);

        AuthResult res = UserDAO.updatePassword(resetUserId, p1);

        if (res.success()) {
            SnackBar.show(SnackBarType.SUCCESS, "Success", res.message(), Duration.seconds(3));
            ChangeScene.switchScene("LoginForm.fxml", "Cake Shop System | Login Form");
        } else {
            SnackBar.show(SnackBarType.ERROR, "Failed", res.message(), Duration.seconds(3));
            confirmBtn.setDisable(false);
        }
    }

    // ============================
    // OTP Validation (6 digits only)
    // ============================
    private String validateOtp(String otp) {
        otp = otp == null ? "" : otp.trim();
        if (otp.isEmpty()) return "OTP cannot be empty";
        if (!otp.matches("\\d{6}")) return "OTP must be exactly 6 digits";
        return null;
    }

    // ============================
    // Navigation
    // ============================
    private void backToLogin(ActionEvent e) {
        ChangeScene.switchScene("LoginForm.fxml", "Cake Shop System | Login Form");
    }

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
}
