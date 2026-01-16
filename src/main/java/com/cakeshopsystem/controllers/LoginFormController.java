package com.cakeshopsystem.controllers;

import com.cakeshopsystem.utils.AuthResult;
import com.cakeshopsystem.utils.ChangeScene;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.UserDAO;
import com.cakeshopsystem.utils.validators.Validator;
import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginFormController {
    private int emptyAttemptCount = 0;
    private PauseTransition loginDisableTimer;
    private final int DISABLE_SECONDS = 15;

    @FXML
    private StackPane loginStackPane;

    @FXML
    private FontIcon eyeSlashToggleButton;

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
        usernameTextField.setText("John");
        pswPasswordField.setText("John123!@#");

        usernameTextField.requestFocus();

        // --- Make hidden fields not take up layout space ---
        pswPasswordField.managedProperty().bind(pswPasswordField.visibleProperty()); // If not visible, don't reserve space
        txtPasswordField.managedProperty().bind(txtPasswordField.visibleProperty()); // If not visible, don't reserve space

        // --- Ensure initial state is consistent ---
        pswPasswordField.setVisible(true);    // Start with hidden-password field visible
        txtPasswordField.setVisible(false);   // Start with plain-text field hidden

        errorMessage.managedProperty().bind(errorMessage.visibleProperty());
        errorMessage.visibleProperty().bind(errorMessage.textProperty().isNotEmpty());

        // --- Keep both password fields synced (single path, avoids redundant loops) ---
        pswPasswordField.textProperty().addListener((obs, oldValue, newValue) -> syncPasswordFields(newValue)); // Typing in PasswordField
        txtPasswordField.textProperty().addListener((obs, oldValue, newValue) -> syncPasswordFields(newValue)); // Typing in TextField

        // Reset attempts as soon as user types something (optional but recommended)
        usernameTextField.textProperty().addListener((o, ov, nv) -> resetEmptyAttemptsIfAnyInput());
        pswPasswordField.textProperty().addListener((o, ov, nv) -> resetEmptyAttemptsIfAnyInput());
        txtPasswordField.textProperty().addListener((o, ov, nv) -> resetEmptyAttemptsIfAnyInput());

        eyeSlashToggleButton.setOnMouseClicked(event -> togglePasswordVisibility());

        // --- Make Enter key submit the form (default button behavior) ---
        loginButton.setDefaultButton(true);                 // Treat this as the default action when user presses Enter

        // --- Also submit when user presses Enter inside input fields ---
        usernameTextField.setOnAction(e -> attemptLogin());  // Enter on username field triggers login
        pswPasswordField.setOnAction(e -> attemptLogin());   // Enter on hidden password field triggers login
        txtPasswordField.setOnAction(e -> attemptLogin());   // Enter on visible password field triggers login

        // --- Normal click/tap behavior ---
        loginButton.setOnAction(e -> attemptLogin());       // Clicking the login button triggers login
    }

    private void attemptLogin() {
        if (isBothFieldsEmpty()) {
            handleEmptySubmitAttempt();
            return;
        }

        // If they entered something, proceed with normal login
        emptyAttemptCount = 0;
        handleLogin();
    }

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
            emptyAttemptCount = 0; // reset after penalty
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
        // Reset counter if the user has started typing in either field
        String user = usernameTextField.getText() == null ? "" : usernameTextField.getText().trim();
        String pass = getPasswordValue() == null ? "" : getPasswordValue().trim();

        if (!user.isEmpty() || !pass.isEmpty()) {
            emptyAttemptCount = 0;
        }
    }

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("login-worker");
        return t;
    });

    private void handleLogin() {
        // --- Reset previous feedback (fresh attempt) ---
        clearLoginFeedback(); // Clear label texts + remove error styling

        // --- Read current input values (trimmed & null-safe) ---
        String usernameValue = safeTrim(usernameTextField.getText()); // Username from UI
        String passwordValue = safeTrim(getPasswordValue());          // Password from visible/active field

        // --- Validate on FX thread (before background work) ---
        String usernameError = Validator.validateUsername(usernameValue);
        String passwordError = Validator.validateLoginPassword(passwordValue);

        // --- Validate inputs and stop early if invalid ---
        if (!validateLoginInputs(usernameValue, passwordValue)) {                     // Validate + show messages/snackbar
            return;                                                                   // Do not continue to background auth
        }

        setLoginUiBusy(true);

        // --- Run authentication in background thread ---
        startLoginAsync(usernameValue, passwordValue); // Build + run Task, wire handlers, and submit to executor
    }

    @FXML
    private void handleForgotPassword() {
//        ChangeScene("ForgotPassword.fxml", "Forgot Password");
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

    /**
     * Ensures the login UI is always restored when the task finishes,
     * regardless of whether it succeeds, fails, or gets cancelled.
     */
    private void wireBusyState(Task<?> task) {
        // Listen for state changes so we can reliably restore the UI.
        task.stateProperty().addListener((obs, oldState, newState) -> {
            switch (newState) {
                case SUCCEEDED, FAILED, CANCELLED -> setLoginUiBusy(false); // Always re-enable UI when task ends
                default -> { /* Ignore other states (READY/RUNNING/SCHEDULED) */ }
            }
        });
    }

    private String getPasswordValue() {
        return isPasswordVisible ? txtPasswordField.getText() : pswPasswordField.getText();
    }

    private static String safeTrim(String s) {
        return s == null ? "" : s.trim();
    }

    @FXML
    private void togglePasswordVisibility() {
        // --- Capture current state before switching ---
        final boolean currentlyVisible = isPasswordVisible; // Snapshot current visibility state

        // Read current text + caret from whichever field is currently active
        final String currentText = getPasswordValue();      // Current password string from active field
        final int caret = currentlyVisible
                ? txtPasswordField.getCaretPosition()       // Caret position when plain-text is active
                : pswPasswordField.getCaretPosition();      // Caret position when password field is active

        // --- Flip state ---
        isPasswordVisible = !isPasswordVisible;             // Toggle visibility flag

        // --- Apply visibility (and managed binding handles layout space) ---
        pswPasswordField.setVisible(!isPasswordVisible);    // Show PasswordField when NOT visible mode
        txtPasswordField.setVisible(isPasswordVisible);     // Show TextField when visible mode

        // --- Sync text into the field that is becoming visible ---
        if (isPasswordVisible) {
            txtPasswordField.setText(currentText);          // Ensure plain-text field matches current value
            txtPasswordField.requestFocus();                // Move focus so user can keep typing
            txtPasswordField.positionCaret(Math.min(caret, currentText.length())); // Restore caret safely
        } else {
            pswPasswordField.setText(currentText);          // Ensure PasswordField matches current value
            pswPasswordField.requestFocus();                // Move focus so user can keep typing
            pswPasswordField.positionCaret(Math.min(caret, currentText.length())); // Restore caret safely
        }

        // Changes between eye & eye-slash icon
        eyeSlashToggleButton.setIconLiteral(isPasswordVisible ? "fas-eye" : "fas-eye-slash");
    }

    /**
     * Keeps PasswordField and TextField values in sync without redundant updates.
     * Updates the target field only if its value is different.
     */
    private void syncPasswordFields(String newValue) {
        // Update PasswordField if needed
        if (!Objects.equals(pswPasswordField.getText(), newValue)) {
            pswPasswordField.setText(newValue); // Keep hidden field in sync
        }

        // Update TextField if needed
        if (!Objects.equals(txtPasswordField.getText(), newValue)) {
            txtPasswordField.setText(newValue); // Keep visible field in sync
        }
    }

    /**
     * Clears login feedback labels (text + error styling) before a new login attempt.
     */
    private void clearLoginFeedback() {
        clearLabel(errorMessage);      // Clear top-level error label
        clearLabel(usernameMessage);   // Clear username validation label
        clearLabel(passwordMessage);   // Clear password validation label
    }

    /**
     * Clears a label's text and removes the "error-message" style if present.
     */
    private void clearLabel(Label label) {
        label.setText("");                         // Remove visible text
        label.getStyleClass().remove("error-message"); // Remove red/error styling if it was applied
    }

    /**
     * Validates username & password inputs and updates UI validation labels.
     * Returns true if inputs are valid and login can proceed.
     */
    private boolean validateLoginInputs(String usernameValue, String passwordValue) {
        // --- Validate on FX thread (before background work) ---
        String usernameError = Validator.validateUsername(usernameValue);            // Validate username rules
        String passwordError = Validator.validateLoginPassword(passwordValue);       // Validate login password rules

        // --- Show validation feedback beside fields ---
        applyValidation(usernameMessage, usernameError);                             // Show username error (if any)
        applyValidation(passwordMessage, passwordError);                             // Show password error (if any)

        // --- Special case: both empty -> show a single snackbar message ---
        boolean usernameEmpty = usernameValue.isBlank();                             // True if user left username blank
        boolean passwordEmpty = passwordValue.isBlank();                             // True if user left password blank
        boolean bothFieldEmpty = usernameEmpty && passwordEmpty;                     // True if user left both fields blank

        if (bothFieldEmpty) {
            SnackBar.show(
                    SnackBarType.ERROR,                                              // Error type snackbar
                    "Missing Information",                                           // Snackbar title
                    "Please enter both your username and password",                  // Snackbar message
                    Duration.seconds(3)                                              // Auto-hide after 3 seconds
            );
            return false;                                                            // Stop login
        }

        // --- If either field fails validation, stop login ---
        return usernameError == null && passwordError == null;                       // Proceed only when both are valid
    }

    /**
     * Builds and runs the background login task, then updates the UI on success/failure.
     */
    private void startLoginAsync(String usernameValue, String passwordValue) {
        setLoginUiBusy(true); // Disable UI immediately for responsiveness

        Task<AuthResult> loginTask = createLoginTask(usernameValue, passwordValue); // Build background task
        wireBusyState(loginTask); // Guarantee UI is restored on success/failure/cancel

        // --- Runs on FX thread when task succeeds ---
        loginTask.setOnSucceeded(event -> {
            handleLoginSucceeded(usernameValue, loginTask.getValue()); // Handle AuthResult and navigate/show messages
        });

        // --- Runs on FX thread when task fails ---
        loginTask.setOnFailed(event -> {
            handleLoginFailed(loginTask.getException()); // Handle unexpected exception and show fallback message
        });

        executor.submit(loginTask); // Submit task to background thread
    }

    /**
     * Creates a Task that authenticates the user in a background thread.
     */
    private Task<AuthResult> createLoginTask(String usernameValue, String passwordValue) {
        return new Task<>() {
            @Override
            protected AuthResult call() throws Exception {
                // Authenticate against the database (do NOT touch UI from this thread)
                return UserDAO.authenticateUser(usernameValue, passwordValue);
            }
        };
    }

    /**
     * Handles the AuthResult when authentication succeeds (task completed normally).
     */
    private void handleLoginSucceeded(String usernameValue, AuthResult authResult) {
        // If authentication success == true, switch to main screen
        if (authResult != null && Boolean.TRUE.equals(authResult.success())) {
            ChangeScene.switchScene("Main.fxml", "Main Page"); // Navigate to main page
            SnackBar.show(SnackBarType.SUCCESS, "Welcome, " + usernameValue, "", Duration.seconds(3)); // Welcome toast
            return; // Stop here (no error to show)
        }

        // Otherwise show the returned message (or a safe fallback)
        String msg = (authResult == null || authResult.message() == null)
                ? "Login failed."
                : authResult.message();

        errorMessage.setText(msg); // Show message under the form
        errorMessage.getStyleClass().setAll("error-message"); // Apply error styling
        SnackBar.show(SnackBarType.ERROR, msg, "", Duration.seconds(3)); // Show snackbar error
    }

    /**
     * Handles unexpected errors (task threw an exception).
     */
    private void handleLoginFailed(Throwable ex) {
        if (ex != null) {
            ex.printStackTrace(); // Log the real error for debugging
        }

        String msg = "Something went wrong. Please try again."; // User-friendly fallback
        errorMessage.setText(msg); // Show message under the form
        errorMessage.getStyleClass().setAll("error-message"); // Apply error styling
        SnackBar.show(SnackBarType.ERROR, msg, "", Duration.seconds(3)); // Show snackbar error
    }
}