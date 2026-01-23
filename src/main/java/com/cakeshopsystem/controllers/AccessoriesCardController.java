package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Product;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class AccessoriesCardController {

    public VBox accessoryCardVbox;
    public ImageView accessoryImage;
    public Label accessoryName;
    public Label accessoryPrice;
    public Button terminus;
    public Label quantityLabel;
    public Button btnplus;
    public Button addToCartBtn;

    public void setData(Product product) {
        accessoryName.setText(String.valueOf(product.getProductName()));
        accessoryPrice.setText(String.valueOf(product.getPrice()));
    }
}
