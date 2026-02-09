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
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;

public class DiyOrderController {

    @FXML private StackPane mainStackPane;
    @FXML private ScrollPane mainScrollPane;
    @FXML private FlowPane mainFlowPane;

    private static final String PRODUCT_CARD_FXML = "/views/ProductCard.fxml";
    private static final String DIY_ORDER_FORM_FXML = "/views/DiyOrderForm.fxml";

    @FXML
    public void initialize() {
        loadCakes();
    }

    private void openDiyOrderForm(Product selected) {
        if (selected == null) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(DIY_ORDER_FORM_FXML));
            Parent root = loader.load();

            DiyOrderFormController form = loader.getController();
            form.setBaseCake(selected);

            MainController.togglePopupContent(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void loadCakes() {
        mainFlowPane.getChildren().clear();

        ObservableList<Product> cakeList = ProductCache.getProductsByCategoryId(1);
        for (Product p : cakeList) {

            if (!p.isActive()) continue;

            Cake cake = CakeCache.getCakeByProductId(p.getProductId());
            if (cake == null) continue;

            if (cake.getCakeType() != CakeType.PREBAKED) continue;
            if (!cake.isDiyAllowed()) continue;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setProductData(p);
                controller.setOnCardDoubleClick(this::openDiyOrderForm);

                mainFlowPane.getChildren().add(card);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }
}
