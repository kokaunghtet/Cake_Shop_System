package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Member;
import com.cakeshopsystem.utils.cache.MemberCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.MemberDAO;
import javafx.animation.PauseTransition;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class RegisterMemberController {

    // =====================================
    // CONSTANTS
    // =====================================
    private static final Duration ERROR_DURATION = Duration.seconds(2);
    private static final Duration SUCCESS_DURATION = Duration.seconds(2);
    private static final Duration CLOSE_DELAY = Duration.millis(200);

    private static final Pattern NAME_PATTERN = Pattern.compile("^[a-zA-Z\\s]+$");
    private static final Pattern PHONE_LOCAL = Pattern.compile("^09\\d{7,9}$");
    private static final Pattern PHONE_INTL = Pattern.compile("^\\+959\\d{7,9}$");

    // =====================================
    // FXML UI COMPONENTS
    // =====================================
    @FXML
    private Button btnBack;
    @FXML
    private VBox confirmationBox;
    @FXML
    private VBox registerBox;

    @FXML
    private Button btnConfirm;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnRegister;

    @FXML
    private TextField txtName;
    @FXML
    private TextField txtPhone;

    // =====================================
    // STATE & CALLBACKS
    // =====================================
    private int qualifiedOrderId;

    private Runnable onRegistered;
    private Runnable onSkipped;

    // =====================================
    // LIFECYCLE
    // =====================================
    @FXML
    public void initialize() {
        btnCancel.setOnAction(e -> closeThen(onSkipped));
        btnConfirm.setOnAction(e -> showRegisterForm());
        btnBack.setOnAction(e -> showConfirmation());
        btnRegister.setOnAction(e -> doRegister());
    }

    // =====================================
    // DATA & CALLBACK SETTERS
    // =====================================
    public void setData(int qualifiedOrderId) {
        this.qualifiedOrderId = qualifiedOrderId;
    }

    public void setOnRegistered(Runnable r) {
        this.onRegistered = r;
    }

    public void setOnSkipped(Runnable r) {
        this.onSkipped = r;
    }

    // =====================================
    // REGISTRATION FLOW
    // =====================================
    private void doRegister() {
        String memberName = validateName();
        if (memberName == null) return;

        String memberPhone = validateAndNormalizePhone();
        if (memberPhone == null) return;

        ObservableList<Member> members = MemberCache.getMembersList();
        for(Member m: members) {
            if(m.getPhone().equals(memberPhone)) {
                SnackBar.show(SnackBarType.INFO, "Already Register", "This phone number is already registered as a member", Duration.seconds(2));
                return;
            }
        }

        btnRegister.setDisable(true);

        Member newMember = new Member(
                memberName,
                memberPhone,
                qualifiedOrderId,
                LocalDateTime.now()
        );

        boolean ok = MemberDAO.insertMember(newMember);

        if (!ok) {
            btnRegister.setDisable(false);
            showError("Failed", "Cannot register member (duplicate phone/order?)");
            return;
        }

        showSuccess();
        closeThen(onRegistered);
    }

    // =====================================
    // INPUT VALIDATION
    // =====================================
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

//    private String validatePhone() {
//        String v = trimmed(txtPhone);
//
//        if (v.isEmpty()) {
//            showError("Invalid", "Member Phone Number is required.");
//            txtPhone.requestFocus();
//            return null;
//        }
//
//        if (PHONE_LOCAL.matcher(v).matches() || PHONE_INTL.matcher(v).matches()) {
//            return v;
//        }
//
//        showError("Invalid", "Use 09XXXXXXXXX or +959XXXXXXXXX.");
//        txtPhone.requestFocus();
//        return null;
//    }

    private String validateAndNormalizePhone() {
        String v = trimmed(txtPhone).replaceAll("\\s+", ""); // optional: remove spaces

        if (v.isEmpty()) {
            showError("Invalid", "Member Phone Number is required.");
            txtPhone.requestFocus();
            return null;
        }

        if (PHONE_LOCAL.matcher(v).matches()) {
            String normalized = "+959" + v.substring(2);
            return normalized;
        }

        if (PHONE_INTL.matcher(v).matches()) {
            return v;
        }

        showError("Invalid", "Use 09XXXXXXXXX or +959XXXXXXXXX.");
        txtPhone.requestFocus();
        return null;
    }


    // =====================================
    // VIEW TOGGLING
    // =====================================
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

    // =====================================
    // POPUP CONTROL
    // =====================================
    private void closeThen(Runnable cb) {
        MainController.handleClosePopupContent();

        PauseTransition pt = new PauseTransition(CLOSE_DELAY);
        pt.setOnFinished(ev -> {
            if (cb != null) cb.run();
        });
        pt.play();
    }

    // =====================================
    // UTILITY HELPERS
    // =====================================
    private static String trimmed(TextField tf) {
        String t = tf.getText();
        return (t == null) ? "" : t.trim();
    }

    // =====================================
    // FEEDBACK
    // =====================================
    private void showError(String title, String message) {
        SnackBar.show(SnackBarType.ERROR, title, message, ERROR_DURATION);
    }

    private void showSuccess() {
        SnackBar.show(SnackBarType.SUCCESS, "Success", "Member registered.", SUCCESS_DURATION);
    }
}
