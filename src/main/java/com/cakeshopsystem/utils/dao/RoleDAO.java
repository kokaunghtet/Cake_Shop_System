package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Role;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class RoleDAO {

    public static ObservableList<Role> getAllRoles() {
        ObservableList<Role> roles = FXCollections.observableArrayList();
        String query = "SELECT * FROM roles";
        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                roles.add(mapRole(rs));
            }
        } catch (SQLException err) {
            System.err.println("Error fetching roles: " + err.getLocalizedMessage());
        }
        return roles;
    }

    public static Role getRoleById(int roleId) {
        String query = "SELECT * FROM roles WHERE role_id = ?";
        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, roleId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Role role = mapRole(rs);
                    return role;
                }
            }
        } catch (SQLException err) {
            System.err.println("Error fetching role by id: " + err.getLocalizedMessage());
        }
        return null;
    }


    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    // ----------------- Insert Role -----------------
    public static boolean insertRole(Role role) {
        String sql = "INSERT INTO roles (role_name) VALUES (?)";
        try (Connection con = DB.connect(); PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, role.getRoleName());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting roles failed, no rows affected.");

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int roleId = generatedKeys.getInt(1);
                    role.setRoleId(roleId);
                } else {
                    throw new SQLException("Inserting roles failed, no Id obtained");
                }
            }

            return true;
        } catch (SQLException err) {
            System.err.println("Error inserting role: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ----------------- Update Role -----------------

    public static boolean updateRole(Role role) {
        String sql = "UPDATE roles SET role_name = ? WHERE role_id = ?";
        try (Connection con = DB.connect(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setString(1, role.getRoleName());
            stmt.setInt(2, role.getRoleId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException err) {
            System.err.println("Error updating role: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ----------------- Delete Role -----------------

    public static boolean deleteRole(int roleId) {
        String sql = "DELETE FROM roles WHERE role_id = ?";
        try (Connection con = DB.connect(); PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setInt(1, roleId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException err) {
            System.err.println("Error deleting role: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================
    private static Role mapRole(ResultSet rs) throws SQLException {
        int roleId = rs.getInt("role_id");
        String roleName = rs.getString("role_name");
        return new Role(roleId, roleName);
    }
}
