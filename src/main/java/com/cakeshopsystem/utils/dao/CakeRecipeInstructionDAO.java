package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.CakeRecipeInstruction;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class CakeRecipeInstructionDAO {

    // =====================================
    // ========== READ OPERATIONS ==========
    // =====================================

    public static ObservableList<CakeRecipeInstruction> getAllCakeRecipeInstructions() {
        ObservableList<CakeRecipeInstruction> list = FXCollections.observableArrayList();
        String query = "SELECT * FROM cake_recipe_instructions";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapCakeRecipeInstruction(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching cake recipe instructions: " + err.getLocalizedMessage());
        }

        return list;
    }

    public static CakeRecipeInstruction getCakeRecipeInstructionById(int cakeRecipeInstructionId) {
        String query = "SELECT * FROM cake_recipe_instructions WHERE cake_recipe_instruction_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, cakeRecipeInstructionId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapCakeRecipeInstruction(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching cake recipe instruction by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static CakeRecipeInstruction getCakeRecipeInstructionByCakeId(int cakeId) {
        String query = "SELECT * FROM cake_recipe_instructions WHERE cake_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(query)) {

            stmt.setInt(1, cakeId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapCakeRecipeInstruction(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching cake recipe instruction by cake_id: " + err.getLocalizedMessage());
        }

        return null;
    }

    // =====================================
    // ========== CRUD OPERATIONS ==========
    // =====================================

    public static boolean insertCakeRecipeInstruction(CakeRecipeInstruction cri) {
        // Requires AUTO_INCREMENT on cake_recipe_instruction_id
        String sql = "INSERT INTO cake_recipe_instructions (cake_id, instruction) VALUES (?, ?)";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, cri.getCakeId());
            stmt.setString(2, cri.getInstruction());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) throw new SQLException("Inserting cake_recipe_instructions failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    cri.setCakeRecipeInstructionId(keys.getInt(1));
                } else {
                    throw new SQLException("Inserting cake_recipe_instructions failed, no id obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            System.err.println("Error inserting cake recipe instruction: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean updateCakeRecipeInstruction(CakeRecipeInstruction cri) {
        // Usually you only edit instruction; cake_id typically doesn't change.
        String sql = "UPDATE cake_recipe_instructions SET cake_id = ?, instruction = ? WHERE cake_recipe_instruction_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, cri.getCakeId());
            stmt.setString(2, cri.getInstruction());
            stmt.setInt(3, cri.getCakeRecipeInstructionId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating cake recipe instruction: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean deleteCakeRecipeInstruction(int cakeRecipeInstructionId) {
        String sql = "DELETE FROM cake_recipe_instructions WHERE cake_recipe_instruction_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, cakeRecipeInstructionId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error deleting cake recipe instruction: " + err.getLocalizedMessage());
            return false;
        }
    }

    // =====================================
    // ========== Helper Methods ===========
    // =====================================
    private static CakeRecipeInstruction mapCakeRecipeInstruction(ResultSet rs) throws SQLException {
        int id = rs.getInt("cake_recipe_instruction_id");
        int cakeId = rs.getInt("cake_id");
        String instruction = rs.getString("instruction");

        return new CakeRecipeInstruction(id, cakeId, instruction);
    }
}
