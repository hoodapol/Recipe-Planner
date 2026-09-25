package com.recipeplanner.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RecipeApiClient {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    /**
     * Maps our own Category enum to one or more real TheMealDB category
     * names, used with filter.php to guarantee coverage for every one
     * of our categories (since random.php alone can't guarantee this).
     */
    private static final Map<Category, List<String>> SOURCE_CATEGORIES = Map.of(
            Category.BREAKFAST, List.of("Breakfast"),
            Category.DESSERT, List.of("Dessert"),
            Category.APPETIZER, List.of("Starter", "Side"),
            Category.SNACK, List.of("Vegetarian", "Vegan"),
            Category.LUNCH, List.of("Pasta", "Miscellaneous"),
            Category.DINNER, List.of("Chicken", "Beef", "Seafood", "Pork", "Lamb")
    );

    /**
     * Fetches enough recipes so every one of our Category values has at
     * least `perCategoryMinimum` recipes, using TheMealDB's real category
     * filter (not left to random chance).
     */
    public static List<Recipe> fetchBalancedRecipes(int perCategoryMinimum) {
        List<Recipe> allRecipes = new ArrayList<>();
        Set<String> seenExternalIds = new HashSet<>();

        for (Category target : Category.values()) {
            List<String> sourceCategories = SOURCE_CATEGORIES.getOrDefault(target, List.of("Miscellaneous"));
            int collected = 0;

            for (String sourceCategory : sourceCategories) {
                if (collected >= perCategoryMinimum) break;

                List<String> mealIds = fetchMealIdsByCategory(sourceCategory);
                Collections.shuffle(mealIds);

                for (String mealId : mealIds) {
                    if (collected >= perCategoryMinimum) break;
                    if (seenExternalIds.contains(mealId)) continue;

                    Recipe recipe = fetchFullRecipeById(mealId);
                    if (recipe == null) continue;

                    recipe.setCategory(target);
                    seenExternalIds.add(mealId);
                    allRecipes.add(recipe);
                    collected++;
                }
            }
        }

        return allRecipes;
    }

    /**
     * Adds extra recipes purely at random, on top of an already-fetched
     * batch, skipping anything already present (by externalId).
     */
    public static List<Recipe> fetchAdditionalRandomRecipes(int count, Set<String> alreadyHaveExternalIds) {
        List<Recipe> recipes = new ArrayList<>();
        Set<String> seen = new HashSet<>(alreadyHaveExternalIds);

        int attempts = 0;
        int maxAttempts = count * 3;

        while (recipes.size() < count && attempts < maxAttempts) {
            attempts++;
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://www.themealdb.com/api/json/v1/1/random.php"))
                        .build();

                HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.body());
                JsonNode meals = root.get("meals");

                if (meals == null || meals.isNull() || meals.isEmpty()) {
                    continue;
                }

                JsonNode meal = meals.get(0);
                String externalId = meal.get("idMeal").asText();

                if (seen.contains(externalId)) {
                    continue;
                }

                seen.add(externalId);
                recipes.add(parseRecipe(meal));

            } catch (Exception e) {
                System.out.println("Error fetching random recipe: " + e.getMessage());
            }
        }

        return recipes;
    }

    public static List<Recipe> fetchRandomRecipes(int count) {
        return fetchAdditionalRandomRecipes(count, new HashSet<>());
    }

    public static List<Recipe> searchRecipesByName(String query) {
        List<Recipe> recipes = new ArrayList<>();

        try {
            String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.themealdb.com/api/json/v1/1/search.php?s=" + encodedQuery))
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode meals = root.get("meals");

            if (meals == null || meals.isNull()) {
                return recipes;
            }

            for (JsonNode meal : meals) {
                recipes.add(parseRecipe(meal));
            }

        } catch (Exception e) {
            System.out.println("Error searching recipes: " + e.getMessage());
        }

        return recipes;
    }

    private static List<String> fetchMealIdsByCategory(String categoryName) {
        List<String> ids = new ArrayList<>();

        try {
            String encodedCategory = URLEncoder.encode(categoryName, StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.themealdb.com/api/json/v1/1/filter.php?c=" + encodedCategory))
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode meals = root.get("meals");

            if (meals == null || meals.isNull()) {
                return ids;
            }

            for (JsonNode meal : meals) {
                ids.add(meal.get("idMeal").asText());
            }

        } catch (Exception e) {
            System.out.println("Error fetching category '" + categoryName + "': " + e.getMessage());
        }

        return ids;
    }

    private static Recipe fetchFullRecipeById(String mealId) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.themealdb.com/api/json/v1/1/lookup.php?i=" + mealId))
                    .build();

            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(response.body());
            JsonNode meals = root.get("meals");

            if (meals == null || meals.isNull() || meals.isEmpty()) {
                return null;
            }

            return parseRecipe(meals.get(0));

        } catch (Exception e) {
            System.out.println("Error fetching recipe id=" + mealId + ": " + e.getMessage());
            return null;
        }
    }

    private static Recipe parseRecipe(JsonNode meal) {
        String title = meal.get("strMeal").asText();
        String instructionsRaw = meal.get("strInstructions").asText();
        String apiCategory = meal.get("strCategory") != null ? meal.get("strCategory").asText() : "";
        String imageUrl = meal.get("strMealThumb") != null ? meal.get("strMealThumb").asText() : "";
        String externalId = meal.get("idMeal") != null ? meal.get("idMeal").asText() : "";

        Category category = mapCategory(apiCategory);

        Recipe recipe = new Recipe(title, "Imported from TheMealDB (" + apiCategory + ")", category);
        recipe.setImageUrl(imageUrl);
        recipe.setExternalId(externalId);

        for (int i = 1; i <= 20; i++) {
            JsonNode ingredientNode = meal.get("strIngredient" + i);
            JsonNode measureNode = meal.get("strMeasure" + i);

            if (ingredientNode == null) continue;

            String ingredientName = ingredientNode.asText().trim();
            String measure = measureNode != null ? measureNode.asText().trim() : "";

            if (!ingredientName.isEmpty()) {
                String displayName = measure.isEmpty() ? ingredientName : measure + " " + ingredientName;
                recipe.addIngredients(new Ingredient(displayName, 0, ""));
            }
        }

        String[] rawLines = instructionsRaw.split("\\r?\\n|\\.\\s+");
        for (String line : rawLines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;
            if (trimmed.matches("(?i)^step\\s*\\d+\\.?$")) continue;

            recipe.addSteps(trimmed.endsWith(".") ? trimmed : trimmed + ".");
        }

        return recipe;
    }

    private static Category mapCategory(String apiCategory) {
        String normalized = apiCategory.toLowerCase();
        if (normalized.contains("dessert")) return Category.DESSERT;
        if (normalized.contains("breakfast")) return Category.BREAKFAST;
        if (normalized.contains("starter") || normalized.contains("side")) return Category.APPETIZER;
        if (normalized.contains("vegan") || normalized.contains("vegetarian")) return Category.SNACK;
        if (normalized.contains("pasta")) return Category.LUNCH;
        return Category.DINNER;
    }
}