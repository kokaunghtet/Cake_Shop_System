
package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.OrderItem;
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

public class CakeCardController{

    @FXML
    private Button btnAddtoCart;

    @FXML
    private ImageView cakeImage;

    @FXML
    private Label cakeName;

    @FXML
    private Label cakeSize;

    @FXML
    private Label cakePrice;

    @FXML
    private VBox cardPane_vbox;

    @FXML
    private Button decreaseBtn;

    @FXML
    private Button increaseBtn;

    @FXML
    private Label quantityLabel;
    private OrderItem cakeItem;
    private Runnable onQuantityChanged;

    public void setData(OrderItem item,Runnable onQuantityChanged){
        this.cakeItem = item;
        this.onQuantityChanged = onQuantityChanged;

        cakeName.setText(item.getName());
        cakePrice.setText(item.getPrice() + " MMK");

        double width=150;
        double height=200;
        cakeImage.setFitWidth(width);
        cakeImage.setFitHeight(height);
        cakeImage.setPreserveRatio(false);
        cakeImage.setSmooth(true);

        Image img=null;

        Log LoggerUtil = null;
        try (InputStream is = getClass().getResourceAsStream("/images/" + item.getImageURL())) {
            if (is != null) {
                img = new Image(is);
            } else {
                LoggerUtil.logWarn(logger);
            }
        } catch (Exception e) {
            LoggerUtil.logError(logger);
        }
    }

//    private void updatequantityLabel(){
//        quantityLabel.setText(String.valueOf(cakeItem.getQuantity()));
//    }
//
//    @FXML
//    void clickdecreaseBtn(ActionEvent event) {
//        if (cakeItem.getQuantity() > 0) {
//            cakeItem.setQuantity(cakeItem.getQuantity() - 1);
//            updatequantityLabel();
//            if (onQuantityChanged != null) onQuantityChanged.run();
//        }
//    }
//
//    @FXML
//    void clickincreaseBtn(ActionEvent event) {
//        cakeItem.setQuantity(cakeItem.getQuantity() + 1);
//        updatequantityLabel();
//        if (onQuantityChanged != null) onQuantityChanged.run();
//    }
//
//    public void resetquantityLabel() {
//        if (cakeItem != null) {
//            cakeItem.setQuantity(0);
//            quantityLabel.setText("0");
//        }
//    }
//
//    @Override
//    public void initialize(URL location, ResourceBundle resources) {
//
//    }
}

