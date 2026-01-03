package com.cakeshopsystem.controllers;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class MainController {

    @FXML
    private Hyperlink adminProductLink;

    @FXML
    private Hyperlink bookingLink;

    @FXML
    private Hyperlink customerOrderLink;

    @FXML
    private Hyperlink dashboardLink;

    @FXML
    private BorderPane mainContent;

    @FXML
    private StackPane mainStackPane;

    @FXML
    private Hyperlink memberLink;

    @FXML
    private FontAwesomeIconView moonIcon;

    @FXML
    private ImageView profileImageView;

    @FXML
    private Label roleLabel;

    @FXML
    private FontAwesomeIconView settingIcon;

    @FXML
    private Hyperlink staffProductLink;

    @FXML
    private HBox topBar;

    @FXML
    private Label usenameLabel;

    @FXML
    private Hyperlink userLink;

}
