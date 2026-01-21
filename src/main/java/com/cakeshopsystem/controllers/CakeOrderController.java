
package com.cakeshopsystem.controllers;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class CakeOrderController {

    @FXML
    private ScrollPane accessoriesScrollPane;

    @FXML
    private Button btnAddToCardAll;

    @FXML
    private ScrollPane cakeScrollPane;

    @FXML
    private ScrollPane dessertsScrollPane;

    @FXML
    private ScrollPane discountItemsScrollPane;

    @FXML
    private ScrollPane drinkScrollPane;

    @FXML
    private Label lblAccessories;

    @FXML
    private Label lblCake;

    @FXML
    private Label lblDesserts;

    @FXML
    private Label lblDiscountItems;

    @FXML
    private Label lblDrink;

    @FXML
    private ScrollPane productsCardScrollPane;

    @FXML
    private VBox productsCardVbox;

    @FXML
    private AnchorPane productsCard_root;

    @FXML
    private VBox vboxAccessories;

    @FXML
    private VBox vboxCake;

    @FXML
    private VBox vboxDesserts;

    @FXML
    private VBox vboxDiscountItem;

    @FXML
    private VBox vboxDrink;

    @FXML
    public void initialize(){

    }

    private void loadProductMenu(){
    if(productsCardVbox==null) return;
    productsCardVbox.getChildren().clear();

    }

    @FXML
    void clickAddToCardAll(ActionEvent event) {

    }


}

