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

public class CustomOrderController {

    // =================================================
    // FXML UI components
    // =================================================
    @FXML private ScrollPane mainScrollPane;
    @FXML private StackPane mainStackPane;
    @FXML private FlowPane mainFlowPane;
    @FXML private Button btnCreateCustomOrder;

    // =================================================
    // FXML paths & constants
    // =================================================
    private static final String PRODUCT_CARD_FXML = "/views/ProductCard.fxml";
    private static final String CUSTOM_ORDER_FORM = "/views/CustomOrderForm.fxml";

    // =================================================
    // JavaFX lifecycle
    // =================================================
    @FXML
    public void initialize() {
        loadCakes();
        btnCreateCustomOrder.setOnAction(e -> openCustomOrderForm(null));
    }

    // =================================================
    // Navigation / popup handling
    // =================================================
    private void openCustomOrderForm(Product selected) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(CUSTOM_ORDER_FORM));
            Parent root = loader.load();

            CustomOrderFormController form = loader.getController();
            form.setBaseCake(selected);

            MainController.togglePopupContent(root);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // =================================================
    // Data loading & UI population
    // =================================================
    private void loadCakes() {
        ObservableList<Product> cakeList = ProductCache.getProductsByCategoryId(1);

        for (Product p : cakeList) {

            if (!p.isActive()) continue;

            Cake cake = CakeCache.getCakeByProductId(p.getProductId());
            if (cake == null) continue;

            if (cake.getCakeType() != CakeType.PREBAKED) continue;

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(PRODUCT_CARD_FXML));
                Parent card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setProductData(p);
                controller.setOnCardDoubleClick(this::openCustomOrderForm);

                mainFlowPane.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading cake card: ");
                err.printStackTrace();
            }
        }
    }
}
