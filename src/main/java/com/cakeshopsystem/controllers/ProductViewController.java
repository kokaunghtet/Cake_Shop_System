package com.cakeshopsystem.controllers;

import com.cakeshopsystem.models.Drink;
import com.cakeshopsystem.models.Inventory;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.dao.DrinkDAO;
import com.cakeshopsystem.utils.dao.InventoryDAO;
import com.cakeshopsystem.utils.dao.ProductDAO;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;

import java.util.Objects;

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

        loadDiscountedItems();
        loadCakes();
        loadDrinks();
        loadBakedGoods();
        loadAccessories();

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

    private void loadDiscountedItems() {
        ObservableList<Inventory> discountedItemList = InventoryDAO.getInventoryDiscountCandidates();
        for (Inventory inventoryRow : discountedItemList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CakeCard.fxml"));
                Parent card = loader.load();

                CakeCardController controller = loader.getController();
                controller.setData(inventoryRow);

                discountedItemsHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading cake card : " + err.getLocalizedMessage());
            }
        }
    }

    private void loadCakes() {
        ObservableList<Product> cakeList = ProductDAO.getProductsByCategoryId(1);

        for (Product productCake : cakeList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/CakeCard.fxml"));
                Parent card = loader.load();

                CakeCardController controller = loader.getController();
                controller.setData(productCake);

                cakeHBox.getChildren().add(card);
            } catch (Exception err) {
                System.out.println("Error loading cake card : " + err.getLocalizedMessage());
            }
        }
    }

    private void loadDrinks() {
        ObservableList<Product> drinkList = ProductDAO.getProductsByCategoryId(2);

        for (Product p : drinkList) {
            var drinks = DrinkDAO.getDrinksByProductId(p.getProductId());

            double base = p.getPrice();
            double hot = base + drinks.getFirst().getPriceDelta();
            double cold = base + drinks.getLast().getPriceDelta();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/DrinkCard.fxml"));
                Parent card = loader.load();

                DrinkCardController controller = loader.getController();
                controller.setData(p);

                drinkHBox.getChildren().add(card);
            } catch (Exception e) {
                System.out.println("Error loading drink card : " + e.getLocalizedMessage());
            }
        }
    }

    private void loadBakedGoods() {
        ObservableList<Product> bakedGoodList = ProductDAO.getProductsByCategoryId(3);

        for (Product productBakedGood : bakedGoodList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AccessoriesCard.fxml"));
                Parent card = loader.load();

                AccessoriesCardController controller = loader.getController();
                controller.setData(productBakedGood);

                bakedGoodHBox.getChildren().add(card);
            } catch (Exception e) {
                System.out.println("Error loading baked good card : " + e.getLocalizedMessage());
            }
        }
    }

    private void loadAccessories() {
        ObservableList<Product> accessoryList = ProductDAO.getProductsByCategoryId(4);

        for (Product productAccessory : accessoryList) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/AccessoriesCard.fxml"));
                Parent card = loader.load();

                AccessoriesCardController controller = loader.getController();
                controller.setData(productAccessory);

                accessoryHBox.getChildren().add(card);
            } catch (Exception e) {
                System.out.println("Error loading accessory card : " + e.getLocalizedMessage());
            }
        }
    }
}
