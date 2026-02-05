package com.cakeshopsystem.models;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;


public class User {
    private int userId;
    private String userName;
    private String email;
    private String password;
    private String imagePath = null;
    private boolean isActive = true;
    private int role;

    public User() {
    }

    public User(int userId, String userName, String email, String password, String imagePath, boolean isActive, int role) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.imagePath = imagePath;
        this.isActive = isActive;
        this.role = role;
    }

    public User(int userId, String userName, String email, int role) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" + "userId=" + userId + ", userName='" + userName + '\'' + ", email='" + email + '\'' + ", password='" + password + '\'' + ", imagePath='" + imagePath + '\'' + ", isActive=" + isActive + ", role=" + role + '}';
    }
}
