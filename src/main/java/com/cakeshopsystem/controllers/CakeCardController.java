package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.cache.CakeCache;
import com.cakeshopsystem.utils.dao.ProductDAO;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.util.Objects;

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
        cakeName.setText(product.getProductName());
        if(Objects.requireNonNull(CakeCache.getCakeByProductId(product.getProductId())).isDiyAllowed()) {
            diyAvailable.setText("DIY Available");
        } else {
            diyAvailable.setVisible(false);
            diyAvailable.setManaged(false);
        }
        cakePrice.setText(String.valueOf(product.getPrice()));
    }

    public void setData(Inventory inventory) {
        diyAvailable.setVisible(false);
        diyAvailable.setManaged(false);
        cakeName.setText(String.valueOf(Objects.requireNonNull(ProductDAO.getProductById(inventory.getProductId())).getProductName()));
        cakePrice.setText(String.valueOf(Objects.requireNonNull(ProductDAO.getProductById(inventory.getProductId())).getPrice()*0.4));
    }
}

