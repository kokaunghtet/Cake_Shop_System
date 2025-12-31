package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.User;
import com.cakeshopsystem.utils.AuthResult;
import com.cakeshopsystem.utils.dao.UserDAO;
import com.cakeshopsystem.utils.session.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class UserCache {

    // ===================== Cache Containers =====================
    private static final ObservableList<User> userList = FXCollections.observableArrayList();
    private static final Map<Integer, User> userMap = new HashMap<>();
    private static final Map<Integer, ObservableList<User>> usersByRole = new HashMap<>();

    // ===================== State & Config =====================
    private static final Object refreshLock = new Object();
    private static final int DEFAULT_LIMIT = SessionManager.PAGINATION_LIMIT;

    private static volatile boolean isRefreshing = false;
    private static int currentOffset = 0;
    private static int totalUsers = 0;
    private static Map<Integer, Integer> totalUsersByRole = new HashMap<>();

    private UserCache() {}

    // ===================== Public Accessors =====================

    public static ObservableList<User> getUsersList() {
        if (userList.isEmpty()) refreshUsers();
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

    // ===================== Pagination / Loading =====================

    /**
     * Loads a page from DB and merges into cache. Returns ONLY the freshly loaded page.
     */
    public static ObservableList<User> loadMoreUsers(int limit, int offset) {
        synchronized (refreshLock) {
            if (isRefreshing) return FXCollections.observableArrayList();
            isRefreshing = true;
        }

        try {
            int safeLimit = Math.max(1, limit);
            int safeOffset = Math.max(0, offset);

            ObservableList<User> users = UserDAO.getAllUsers(safeLimit, safeOffset);

            synchronized (refreshLock) {
                for (User user : users) {
                    if (!userMap.containsKey(user.getUserId())) {
                        addToCache(user);
                    } else {
                        // keep cache updated if the row changed
                        addToCache(user);
                    }
                }
                currentOffset = safeOffset;
                FXCollections.sort(userList, Comparator.comparingInt(User::getUserId));
            }

            return FXCollections.observableArrayList(users);
        } finally {
            synchronized (refreshLock) {
                isRefreshing = false;
            }
        }
    }

    // ===================== Refresh =====================

    public static void refreshUsers() {
        refreshUsers(DEFAULT_LIMIT, currentOffset);
    }

    public static void refreshUsers(int limit, int offset) {
        synchronized (refreshLock) {
            if (isRefreshing) return;
            isRefreshing = true;
        }

        try {
            int safeLimit = Math.max(1, limit);
            int safeOffset = Math.max(0, offset);

            ObservableList<User> users = UserDAO.getAllUsers(safeLimit, safeOffset);
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

                currentOffset = safeOffset;
                FXCollections.sort(userList, Comparator.comparingInt(User::getUserId));
            }
        } finally {
            synchronized (refreshLock) {
                isRefreshing = false;
            }
        }
    }

    // ===================== CRUD Operations =====================

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

    // ===================== Clear Cache =====================

    public static void clearCache() {
        synchronized (refreshLock) {
            userList.clear();
            userMap.clear();
            usersByRole.clear();
            totalUsers = 0;
            totalUsersByRole.clear();
            currentOffset = 0;
            isRefreshing = false;
        }
    }

    // ===================== Internal Helpers =====================

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
