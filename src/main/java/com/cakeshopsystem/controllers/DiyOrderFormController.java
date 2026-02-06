package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Cake;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.cache.CakeCache;
import com.cakeshopsystem.utils.cache.ProductCache;
import com.cakeshopsystem.utils.components.SnackBar;
import com.cakeshopsystem.utils.constants.SnackBarType;
import com.cakeshopsystem.utils.dao.DiyCakeBookingDAO;
import com.cakeshopsystem.utils.dao.MemberDAO;
import com.cakeshopsystem.utils.services.CartService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.regex.Pattern;

public class DiyOrderFormController {

    // =====================================
    // FXML UI COMPONENTS
    // =====================================
    @FXML private VBox vbSearchMember;
    @FXML private TextField tfPhoneNumber;
    @FXML private Button btnCancel;
    @FXML private Button btnSearch;

    @FXML private VBox vbDiyOrder;
    @FXML private DatePicker dbOrderDate;

    @FXML private ToggleGroup tgSessionTime;
    @FXML private RadioButton rbFirst;
    @FXML private RadioButton rbSecond;
    @FXML private RadioButton rbThird;

    @FXML private Label lblCakeName;
    @FXML private Button btnConfirm;

    // =====================================
    // STATE & CONSTANTS
    // =====================================
    private Product baseCakeProduct;
    private Integer memberId;

    private static final double DIY_SERVICE_FEE = 10000.0;
    private static final Pattern PHONE_LOCAL = Pattern.compile("^09\\d{9}$");
    private static final Pattern PHONE_INTL  = Pattern.compile("^\\+959\\d{9}$");

    // =====================================
    // LIFECYCLE
    // =====================================
    @FXML
    public void initialize() {
        bindManagedToVisible(vbSearchMember, vbDiyOrder);

        vbSearchMember.setVisible(true);
        vbDiyOrder.setVisible(false);

        memberId = null;
        baseCakeProduct = null;

        blockPastDates(dbOrderDate);

        btnCancel.setOnAction(e -> MainController.handleClosePopupContent());
        btnSearch.setOnAction(e -> handleSearchMember());
        btnConfirm.setOnAction(e -> handleConfirm());

        tfPhoneNumber.setOnAction(e -> handleSearchMember());

        btnConfirm.setDisable(true);

        dbOrderDate.valueProperty().addListener((obs, o, n) -> {
            refreshSessionAvailability();
            updateConfirmEnabled();
        });

        tgSessionTime.selectedToggleProperty().addListener((obs, o, n) -> updateConfirmEnabled());
    }

    // =====================================
    // BASE CAKE BINDING
    // =====================================
    public void setBaseCake(Product p) {
        this.baseCakeProduct = p;
        if (lblCakeName != null) lblCakeName.setText(p == null ? "" : p.getProductName());
        updateConfirmEnabled();
    }

    // =====================================
    // MEMBER SEARCH
    // =====================================
    private void handleSearchMember() {
        String phone = validatePhone();
        if (phone == null) return;

        Integer id = MemberDAO.findMemberIdByPhone(phone);
        if (id == null) {
            showError("This phone number is not registered as a member. DIY session is members-only.");
            return;
        }

        this.memberId = id;

        vbSearchMember.setVisible(false);
        vbDiyOrder.setVisible(true);

        if (dbOrderDate.getValue() == null) dbOrderDate.setValue(LocalDate.now());

        refreshSessionAvailability();
        updateConfirmEnabled();
    }

    // =====================================
    // SESSION AVAILABILITY
    // =====================================
    private void refreshSessionAvailability() {
        LocalDate date = dbOrderDate.getValue();

        rbFirst.setDisable(false);
        rbSecond.setDisable(false);
        rbThird.setDisable(false);

        if (date == null) {
            tgSessionTime.selectToggle(null);
            return;
        }

        Set<LocalTime> taken = DiyCakeBookingDAO.getTakenSessionStarts(date);

        if (taken.contains(LocalTime.of(9, 0))) rbFirst.setDisable(true);
        if (taken.contains(LocalTime.of(12, 0))) rbSecond.setDisable(true);
        if (taken.contains(LocalTime.of(15, 0))) rbThird.setDisable(true);

        Toggle selected = tgSessionTime.getSelectedToggle();
        if (selected instanceof RadioButton rb && rb.isDisable()) {
            tgSessionTime.selectToggle(null);
        }
    }

    // =====================================
    // CONFIRM FLOW
    // =====================================
    private void handleConfirm() {
        if (baseCakeProduct == null) {
            showError("Please choose a cake first.");
            return;
        }
        if (memberId == null) {
            showError("Please search member first.");
            return;
        }

        LocalDate sessionDate = dbOrderDate.getValue();
        if (sessionDate == null) {
            showError("Date is required.");
            return;
        }

        LocalTime sessionStart = getSelectedSessionStart();
        if (sessionStart == null) {
            showError("Please choose a session time.");
            return;
        }

        if (!DiyCakeBookingDAO.isSlotAvailable(sessionDate, sessionStart)) {
            showError("That session is already booked. Choose another one.");
            refreshSessionAvailability();
            return;
        }

        Cake cake = CakeCache.getCakeByProductId(baseCakeProduct.getProductId());
        if (cake == null) {
            showError("Missing cake record for this product.");
            return;
        }

        double unitPrice = baseCakeProduct.getPrice() + DIY_SERVICE_FEE;

        String label = sessionLabel(sessionStart);
        String displayName = "DIY " + label + " - " + baseCakeProduct.getProductName();

        Product diySessionProduct = ProductCache.getProductsByCategoryId(1)
                .stream()
                .filter(p -> "DIY Session".equalsIgnoreCase(p.getProductName()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Missing 'DIY Session' product in DB"));

        CartService.getInstance().addOrReplaceDiyBooking(
                diySessionProduct,
                displayName,
                unitPrice,
                cake.getCakeId(),
                memberId,
                sessionDate,
                sessionStart
        );

        MainController.handleClosePopupContent();
    }

    // =====================================
    // VALIDATION & SELECTION HELPERS
    // =====================================
    private void updateConfirmEnabled() {
        boolean ok =
                baseCakeProduct != null
                        && memberId != null
                        && dbOrderDate.getValue() != null
                        && getSelectedSessionStart() != null;

        btnConfirm.setDisable(!ok);
    }

    private LocalTime getSelectedSessionStart() {
        Toggle t = tgSessionTime.getSelectedToggle();
        if (!(t instanceof RadioButton rb)) return null;

        if (rb == rbFirst && !rbFirst.isDisable()) return LocalTime.of(9, 0);
        if (rb == rbSecond && !rbSecond.isDisable()) return LocalTime.of(12, 0);
        if (rb == rbThird && !rbThird.isDisable()) return LocalTime.of(15, 0);

        return null;
    }

    private String sessionLabel(LocalTime start) {
        if (start == null) return "";
        if (start.equals(LocalTime.of(9, 0))) return "09:00-11:00";
        if (start.equals(LocalTime.of(12, 0))) return "12:00-14:00";
        if (start.equals(LocalTime.of(15, 0))) return "15:00-17:00";
        return start.toString();
    }

    // =====================================
    // DATE & UI UTILITIES
    // =====================================
    public static void blockPastDates(DatePicker datePicker) {
        LocalDate today = LocalDate.now();

        datePicker.setDayCellFactory(dp -> new DateCell() {
            @Override
            public void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) return;

                boolean isPast = item.isBefore(today);
                setDisable(isPast);
                setStyle(isPast ? "-fx-opacity: 0.4;" : "");
            }
        });

        datePicker.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null && newV.isBefore(today)) datePicker.setValue(null);
        });
    }

    private void bindManagedToVisible(Region... regions) {
        for (Region r : regions) {
            if (r == null) continue;
            r.managedProperty().bind(r.visibleProperty());
        }
    }

    private String validatePhone() {
        String v = normalizePhone(tfPhoneNumber.getText()); // trims + removes spaces

        if (v.isEmpty()) {
            showError("Phone number is required.");
            tfPhoneNumber.requestFocus();
            return null;
        }

        if (PHONE_LOCAL.matcher(v).matches() || PHONE_INTL.matcher(v).matches()) {
            return v;
        }

        showError("Use 09XXXXXXXXX or +959XXXXXXXXX.");
        tfPhoneNumber.requestFocus();
        return null;
    }

    private String normalizePhone(String raw) {
        if (raw == null) return "";
        return raw.trim().replaceAll("\\s+", "");
    }

    // =====================================
    // FEEDBACK
    // =====================================
    private void showError(String body) {
        SnackBar.show(SnackBarType.ERROR, "Failed!", body, Duration.seconds(2));
    }
}
