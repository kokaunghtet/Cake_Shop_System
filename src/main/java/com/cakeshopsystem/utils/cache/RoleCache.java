package com.cakeshopsystem.utils.cache;

import com.cakeshopsystem.models.Role;
import com.cakeshopsystem.utils.dao.RoleDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.HashMap;
import java.util.Map;

public class RoleCache {

    private static final ObservableList<Role> rolesList = FXCollections.observableArrayList();
    private static final Map<Integer, Role> rolesMap = new HashMap<>();
    private static final Map<String, Integer> roleNameToIdMap = new HashMap<>();

    private RoleCache() {}

    // ===================== Getters =====================

    public static ObservableList<Role> getRolesList() {
        if (rolesList.isEmpty()) refreshRoles();
        return rolesList;
    }

    public static Map<Integer, Role> getRolesMap() {
        if (rolesMap.isEmpty()) refreshRoles();
        return rolesMap;
    }

    public static Role getRoleById(int roleId) {
        if (rolesMap.isEmpty()) refreshRoles();
        return rolesMap.get(roleId);
    }

    public static int getRoleIdByName(String roleName) {
        if (roleName == null || roleName.isBlank()) return -1;
        if (roleNameToIdMap.isEmpty()) refreshRoles();
        return roleNameToIdMap.getOrDefault(roleName.trim().toLowerCase(), -1);
    }

    // ===================== Refresh =====================

    public static void refreshRoles() {
        rolesList.clear();
        rolesMap.clear();
        roleNameToIdMap.clear();

        for (Role role : RoleDAO.getAllRoles()) {
            cacheRole(role);
        }
    }

    // ===================== CRUD Wrappers =====================

    public static boolean addRole(Role role) {
        if (role == null) return false;

        boolean ok = RoleDAO.insertRole(role);
        if (ok) {
            // If DB generated id and DAO sets it into the object, cacheRole will store correct ID.
            cacheRole(role);
        }
        return ok;
    }

    public static boolean updateRole(Role role) {
        if (role == null) return false;

        boolean ok = RoleDAO.updateRole(role);
        if (ok) {
            // Replace in list by roleId (safer than indexOf if equals/hashCode not overridden)
            int idx = findIndexById(role.getRoleId());
            if (idx >= 0) rolesList.set(idx, role);
            else rolesList.add(role);

            rolesMap.put(role.getRoleId(), role);
            roleNameToIdMap.put(normalize(role.getRoleName()), role.getRoleId());
        }
        return ok;
    }

    public static boolean deleteRole(int roleId) {
        boolean ok = RoleDAO.deleteRole(roleId);
        if (ok) {
            Role removed = rolesMap.remove(roleId);
            if (removed != null) {
                rolesList.removeIf(r -> r.getRoleId() == roleId);
                roleNameToIdMap.remove(normalize(removed.getRoleName()));
            } else {
                // fallback cleanup
                rolesList.removeIf(r -> r.getRoleId() == roleId);
                rebuildNameMapFromList();
            }
        }
        return ok;
    }

    // ===================== Helpers =====================

    private static void cacheRole(Role role) {
        if (role == null) return;

        int roleId = role.getRoleId();

        // list (replace if exists)
        int idx = findIndexById(roleId);
        if (idx >= 0) rolesList.set(idx, role);
        else rolesList.add(role);

        // maps
        rolesMap.put(roleId, role);
        roleNameToIdMap.put(normalize(role.getRoleName()), roleId);
    }

    private static int findIndexById(int roleId) {
        for (int i = 0; i < rolesList.size(); i++) {
            if (rolesList.get(i).getRoleId() == roleId) return i;
        }
        return -1;
    }

    private static String normalize(String s) {
        return s == null ? "" : s.trim().toLowerCase();
    }

    private static void rebuildNameMapFromList() {
        roleNameToIdMap.clear();
        for (Role role : rolesList) {
            roleNameToIdMap.put(normalize(role.getRoleName()), role.getRoleId());
        }
    }
}
