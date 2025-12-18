package com.cakeshopsystem.models;

public class User {
    private int userId;
    private String userName;
    private String password;
    private int roleId;
    private String email;
    private boolean isActive;

    public User() {
    }

    public User(int userId, String email, boolean isActive, String password, int roleId, String userName) {
        this.userId = userId;
        this.email = email;
        this.isActive = isActive;
        this.password = password;
        this.roleId = roleId;
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
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

    @Override
    public String toString() {
        return "User{" +
                "User_Id=" + userId +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", Role_Id=" + roleId +
                ", Email='" + email + '\'' +
                ", Is_active=" + isActive +
                '}';
    }
}
