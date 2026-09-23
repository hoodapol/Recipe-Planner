package com.recipeplanner.model;

import java.util.List;

public class RecipeRepository {

    private static final RecipeRepository INSTANCE = new RecipeRepository();

    private RecipeRepository() {
        seedIfEmpty();
    }

    public static RecipeRepository getInstance() {
        return INSTANCE;
    }

    public List<Recipe> getAllRecipes() {
        return DatabaseManager.getAllRecipes();
    }

    private void seedIfEmpty() {
        DatabaseManager.initializeTables();

        if (!DatabaseManager.getAllRecipes().isEmpty()) {
            return;
        }

        System.out.println("Database empty — fetching recipes from API...");

        List<Recipe> fetched = RecipeApiClient.fetchRecipesByFirstLetter("a");

        for (Recipe recipe : fetched) {
            DatabaseManager.insertRecipe(recipe);
        }

        System.out.println("Seeded " + fetched.size() + " recipes from API.");
    }
}