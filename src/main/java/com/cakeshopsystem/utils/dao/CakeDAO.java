package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Cake;
import com.cakeshopsystem.models.Product;
import com.cakeshopsystem.utils.constants.CakeShape;
import com.cakeshopsystem.utils.constants.CakeType;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CakeDAO {

    // =====================================
    // ========== READ OPERATIONS ===========
    // =====================================

    public static ObservableList<Cake> getAllCakes() {
        ObservableList<Cake> cakes = FXCollections.observableArrayList();
        String query = "SELECT * FROM cakes";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cakes.add(mapCake(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching cakes: " + err.getLocalizedMessage());
        }

        return cakes;
    }

    public static Cake getCakeById(int cakeId) {
        String query = "SELECT * FROM cakes WHERE cake_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, cakeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapCake(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching cake by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static Cake getCakeByProductId(int productId) {
        String query = "SELECT * FROM cakes WHERE product_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, productId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapCake(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching cake by product id: " + err.getLocalizedMessage());
        }


        return null;
    }

    public static ObservableList<Cake> getCakesByType(CakeType cakeType) {
        ObservableList<Cake> cakes = FXCollections.observableArrayList();
        if (cakeType == null) return cakes;

        String query = "SELECT * FROM cakes WHERE cake_type = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setString(1, toDbCakeType(cakeType));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cakes.add(mapCake(rs));
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching cakes by type: " + err.getLocalizedMessage());
        }

        return cakes;
    }

    public static ObservableList<Cake> getDiyAllowedCakes() {
        ObservableList<Cake> cakes = FXCollections.observableArrayList();
        String query = "SELECT * FROM cakes WHERE is_diy_allowed = 1";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                cakes.add(mapCake(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching diy allowed cakes: " + err.getLocalizedMessage());
        }

        return cakes;
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    // ----------------- Insert Cake -----------------
    public static boolean insertCake(Cake cake) {
        if (!isCakeValidForDb(cake)) return false;

        // DB rule: Only Prebaked can be DIY
        if (!isPrebaked(cake.getCakeType())) {
            cake.setDiyAllowed(false);
        }

        String sql = "INSERT INTO cakes (product_id, flavour_id, topping_id, size_id, cake_type, shape, is_diy_allowed) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, cake.getProductId());
            stmt.setInt(2, cake.getFlavourId());
            stmt.setInt(3, cake.getToppingId());
            stmt.setInt(4, cake.getSizeId());
            stmt.setString(5, toDbCakeType(cake.getCakeType()));
            stmt.setString(6, toDbCakeShape(cake.getShape()));
            stmt.setBoolean(7, cake.isDiyAllowed());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting cakes failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    cake.setCakeId(keys.getInt(1));
                } else {
                    throw new SQLException("Inserting cakes failed, no id obtained");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting cake: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ----------------- Update Cake -----------------
    public static boolean updateCake(Cake cake) {
        if (!isCakeValidForDb(cake)) return false;

        // DB rule: Only Prebaked can be DIY
        if (!isPrebaked(cake.getCakeType())) {
            cake.setDiyAllowed(false);
        }

        String sql = "UPDATE cakes SET product_id = ?, flavour_id = ?, topping_id = ?, size_id = ?, cake_type = ?, shape = ?, is_diy_allowed = ? " +
                "WHERE cake_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, cake.getProductId());
            stmt.setInt(2, cake.getFlavourId());
            stmt.setInt(3, cake.getToppingId());
            stmt.setInt(4, cake.getSizeId());
            stmt.setString(5, toDbCakeType(cake.getCakeType()));
            stmt.setString(6, toDbCakeShape(cake.getShape()));
            stmt.setBoolean(7, cake.isDiyAllowed());
            stmt.setInt(8, cake.getCakeId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating cake: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ----------------- Delete Cake -----------------
    public static boolean deleteCake(int cakeId) {
        String sql = "DELETE FROM cakes WHERE cake_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, cakeId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting cake: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================

    private static Cake mapCake(ResultSet rs) throws SQLException {
        int cakeId = rs.getInt("cake_id");
        int productId = rs.getInt("product_id");
        int flavourId = rs.getInt("flavour_id");
        int toppingId = rs.getInt("topping_id");
        int sizeId = rs.getInt("size_id");

        String cakeTypeStr = rs.getString("cake_type");
        String shapeStr = rs.getString("shape");

        boolean isDiyAllowed = rs.getBoolean("is_diy_allowed");

        CakeType cakeType = fromDbCakeType(cakeTypeStr);
        CakeShape shape = fromDbCakeShape(shapeStr);

        return new Cake(cakeId, productId, flavourId, toppingId, sizeId, cakeType, shape, isDiyAllowed);
    }

    private static boolean isCakeValidForDb(Cake cake) {
        if (cake == null) return false;
        if (cake.getProductId() <= 0) return false;
        if (cake.getFlavourId() <= 0) return false;
        if (cake.getToppingId() <= 0) return false;
        if (cake.getSizeId() <= 0) return false;
        if (cake.getCakeType() == null) return false;
        if (cake.getShape() == null) return false;
        return true;
    }

    private static boolean isPrebaked(CakeType cakeType) {
        if (cakeType == null) return false;
        return cakeType.name().equalsIgnoreCase("PREBAKED")
                || cakeType.name().equalsIgnoreCase("PRE_BAKED")
                || cakeType.name().equalsIgnoreCase("PREBAKED_CAKE");
    }

    private static String toDbCakeType(CakeType cakeType) {
        if (cakeType == null) return null;

        String n = cakeType.name();
        if (n.equalsIgnoreCase("PREBAKED") || n.equalsIgnoreCase("PRE_BAKED")) return "Prebaked";
        if (n.equalsIgnoreCase("CUSTOM")) return "Custom";

        // fallback: try to store enum name (not recommended, but safer than null)
        return n;
    }

    private static String toDbCakeShape(CakeShape shape) {
        if (shape == null) return null;

        String n = shape.name();
        if (n.equalsIgnoreCase("SQUARE")) return "Square";
        if (n.equalsIgnoreCase("CIRCLE")) return "Circle";
        if (n.equalsIgnoreCase("HEART")) return "Heart";

        return n;
    }

    private static CakeType fromDbCakeType(String dbValue) {
        if (dbValue == null) return null;

        for (CakeType t : CakeType.values()) {
            if (t.name().equalsIgnoreCase(dbValue)) return t;
            // allow mapping "Prebaked" -> PREBAKED
            if (dbValue.equalsIgnoreCase("Prebaked") && t.name().equalsIgnoreCase("PREBAKED")) return t;
            if (dbValue.equalsIgnoreCase("Custom") && t.name().equalsIgnoreCase("CUSTOM")) return t;
        }
        return null;
    }

    private static CakeShape fromDbCakeShape(String dbValue) {
        if (dbValue == null) return null;

        for (CakeShape s : CakeShape.values()) {
            if (s.name().equalsIgnoreCase(dbValue)) return s;
            if (dbValue.equalsIgnoreCase("Square") && s.name().equalsIgnoreCase("SQUARE")) return s;
            if (dbValue.equalsIgnoreCase("Circle") && s.name().equalsIgnoreCase("CIRCLE")) return s;
            if (dbValue.equalsIgnoreCase("Heart") && s.name().equalsIgnoreCase("HEART")) return s;
        }
        return null;
    }

    // CakeDAO.java
    public static boolean updateDiyAllowedIfPrebaked(int productId, boolean isDiyAllowed) {
        String sql = """
                    UPDATE cakes
                    SET is_diy_allowed = ?
                    WHERE product_id = ?
                      AND cake_type = 'Prebaked'
                """;

        try (var con = DB.connect();
             var ps = con.prepareStatement(sql)) {

            ps.setBoolean(1, isDiyAllowed);
            ps.setInt(2, productId);

            // returns true even if 0 rows updated (means it was Custom)
            ps.executeUpdate();
            return true;

        } catch (Exception e) {
            System.err.println("updateDiyAllowedIfPrebaked error: " + e.getMessage());
            return false;
        }
    }

    // =====================================
// ===== TOP SELLING CAKES ==============
// =====================================
    // =====================================
// ===== TOP SELLING CAKES (LABEL + IMAGE) =====
// =====================================



}
