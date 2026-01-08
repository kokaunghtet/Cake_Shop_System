package com.cakeshopsystem.utils.dao;

import com.cakeshopsystem.models.Ingredient;
import com.cakeshopsystem.utils.constants.IngredientUnit;
import com.cakeshopsystem.utils.databaseconnection.DB;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;

public class IngredientDAO {

    // ==========================
    // -------- READ ------------
    // ==========================
    public static ObservableList<Ingredient> getAllIngredients() {
        ObservableList<Ingredient> ingredients = FXCollections.observableArrayList();
        String sql = "SELECT * FROM ingredients ORDER BY ingredient_id";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ingredients.add(mapIngredient(rs));
            }

        } catch (SQLException err) {
            System.err.println("Error fetching ingredients: " + err.getLocalizedMessage());
        }

        return ingredients;
    }

    public static Ingredient getIngredientById(int ingredientId) {
        String sql = "SELECT * FROM ingredients WHERE ingredient_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, ingredientId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapIngredient(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching ingredient by id: " + err.getLocalizedMessage());
        }

        return null;
    }

    public static Ingredient getIngredientByName(String ingredientName) {
        String sql = "SELECT * FROM ingredients WHERE ingredient_name = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, ingredientName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapIngredient(rs);
            }

        } catch (SQLException err) {
            System.err.println("Error fetching ingredient by name: " + err.getLocalizedMessage());
        }

        return null;
    }

    // ==========================
    // -------- WRITE -----------
    // ==========================
    public static boolean insertIngredient(Ingredient ingredient) {
        String sql = """
                INSERT INTO ingredients (ingredient_name, unit)
                VALUES (?, ?)
                """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, ingredient.getIngredientName());
            stmt.setString(2, toDbUnit(ingredient.getUnit()));

            int affected = stmt.executeUpdate();
            if (affected == 0) throw new SQLException("Inserting ingredient failed, no rows affected.");

            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (keys.next()) {
                    ingredient.setIngredientId(keys.getInt(1));
                } else {
                    throw new SQLException("Inserting ingredient failed, no ID obtained.");
                }
            }

            return true;

        } catch (SQLException err) {
            // Will also trigger on duplicate ingredient_name because of UNIQUE constraint
            System.err.println("Error inserting ingredient: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean updateIngredient(Ingredient ingredient) {
        String sql = """
                UPDATE ingredients
                SET ingredient_name = ?,
                    unit = ?
                WHERE ingredient_id = ?
                """;

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setString(1, ingredient.getIngredientName());
            stmt.setString(2, toDbUnit(ingredient.getUnit()));
            stmt.setInt(3, ingredient.getIngredientId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            System.err.println("Error updating ingredient: " + err.getLocalizedMessage());
            return false;
        }
    }

    public static boolean deleteIngredient(int ingredientId) {
        String sql = "DELETE FROM ingredients WHERE ingredient_id = ?";

        try (Connection con = DB.connect();
             PreparedStatement stmt = con.prepareStatement(sql)) {

            stmt.setInt(1, ingredientId);
            return stmt.executeUpdate() > 0;

        } catch (SQLException err) {
            // may fail if referenced by recipes (FK constraint) — that's expected
            System.err.println("Error deleting ingredient: " + err.getLocalizedMessage());
            return false;
        }
    }

    // ==========================
    // ------ ROW MAPPER --------
    // ==========================
    private static Ingredient mapIngredient(ResultSet rs) throws SQLException {
        Ingredient ing = new Ingredient();

        ing.setIngredientId(rs.getInt("ingredient_id"));
        ing.setIngredientName(rs.getString("ingredient_name"));

        String unitStr = rs.getString("unit");
        ing.setUnit(parseUnit(unitStr));

        return ing;
    }

    // ==========================
    // ----- UNIT HELPERS -------
    // ==========================
    private static IngredientUnit parseUnit(String dbValue) {
        if (dbValue == null) return null;

        // Try direct match first (works if enum constants are "Gram", "Kilogram", etc.)
        try {
            return IngredientUnit.valueOf(dbValue);
        } catch (IllegalArgumentException ignored) {}

        // Try uppercase (works if enum constants are "GRAM", "KILOGRAM", etc.)
        try {
            return IngredientUnit.valueOf(dbValue.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {}

        // Last resort: remove spaces/hyphens and uppercase
        String normalized = dbValue.trim().replace(" ", "").replace("-", "").toUpperCase();
        try {
            return IngredientUnit.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {}

        return null;
    }

    /**
     * DB enum values look like: Gram, Kilogram, Tablespoon...
     * This makes the insert/update work even if your Java enum is GRAM/KILOGRAM/etc.
     */
    private static String toDbUnit(IngredientUnit unit) {
        if (unit == null) return null;

        String name = unit.name().trim();
        if (name.isEmpty()) return null;

        // Convert "GRAM" -> "Gram", "TABLESPOON" -> "Tablespoon"
        // If enum is already "Gram", this keeps it as "Gram".
        String raw = name.replace("_", ""); // safety if enum uses underscores
        return raw.substring(0, 1).toUpperCase() + raw.substring(1).toLowerCase();
    }
}
