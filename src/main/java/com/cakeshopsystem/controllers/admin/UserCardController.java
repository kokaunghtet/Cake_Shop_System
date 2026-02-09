package com.cakeshopsystem.controllers.admin;

import com.cakeshopsystem.controllers.MainController;
import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.ImageHelper;
import com.cakeshopsystem.utils.cache.RoleCache;
import com.cakeshopsystem.utils.dao.OrderDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class UserCardController {

    @FXML
    private ImageView userImage;

    @FXML
    private Label lblUserName;

    @FXML
    private Label lblOrderCount;

    private User user;
    private Object ScreenLoader;

    private void loadAvatar(String imagePath) {
        Image avatar = ImageHelper.load(imagePath);
        userImage.setImage(avatar);
        ImageHelper.applyCircularAvatar(userImage, 100);
    }

    private void updateOrderCount() {
        int count = OrderDAO.getAllOrder()
                .stream()
                .mapToInt(o -> o.getUserId() == user.getUserId() ? 1 : 0)
                .sum();

        lblOrderCount.setText("Orders: " + count);
    }

    public void refreshAvatar() {
        if (user == null) return;

        Image avatar = ImageHelper.load(user.getImagePath());
        userImage.setImage(avatar);
        ImageHelper.applyCircularAvatar(userImage, 64);
    }


    public void setData(int userId, String name, int productOrders, int diyOrders, int customOrders, String imagePath) {
        lblUserName.setText(name);

        int total = productOrders + diyOrders + customOrders;
        lblOrderCount.setText("Orders: " + total);

        if (imagePath != null && !imagePath.isEmpty()) {
            loadAvatar(imagePath);
        }
    }

    public void setUser(User user, int orderCount) {
        if (user == null) return;

        this.user = user;

        lblUserName.setText(user.getUserName());
        lblOrderCount.setText("Orders: " + orderCount);

        if (user.getImagePath() != null) {
            loadAvatar(user.getImagePath());
        }
    }

    @FXML
    private void handleCardClick() {
        MainController.togglePopupContent(
                "/views/admin/StaffPerformanceView.fxml"
        );
    }
}


//--------------------------------original//

/*import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.ImageHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class UserCardController {

    @FXML
    private ImageView userImage;
    @FXML
    private Label lblUserName;
    @FXML
    private Label lblOrderCount;

    private User user;

    public void setUser(User user, int orderCount) {
        this.user = user;

        lblUserName.setText(user.getUserName());
        lblOrderCount.setText("Orders: " + orderCount);

        if (user.getImagePath() != null) {
            Image avatar = ImageHelper.load(user.getImagePath());
            userImage.setImage(avatar);
            ImageHelper.applyCircularAvatar(userImage, 64);
        }
    }

    public User getUser() {
        return user;
    }
}*/































