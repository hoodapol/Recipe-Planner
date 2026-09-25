package com.recipeplanner.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
                    sodium REAL,
                    image_url TEXT
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
            INSERT INTO recipes (title, description, category, recipe_type, sugar_content, calories, carbs, spice_level, protein, sodium, image_url)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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

            pstmt.setString(11, recipe.getImageUrl());

            pstmt.executeUpdate();

            ResultSet generatedKeys = pstmt.getGeneratedKeys();
            int recipeId;
            if (generatedKeys.next()) {
                recipeId = generatedKeys.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve generated recipe id.");
            }

            recipe.setId(recipeId);

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
                Recipe recipe = mapRowToRecipe(rs);

                int id = rs.getInt("id");
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

    private static Recipe mapRowToRecipe(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        Category category = Category.valueOf(rs.getString("category"));
        String recipeType = rs.getString("recipe_type");
        String imageUrl = rs.getString("image_url");

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

        recipe.setId(id);
        recipe.setImageUrl(imageUrl);

        return recipe;
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

    public static void updateRecipeDescription(int recipeId, String newDescription) {
        String sql = "UPDATE recipes SET description = ? WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newDescription);
            pstmt.setInt(2, recipeId);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Updated " + rowsAffected + " row(s).");

        } catch (SQLException e) {
            System.out.println("Error updating recipe: " + e.getMessage());
        }
    }

    public static void deleteRecipe(int recipeId) {
        String deleteIngredientsSql = "DELETE FROM ingredients WHERE recipe_id = ?";
        String deleteStepsSql = "DELETE FROM steps WHERE recipe_id = ?";
        String deleteRecipeSql = "DELETE FROM recipes WHERE id = ?";

        try (Connection conn = connect()) {
            try (PreparedStatement pstmt = conn.prepareStatement(deleteIngredientsSql)) {
                pstmt.setInt(1, recipeId);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(deleteStepsSql)) {
                pstmt.setInt(1, recipeId);
                pstmt.executeUpdate();
            }
            try (PreparedStatement pstmt = conn.prepareStatement(deleteRecipeSql)) {
                pstmt.setInt(1, recipeId);
                int rowsAffected = pstmt.executeUpdate();
                System.out.println("Deleted " + rowsAffected + " row(s) from recipes.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting recipe: " + e.getMessage());
        }
    }

    public static void addFavoriteRecipe(int recipeId) {
        if (isFavorite(recipeId)) {
            return;
        }
        String sql = "INSERT INTO favorites (recipe_id) VALUES (?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error adding favorite: " + e.getMessage());
        }
    }

    public static void removeFavoriteRecipe(int recipeId) {
        String sql = "DELETE FROM favorites WHERE recipe_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error removing favorite: " + e.getMessage());
        }
    }

    public static boolean isFavorite(int recipeId) {
        String sql = "SELECT COUNT(*) AS total FROM favorites WHERE recipe_id = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, recipeId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total") > 0;
            }
        } catch (SQLException e) {
            System.out.println("Error checking favorite: " + e.getMessage());
        }
        return false;
    }

    public static List<Recipe> getFavoriteRecipes() {
        List<Recipe> favorites = new ArrayList<>();
        String sql = "SELECT recipe_id FROM favorites";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int recipeId = rs.getInt("recipe_id");
                Recipe r = getRecipeById(recipeId);
                if (r != null) {
                    favorites.add(r);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching favorites: " + e.getMessage());
        }

        return favorites;
    }

    public static Recipe getRecipeById(int recipeId) {
        String sql = "SELECT * FROM recipes WHERE id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, recipeId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Recipe recipe = mapRowToRecipe(rs);

                for (Ingredient ing : getIngredientsForRecipe(conn, recipeId)) {
                    recipe.addIngredients(ing);
                }
                for (String step : getStepsForRecipe(conn, recipeId)) {
                    recipe.addSteps(step);
                }

                return recipe;
            }
        } catch (SQLException e) {
            System.out.println("Error fetching recipe by id: " + e.getMessage());
        }

        return null;
    }

    public static int countFavorites() {
        String sql = "SELECT COUNT(*) AS total FROM favorites";
        int count = 0;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                count = rs.getInt("total");
            }

        } catch (SQLException e) {
            System.out.println("Error counting favorites: " + e.getMessage());
        }

        return count;
    }
}