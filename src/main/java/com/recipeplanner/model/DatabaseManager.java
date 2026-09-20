package com.recipeplanner.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
public class DatabaseManager {

    private static final String URL = "jdbc:sqlite:recipeplanner.db";

    public static Connection connect() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement pragmaStmt = conn.createStatement()) {
            pragmaStmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }

    public static void initializeTables() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS recipes (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    title TEXT NOT NULL,
                    description TEXT,
                    category TEXT NOT NULL,
                    recipe_type TEXT NOT NULL,
                    sugar_content REAL,
                    calories INTEGER,
                    carbs REAL,
                    spice_level TEXT,
                    protein REAL,
                    sodium REAL
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS ingredients (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    recipe_id INTEGER NOT NULL,
                    name TEXT NOT NULL,
                    quantity REAL NOT NULL,
                    unit TEXT,
                    FOREIGN KEY (recipe_id) REFERENCES recipes(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS steps (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    recipe_id INTEGER NOT NULL,
                    step_order INTEGER NOT NULL,
                    instruction TEXT NOT NULL,
                    FOREIGN KEY (recipe_id) REFERENCES recipes(id)
                );
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS favorites (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    recipe_id INTEGER NOT NULL,
                    FOREIGN KEY (recipe_id) REFERENCES recipes(id) ON DELETE CASCADE
                );
            """);

            System.out.println("Tables created successfully!");

        } catch (SQLException e) {
            System.out.println("Error initializing tables: " + e.getMessage());
        }
    }

    public static void insertRecipe(Recipe recipe) {
        String insertRecipeSql = """
        INSERT INTO recipes (title, description, category, recipe_type, sugar_content, calories, carbs, spice_level, protein, sodium)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(insertRecipeSql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, recipe.getTitle());
            pstmt.setString(2, recipe.getDescription());
            pstmt.setString(3, recipe.getCategory().toString());

            if (recipe instanceof SweetFood sweet) {
                pstmt.setString(4, "SWEET");
                pstmt.setDouble(5, sweet.getSugarContent());
                pstmt.setInt(6, sweet.getCalories());
                pstmt.setDouble(7, sweet.getCarbs());
                pstmt.setNull(8, java.sql.Types.VARCHAR);
                pstmt.setNull(9, java.sql.Types.REAL);
                pstmt.setNull(10, java.sql.Types.REAL);
            } else if (recipe instanceof SavoryFood savory) {
                pstmt.setString(4, "SAVORY");
                pstmt.setNull(5, java.sql.Types.REAL);
                pstmt.setNull(6, java.sql.Types.INTEGER);
                pstmt.setNull(7, java.sql.Types.REAL);
                pstmt.setString(8, savory.getSpicelevel().toString());
                pstmt.setDouble(9, savory.getProtein());
                pstmt.setDouble(10, savory.getSodium());
            } else {
                pstmt.setString(4, "PLAIN");
                pstmt.setNull(5, java.sql.Types.REAL);
                pstmt.setNull(6, java.sql.Types.INTEGER);
                pstmt.setNull(7, java.sql.Types.REAL);
                pstmt.setNull(8, java.sql.Types.VARCHAR);
                pstmt.setNull(9, java.sql.Types.REAL);
                pstmt.setNull(10, java.sql.Types.REAL);
            }

            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            int recipeId;
            if (generatedKeys.next()) {
                recipeId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve generated recipe id.");
            }

            insertIngredients(conn, recipeId, recipe.getIngredients());
            insertSteps(conn, recipeId, recipe.getSteps());

            System.out.println("Inserted recipe: " + recipe.getTitle() + " (id=" + recipeId + ")");

        } catch (SQLException e) {
            System.out.println("Error inserting recipe: " + e.getMessage());
        }
    }

    private static void insertIngredients(Connection conn, int recipeId, List<Ingredient> ingredients) throws SQLException {
        String sql = "INSERT INTO ingredients (recipe_id, name, quantity, unit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (Ingredient ing : ingredients) {
                pstmt.setInt(1, recipeId);
                pstmt.setString(2, ing.getName());
                pstmt.setDouble(3, ing.getQuantity());
                pstmt.setString(4, ing.getUnit());
                pstmt.executeUpdate();
            }
        }
    }

    private static void insertSteps(Connection conn, int recipeId, List<String> steps) throws SQLException {
        String sql = "INSERT INTO steps (recipe_id, step_order, instruction) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < steps.size(); i++) {
                pstmt.setInt(1, recipeId);
                pstmt.setInt(2, i + 1);
                pstmt.setString(3, steps.get(i));
                pstmt.executeUpdate();
            }
        }
    }

    public static List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>();
        String sql = "SELECT * FROM recipes";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                Category category = Category.valueOf(rs.getString("category"));
                String recipeType = rs.getString("recipe_type");

                Recipe recipe;
                switch (recipeType) {
                    case "SWEET" -> recipe = new SweetFood(
                            title, description, category,
                            rs.getDouble("sugar_content"),
                            rs.getInt("calories"),
                            rs.getDouble("carbs")
                    );
                    case "SAVORY" -> recipe = new SavoryFood(
                            title, description, category,
                            SavoryFood.SpiceLevel.valueOf(rs.getString("spice_level")),
                            rs.getDouble("protein"),
                            rs.getDouble("sodium")
                    );
                    default -> recipe = new Recipe(title, description, category);
                }

                for (Ingredient ing : getIngredientsForRecipe(conn, id)) {
                    recipe.addIngredients(ing);
                }
                for (String step : getStepsForRecipe(conn, id)) {
                    recipe.addSteps(step);
                }

                recipes.add(recipe);
            }

        } catch (SQLException e) {
            System.out.println("Error fetching recipes: " + e.getMessage());
        }

        return recipes;
    }

    private static List<Ingredient> getIngredientsForRecipe(Connection conn, int recipeId) throws SQLException {
        List<Ingredient> ingredients = new ArrayList<>();
        String sql = "SELECT * FROM ingredients WHERE recipe_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                ingredients.add(new Ingredient(
                        rs.getString("name"),
                        rs.getDouble("quantity"),
                        rs.getString("unit")
                ));
            }
        }
        return ingredients;
    }

    private static List<String> getStepsForRecipe(Connection conn, int recipeId) throws SQLException {
        List<String> steps = new ArrayList<>();
        String sql = "SELECT * FROM steps WHERE recipe_id = ? ORDER BY step_order";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipeId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                steps.add(rs.getString("instruction"));
            }
        }
        return steps;
    }

    public static void main(String[] args) {
        initializeTables();

        List<Recipe> all = getAllRecipes();
        for (Recipe r : all) {
            System.out.println(r);
            System.out.println(r.getNutritionSummary());
            System.out.println("---");
        }
    }
}