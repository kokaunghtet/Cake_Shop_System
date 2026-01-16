package com.cakeshopsystem.controllers.product;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class CustomOrderDetailController {

    @FXML
    private Button btnCancel;

    @FXML
    private Button btnConfirm;

    @FXML
    private ComboBox<?> comboFlavour;

    @FXML
    private ComboBox<?> comboTopping;

    @FXML
    private ImageView customCakeImageView;

    @FXML
    private VBox customOrderDetailPane;

    @FXML
    private DatePicker datePickerPickUpDate;

    @FXML
    private Label lblFlavour;

    @FXML
    private Label lblMessage;

    @FXML
    private Label lblPickUpDate;

    @FXML
    private Label lblSize;

    @FXML
    private Label lblTopping;

    @FXML
    private TextArea textAreaMessage;

    @FXML
    private ToggleGroup toggleSize;

}
