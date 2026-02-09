package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.AuthResult;
import com.cakeshopsystem.utils.dao.UserDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class UserCache {

    // =====================================
    // CACHE CONTAINERS
    // =====================================
    private static final ObservableList<User> userList = FXCollections.observableArrayList();
    private static final Map<Integer, User> userMap = new HashMap<>();
    private static final Map<Integer, ObservableList<User>> usersByRole = new HashMap<>();

    // =====================================
    // STATE & CONFIG
    // =====================================
    private static final Object refreshLock = new Object();
    private static volatile boolean isRefreshing = false;
    private static int totalUsers = 0;
    private static Map<Integer, Integer> totalUsersByRole = new HashMap<>();

    private UserCache() {}

    // =====================================
    // PUBLIC ACCESSORS
    // =====================================
    public static ObservableList<User> getUsersList() {
        int total = UserDAO.getTotalUserCount();
        if (userList.size() < total) refreshUsers();
        return userList;
    }


    public static User getUserById(int userId) {
        User cached = userMap.get(userId);
        if (cached != null) return cached;

        User user = UserDAO.getUserById(userId);
        if (user != null) addToCache(user);
        return user;
    }

    public static int getTotalUserCount() {
        if (totalUsers == 0) refreshUsers();
        return totalUsers;
    }

    public static int getTotalUserCountByRole(int roleId) {
        if (totalUsersByRole == null || totalUsersByRole.isEmpty()) {
            totalUsersByRole = UserDAO.getUserCountByRole();
        }
        return totalUsersByRole.getOrDefault(roleId, 0);
    }

    public static Map<Integer, ObservableList<User>> getUsersByRoles(int... roleIds) {
        Map<Integer, ObservableList<User>> result = new HashMap<>();
        if (roleIds == null || roleIds.length == 0) return result;

        for (int roleId : roleIds) {
            result.put(roleId, getUsersByRole(roleId));
        }
        return result;
    }

    public static ObservableList<User> getUsersByRole(int roleId) {
        ObservableList<User> cached = usersByRole.computeIfAbsent(roleId, k -> FXCollections.observableArrayList());

        if (cached.size() < getTotalUserCountByRole(roleId)) {
            ObservableList<User> fresh = UserDAO.getUsersByRole(roleId);
            for (User user : fresh) addToCache(user);
            cached = usersByRole.get(roleId);
        }

        return cached;
    }

    // =====================================
    // REFRESH LOGIC
    // =====================================
    public static void refreshUsers() {
        synchronized (refreshLock) {
            if (isRefreshing) return;
            isRefreshing = true;
        }

        try {
            // Calling the updated DAO method (No pagination parameters)
            ObservableList<User> users = UserDAO.getAllUsers();
            totalUsers = UserDAO.getTotalUserCount();
            totalUsersByRole = UserDAO.getUserCountByRole();

            synchronized (refreshLock) {
                userList.clear();
                userMap.clear();
                usersByRole.clear();

                for (User user : users) {
                    userList.add(user);
                    userMap.put(user.getUserId(), user);
                    usersByRole
                            .computeIfAbsent(user.getRole(), k -> FXCollections.observableArrayList())
                            .add(user);
                }

                FXCollections.sort(userList, Comparator.comparingInt(User::getUserId));
            }
        } finally {
            synchronized (refreshLock) {
                isRefreshing = false;
            }
        }
    }

    // =====================================
    // CRUD OPERATIONS
    // =====================================
    public static boolean addUser(User user) {
        AuthResult res = UserDAO.insertUser(user);
        if (res != null && res.success()) {
            synchronized (refreshLock) {
                addToCache(user);
                totalUsers = Math.max(0, totalUsers + 1);
                totalUsersByRole = UserDAO.getUserCountByRole();
                FXCollections.sort(userList, Comparator.comparingInt(User::getUserId));
            }
            return true;
        }
        return false;
    }

    public static boolean updateUser(User user) {
        if (!UserDAO.updateUser(user)) return false;

        synchronized (refreshLock) {
            addToCache(user);
            totalUsersByRole = UserDAO.getUserCountByRole();
            FXCollections.sort(userList, Comparator.comparingInt(User::getUserId));
        }
        return true;
    }

    public static boolean deleteUser(int userId) {
        if (!UserDAO.deleteUser(userId)) return false;

        synchronized (refreshLock) {
            User removed = userMap.remove(userId);
            userList.removeIf(u -> u.getUserId() == userId);

            if (removed != null) {
                ObservableList<User> roleList = usersByRole.get(removed.getRole());
                if (roleList != null) roleList.removeIf(u -> u.getUserId() == userId);
            }

            totalUsers = Math.max(0, totalUsers - 1);
            totalUsersByRole = UserDAO.getUserCountByRole();
        }

        return true;
    }

    // =====================================
    // CACHE MANAGEMENT
    // =====================================
    public static void clearCache() {
        synchronized (refreshLock) {
            userList.clear();
            userMap.clear();
            usersByRole.clear();
            totalUsers = 0;
            totalUsersByRole.clear();
            isRefreshing = false;
        }
    }

    // =====================================
    // INTERNAL HELPERS
    // =====================================
    private static void addToCache(User user) {
        if (user == null) return;

        int id = user.getUserId();

        // Update list
        int idx = findIndexById(id);
        if (idx >= 0) {
            userList.set(idx, user);
        } else {
            userList.add(user);
        }

        // Update map
        userMap.put(id, user);

        // Update role buckets
        usersByRole.values().forEach(list -> list.removeIf(u -> u.getUserId() == id));
        usersByRole
                .computeIfAbsent(user.getRole(), k -> FXCollections.observableArrayList())
                .add(user);
    }

    private static int findIndexById(int userId) {
        for (int i = 0; i < userList.size(); i++) {
            if (userList.get(i).getUserId() == userId) return i;
        }
        return -1;
    }
}