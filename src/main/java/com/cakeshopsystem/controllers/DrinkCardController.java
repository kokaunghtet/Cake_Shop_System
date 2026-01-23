package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.dao.DrinkDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class DrinkCardController {

    public VBox drinkCardVbox;
    public Label drinkName;
    public RadioButton hotOption;
    public RadioButton coldOption;
    public Label drinkPrice;
    public Button decreaseBtn;
    public Label quantityLabel;
    public Button increaseBtn;
    public Button addToCartBtn;
    public ImageView drinkImage;

    public void setData(Product product) {
        var drinks = DrinkDAO.getDrinksByProductId(product.getProductId());
        double base = product.getPrice();
        double hot = base + drinks.getFirst().getPriceDelta();
        double cold = base + drinks.getLast().getPriceDelta();

        drinkName.setText(String.valueOf(product.getProductName()));
        drinkPrice.setText(String.valueOf(base));
    }

}
