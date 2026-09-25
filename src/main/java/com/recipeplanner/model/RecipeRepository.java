package com.recipeplanner.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        System.out.println("Database empty — fetching balanced recipes from API...");

        List<Recipe> balanced = RecipeApiClient.fetchBalancedRecipes(10);

        Set<String> alreadyFetchedIds = balanced.stream()
                .map(Recipe::getExternalId)
                .collect(Collectors.toSet());

        List<Recipe> extraRandom = RecipeApiClient.fetchAdditionalRandomRecipes(40, alreadyFetchedIds);

        for (Recipe recipe : balanced) {
            DatabaseManager.insertRecipe(recipe);
        }
        for (Recipe recipe : extraRandom) {
            DatabaseManager.insertRecipe(recipe);
        }

        int total = balanced.size() + extraRandom.size();
        System.out.println("Seeded " + total + " recipes (" + balanced.size()
                + " guaranteeing category coverage, " + extraRandom.size() + " random extras).");
    }
}