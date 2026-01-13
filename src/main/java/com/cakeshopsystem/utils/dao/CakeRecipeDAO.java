package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.CakeRecipe;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CakeRecipeDAO {

    // ===================== READ =====================

    public static ObservableList<CakeRecipe> getAllCakeRecipe() {
        ObservableList<CakeRecipe> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM cake_recipes";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                list.add(mapCakeRecipe(rs));
            }
        } catch (SQLException err) {
            System.err.println("Error fetching cake_recipes: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static CakeRecipe getCakeRecipeById(int cakeRecipeId) {
        String sql = "SELECT * FROM cake_recipes WHERE cake_recipe_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)
        ) {
            stmt.setInt(1, cakeRecipeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapCakeRecipe(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching cake_recipe by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static ObservableList<CakeRecipe> getCakeRecipeByCakeId(int cakeId) {
        ObservableList<CakeRecipe> list = FXCollections.observableArrayList();
        String sql = "SELECT * FROM cake_recipes WHERE cake_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)
        ) {
            stmt.setInt(1, cakeId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapCakeRecipe(rs));
                }
            }

        } catch (SQLException err) {
            System.err.println("Error fetching cake_recipes by cake_id: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static CakeRecipe getCakeRecipeByCakeAndIngredient(int cakeId, int ingredientId) {
        String sql = "SELECT * FROM cake_recipes WHERE cake_id = ? AND ingredient_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)
        ) {
            stmt.setInt(1, cakeId);
            stmt.setInt(2, ingredientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapCakeRecipe(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching cake_recipe by cake_id + ingredient_id: " + err.getLocalizedMessage());
        }

        return null;
    }

    // ===================== CRUD =====================

    public static boolean insertCakeRecipe(CakeRecipe cakeRecipe) {
        String sql = "INSERT INTO cake_recipes (cake_id, ingredient_id, required_quantity) VALUES (?, ?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)
        ) {
            stmt.setInt(1, cakeRecipe.getCakeId());
            stmt.setInt(2, cakeRecipe.getIngredientId());
            stmt.setDouble(3, cakeRecipe.getRequiredQuantity());

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("Inserting cake_recipe failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    cakeRecipe.setCakeRecipeId(keys.getInt(1));
                } else {
                    throw new SQLException("Inserting cake_recipe failed, no id obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting cake_recipe: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean updateCakeRecipe(CakeRecipe cakeRecipe) {
        String sql = "UPDATE cake_recipes SET cake_id = ?, ingredient_id = ?, required_quantity = ? WHERE cake_recipe_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)
        ) {
            stmt.setInt(1, cakeRecipe.getCakeId());
            stmt.setInt(2, cakeRecipe.getIngredientId());
            stmt.setDouble(3, cakeRecipe.getRequiredQuantity());
            stmt.setInt(4, cakeRecipe.getCakeRecipeId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating cake_recipe: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean deleteCakeRecipe(int cakeRecipeId) {
        String sql = "DELETE FROM cake_recipes WHERE cake_recipe_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)
        ) {
            stmt.setInt(1, cakeRecipeId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting cake_recipe: " + err.getLocalizedMessage());
            return false;
        }
    }

    // (Optional helper) delete all recipe rows for one cake
    public static boolean deleteCakeRecipeByCakeId(int cakeId) {
        String sql = "DELETE FROM cake_recipes WHERE cake_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)
        ) {
            stmt.setInt(1, cakeId);
            stmt.executeUpdate();
            return true;

        } catch (SQLException err) {
            System.err.println("Error deleting cake_recipes by cake_id: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ===================== Helper =====================

    private static CakeRecipe mapCakeRecipe(ResultSet rs) throws SQLException {
        int id = rs.getInt("cake_recipe_id");
        int cakeId = rs.getInt("cake_id");
        int ingredientId = rs.getInt("ingredient_id");
        double qty = rs.getDouble("required_quantity");

        return new CakeRecipe(id, cakeId, ingredientId, qty);
    }
}
