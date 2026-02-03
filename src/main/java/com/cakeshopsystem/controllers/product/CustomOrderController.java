package  com.cakeshopsystem.controllers.product;

import com.cakeshopsystem.controllers.ProductCardController;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.dao.ProductDAO;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

public class CustomOrderController {

    @FXML
    private FlowPane customCakePane;


    public void initialize(){
        loadProductCard();
    }
    public void loadProductCard(){
        customCakePane.getChildren().clear();

        ObservableList<Product> plist = ProductDAO.getProductsByCategoryId(1);

        for (Product product : plist) {
            try {
                FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/ProductCard.fxml"));

                VBox card = loader.load();

                ProductCardController controller = loader.getController();
                controller.setCakeData(product);

                customCakePane.getChildren().add(card);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
