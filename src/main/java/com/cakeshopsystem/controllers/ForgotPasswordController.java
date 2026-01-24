package com.cakeshopsystem.controllers;

import com.cakeshopsystem.utils.AuthResult;
import com.cakeshopsystem.utils.ChangeScene;
import com.cakeshopsystem.utils.MailHelper;
import com.cakeshopsystem.utils.MailResultSet;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.OneTimePasswordDAO;
import com.cakeshopsystem.utils.dao.UserDAO;
import com.cakeshopsystem.utils.validators.Validator;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.concurrent.CompletableFuture;

public class ForgotPasswordController {

    private static final Duration SNACK_DURATION = Duration.seconds(3);
    private static final Duration ANIM_DURATION  = Duration.millis(300);

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

    @FXML private PasswordField newPasswordPasswordField;
    @FXML private TextField newPasswordTextField;

    @FXML private PasswordField confirmNewPasswordPasswordField;
    @FXML private TextField confirmNewPasswordTextField;

    @FXML private Button confirmBtn;

    // ============================
    // FXML: Navigation
    // ============================
    @FXML private Hyperlink backToLoginLink;

    // ============================
    // Icons
    // ============================
    @FXML private FontIcon eyeSlashToggleButton;   // for new password
    @FXML private FontIcon eyeSlashToggleButton1;  // for confirm password

    // ============================
    // State: Current Reset User
    // ============================
    private Integer resetUserId = null;

    // Store passwords once and bind both fields to them
    private final StringProperty newPasswordValue     = new SimpleStringProperty("");
    private final StringProperty confirmPasswordValue = new SimpleStringProperty("");

    // Independent visibility flags
    private final BooleanProperty newPasswordVisible     = new SimpleBooleanProperty(false);
    private final BooleanProperty confirmPasswordVisible = new SimpleBooleanProperty(false);

    // ============================
    // Controller Lifecycle
    // ============================
    public void initialize() {
        // remove focus outline on text fields
        emailTextField.setFocusTraversable(false);
        newPasswordPasswordField.setFocusTraversable(false);
        confirmNewPasswordPasswordField.setFocusTraversable(false);

        // Start on email/otp step
        passwordForm.setVisible(false);

        otpTextField.setDisable(true);
        verifyOTPBtn.setDisable(true);

        // Setup toggles for both password fields (no manual sync needed)
        setupPasswordToggle(
                newPasswordPasswordField,
                newPasswordTextField,
                eyeSlashToggleButton,
                newPasswordValue,
                newPasswordVisible
        );

        setupPasswordToggle(
                confirmNewPasswordPasswordField,
                confirmNewPasswordTextField,
                eyeSlashToggleButton1,
                confirmPasswordValue,
                confirmPasswordVisible
        );

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
            SnackBar.show(SnackBarType.ERROR, "Email Required", emailError, SNACK_DURATION);
            return;
        }

        String email = emailInput.trim();
        sendOTPBtn.setDisable(true);

        // Determine user (without revealing to attacker)
        int uid = UserDAO.isEmailExists(email);
        resetUserId = uid > 0 ? uid : null;

        // Allow OTP entry regardless (prevents easy email enumeration)
        otpTextField.setDisable(false);
        verifyOTPBtn.setDisable(false);

        // If email not found, pretend it worked (optional but better security UX)
        if (resetUserId == null) {
            sendOTPBtn.setDisable(false);
            SnackBar.show(SnackBarType.WARNING, "Check your email", "If the email is registered, an OTP was sent.", SNACK_DURATION);
            return;
        }

        CompletableFuture<MailResultSet> task = MailHelper.sendMail(email, "Cake Shop System password reset request.");
        task.whenComplete((result, ex) -> Platform.runLater(() -> {
            sendOTPBtn.setDisable(false);

            if (ex != null) {
                SnackBar.show(SnackBarType.ERROR, "Failed", "Could not send OTP email.", SNACK_DURATION);
                return;
            }

            SnackBar.show(
                    result.success() ? SnackBarType.SUCCESS : SnackBarType.ERROR,
                    result.success() ? "Check your email" : "Failed",
                    result.success() ? "" : result.message(),
                    SNACK_DURATION
            );
        }));
    }

    // ============================
    // Step 2: Verify OTP Code
    // ============================
    private void handleVerifyOtp(ActionEvent e) {
        String otpError = validateOtp(otpTextField.getText());
        if (otpError != null) {
            SnackBar.show(SnackBarType.ERROR, "Invalid OTP", otpError, SNACK_DURATION);
            return;
        }

        // Security: don't reveal whether email exists
        if (resetUserId == null) {
            SnackBar.show(SnackBarType.ERROR, "Invalid OTP", "Invalid or expired OTP.", SNACK_DURATION);
            return;
        }

        boolean ok = OneTimePasswordDAO.isOtpValid(resetUserId, otpTextField.getText().trim());
        if (!ok) {
            SnackBar.show(SnackBarType.ERROR, "Invalid OTP", "Invalid or expired OTP.", SNACK_DURATION);
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
            SnackBar.show(SnackBarType.ERROR, "Error", "Please verify OTP first.", SNACK_DURATION);
            return;
        }

        String p1 = newPasswordValue.get();
        String p2 = confirmPasswordValue.get();

        String passError = Validator.validatePassword(p1);
        if (passError != null) {
            SnackBar.show(SnackBarType.ERROR, "Weak Password", passError, SNACK_DURATION);
            return;
        }

        String matchError = Validator.matchPassword(p1, p2);
        if (matchError != null) {
            SnackBar.show(SnackBarType.ERROR, "Mismatch", matchError, SNACK_DURATION);
            return;
        }

        confirmBtn.setDisable(true);

        AuthResult res = UserDAO.updatePassword(resetUserId, p1);
        if (res.success()) {
            SnackBar.show(SnackBarType.SUCCESS, "Success", res.message(), SNACK_DURATION);
            ChangeScene.switchScene("LoginForm.fxml", "Cake Shop System | Login Form");
        } else {
            SnackBar.show(SnackBarType.ERROR, "Failed", res.message(), SNACK_DURATION);
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
    // Password Toggle Helper
    // ============================
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

    // ============================
    // UI Animations
    // ============================
    private void slideIn(VBox vBox) {
        vBox.setVisible(true);

        FadeTransition fadeIn = new FadeTransition(ANIM_DURATION, vBox);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);

        TranslateTransition slide = new TranslateTransition(ANIM_DURATION, vBox);
        slide.setFromX(-100);
        slide.setToX(0);

        new ParallelTransition(fadeIn, slide).play();
    }

    private void slideAway(VBox vBox) {
        FadeTransition fadeOut = new FadeTransition(ANIM_DURATION, vBox);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        TranslateTransition slide = new TranslateTransition(ANIM_DURATION, vBox);
        slide.setFromX(0);
        slide.setToX(100);

        ParallelTransition transition = new ParallelTransition(fadeOut, slide);
        transition.setOnFinished(ev -> vBox.setVisible(false));
        transition.play();
    }
}
