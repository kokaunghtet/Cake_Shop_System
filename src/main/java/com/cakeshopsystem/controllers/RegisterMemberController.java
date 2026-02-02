package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Member;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.MemberDAO;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class RegisterMemberController {

    private static final Duration ERROR_DURATION   = Duration.seconds(2);
    private static final Duration SUCCESS_DURATION = Duration.seconds(2);
    private static final Duration CLOSE_DELAY      = Duration.millis(200);

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");

    private static final Pattern PHONE_LOCAL = Pattern.compile("^09\\d{7,9}$");
    private static final Pattern PHONE_INTL  = Pattern.compile("^\\+959\\d{7,9}$");

    @FXML private Button btnBack;
    @FXML private VBox confirmationBox;
    @FXML private VBox registerBox;

    @FXML private Button btnConfirm;
    @FXML private Button btnCancel;
    @FXML private Button btnRegister;

    @FXML private TextField txtName;
    @FXML private TextField txtPhone;

    private int qualifiedOrderId;

    private Runnable onRegistered;
    private Runnable onSkipped;

    @FXML
    public void initialize() {
        btnCancel.setOnAction(e -> closeThen(onSkipped));
        btnConfirm.setOnAction(e -> showRegisterForm());
        btnBack.setOnAction(e -> showConfirmation());
        btnRegister.setOnAction(e -> doRegister());
    }

    public void setData(int qualifiedOrderId) {
        this.qualifiedOrderId = qualifiedOrderId;
    }

    public void setOnRegistered(Runnable r) {
        this.onRegistered = r;
    }

    public void setOnSkipped(Runnable r) {
        this.onSkipped = r;
    }

    private void doRegister() {
        String memberName  = validateName();
        if (memberName == null) return;

        String memberPhone = validatePhone();
        if (memberPhone == null) return;

        btnRegister.setDisable(true);

        Member newMember = new Member(memberName, memberPhone, qualifiedOrderId, LocalDateTime.now());
        boolean ok = MemberDAO.insertMember(newMember);

        if (!ok) {
            btnRegister.setDisable(false);
            showError("Failed", "Cannot register member (duplicate phone/order?)");
            return;
        }

        showSuccess("Success", "Member registered.");
        closeThen(onRegistered);
    }

    private String validateName() {
        String v = trimmed(txtName);

        if (v.isEmpty()) {
            showError("Invalid", "Member Name is required.");
            txtName.requestFocus();
            return null;
        }

        if (!NAME_PATTERN.matcher(v).matches()) {
            showError("Invalid Input", "Member Name must contain only letters.");
            txtName.requestFocus();
            return null;
        }

        return v;
    }

    private String validatePhone() {
        String v = trimmed(txtPhone);

        if (v.isEmpty()) {
            showError("Invalid", "Member Phone Number is required.");
            txtPhone.requestFocus();
            return null;
        }

        if (PHONE_LOCAL.matcher(v).matches() || PHONE_INTL.matcher(v).matches()) {
            return v;
        }

        showError("Invalid", "Use 09XXXXXXXXX or +959XXXXXXXXX.");
        txtPhone.requestFocus();
        return null;
    }

    private void showRegisterForm() {
        confirmationBox.setVisible(false);
        confirmationBox.setManaged(false);

        registerBox.setVisible(true);
        registerBox.setManaged(true);

        txtName.requestFocus();
    }

    private void showConfirmation() {
        registerBox.setVisible(false);
        registerBox.setManaged(false);

        confirmationBox.setVisible(true);
        confirmationBox.setManaged(true);
    }

    private void closeThen(Runnable cb) {
        MainController.handleClosePopupContent();
        PauseTransition pt = new PauseTransition(CLOSE_DELAY);
        pt.setOnFinished(ev -> {
            if (cb != null) cb.run();
        });
        pt.play();
    }

    private static String trimmed(TextField tf) {
        String t = tf.getText();
        return (t == null) ? "" : t.trim();
    }

    private void showError(String title, String message) {
        SnackBar.show(SnackBarType.ERROR, title, message, ERROR_DURATION);
    }

    private void showSuccess(String title, String message) {
        SnackBar.show(SnackBarType.SUCCESS, title, message, SUCCESS_DURATION);
    }
}
