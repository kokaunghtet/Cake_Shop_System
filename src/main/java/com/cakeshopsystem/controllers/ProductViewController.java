package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Cake;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.dao.CakeDAO;
import com.cakeshopsystem.utils.dao.ProductDAO;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

public class ProductViewController {
    @FXML
    public ScrollPane mainScrollPane;
    @FXML
    public Button addNewProductBtn;
    @FXML
    public ScrollPane discountedItemsScrollPane;
    @FXML
    public HBox discountedItemsHBox;
    @FXML
    public ScrollPane cakeScrollPane;
    @FXML
    public HBox cakeHBox;
    @FXML
    public ScrollPane drinkScrollPane;
    @FXML
    public HBox drinkHBox;
    @FXML
    public ScrollPane bakedGoodScrollPane;
    @FXML
    public HBox bakedGoodHBox;
    @FXML
    public ScrollPane accessoryScrollPane;
    @FXML
    public HBox accessoryHBox;
    @FXML
    public Button cartBtn;

    @FXML
    public void initialize() {
        configureRoleBasedAccess();

        loadCakes();
    }

    private void configureRoleBasedAccess() {
        User user = SessionManager.getUser();
        if (user == null) return;
        if (!RoleCache.getRolesMap().containsKey(user.getRole())) return;

        String roleName = RoleCache.getRolesMap().get(user.getRole()).getRoleName();

        if (roleName.equalsIgnoreCase("Admin")) {
//            cartBtn.setVisible(false);
//            cartBtn.setManaged(false);
        } else if (roleName.equalsIgnoreCase("Cashier")) {
            addNewProductBtn.setVisible(false);
            addNewProductBtn.setManaged(false);
        }
    }

    private void loadCakes() {
        ObservableList<Product> cakeList = ProductDAO.getProductsByCategoryId(1);

        for(Product productCake: cakeList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CakeCard.fxml"));
                Parent card = loader.load();

                CakeCardController controller = loader.getController();
                controller.setData(productCake);

                cakeHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading cake card : "+err.getLocalizedMessage());
            }
        }
    }
}
