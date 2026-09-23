package com.recipeplanner.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class RecipeApiClient {

    public static List<Recipe> fetchRecipesByFirstLetter(String letter) {
        List<Recipe> recipes = new ArrayList<>();

        try {
            HttpClient client = HttpClient.newHttpClient();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://www.themealdb.com/api/json/v1/1/search.php?f=" + letter))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

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
            System.out.println("Error fetching recipes: " + e.getMessage());
        }

        return recipes;
    }

    private static Recipe parseRecipe(JsonNode meal) {
        String title = meal.get("strMeal").asText();
        String instructionsRaw = meal.get("strInstructions").asText();
        String apiCategory = meal.get("strCategory") != null ? meal.get("strCategory").asText() : "";

        Category category = mapCategory(apiCategory);

        Recipe recipe = new Recipe(title, "Imported from TheMealDB (" + apiCategory + ")", category);

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
        if (normalized.contains("snack") || normalized.contains("vegan") || normalized.contains("vegetarian")) return Category.SNACK;
        return Category.DINNER;
    }
}