package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Cake;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.cache.CakeCache;
import com.cakeshopsystem.utils.cache.ProductCache;
import com.cakeshopsystem.utils.constants.CakeType;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

public class DiyOrderController {
    @FXML
    private StackPane mainStackPane;
    @FXML
    private ScrollPane mainScrollPane;
    @FXML
    private FlowPane mainFlowPane;
    @FXML
    private Button btnCreateDiyOrder;

    private static final String PRODUCT_CARD_FXML = "/views/ProductCard.fxml";

    @FXML
    public void initialize() {
        loadCakes();
    }

    private void loadCakes() {
        ObservableList<Product> cakeList = ProductCache.getProductsByCategoryId(1);

        for (Product p : cakeList) {

            Cake cake = CakeCache.getCakeByProductId(p.getProductId());
            if (cake == null) continue;

            if(!cake.isDiyAllowed()) continue;

            if (cake.getCakeType() != CakeType.PREBAKED) continue;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setProductData(p);
//                controller.setOnCardDoubleClick(this::openDiyOrderForm);

                mainFlowPane.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading cake card: ");
                err.printStackTrace();
            }
        }
    }
}
