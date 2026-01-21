package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Cake;
import com.cakeshopsystem.models.OrderItem;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.cache.CakeCache;
import com.mysql.cj.log.Log;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.InputStream;
import java.net.URL;
import java.util.ResourceBundle;

import static com.mysql.cj.conf.PropertyKey.logger;

public class CakeCardController {

    @FXML
    public VBox cakeCardVbox;
    @FXML
    public Button addToCartBtn;
    @FXML
    public Label diyAvailable;
    @FXML
    private ImageView cakeImage;
    @FXML
    private Label cakeName;
    @FXML
    private Label cakeSize;
    @FXML
    private Label cakePrice;
    @FXML
    private Button decreaseBtn;
    @FXML
    private Button increaseBtn;
    @FXML
    private Label quantityLabel;

    public void setData(Product product) {
//        Image cakeImg = new Image("images/yummycake.gif");
//        cakeImage.setImage(cakeImg);
        diyAvailable.setText(String.valueOf(CakeCache.getCakeByProductId(product.getProductId()).isDiyAllowed()));


        cakeName.setText(String.valueOf(product.getProductName()));
        cakePrice.setText(String.valueOf(product.getPrice()));
    }
}

