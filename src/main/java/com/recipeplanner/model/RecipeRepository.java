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

        List<Recipe> balanced = RecipeApiClient.fetchBalancedRecipes(3);

        Set<String> alreadyFetchedIds = balanced.stream()
                .map(Recipe::getExternalId)
                .collect(Collectors.toSet());

        List<Recipe> extraRandom = RecipeApiClient.fetchAdditionalRandomRecipes(18, alreadyFetchedIds);

        for (Recipe recipe : balanced) {
            DatabaseManager.insertRecipe(recipe);
        }
        for (Recipe recipe : extraRandom) {
            DatabaseManager.insertRecipe(recipe);
        }

        int sampleCount = seedSweetAndSavorySamples();

        int total = balanced.size() + extraRandom.size() + sampleCount;
        System.out.println("Seeded " + total + " recipes (" + balanced.size()
                + " guaranteeing category coverage, " + extraRandom.size()
                + " random extras, " + sampleCount + " SweetFood/SavoryFood samples).");
    }

    private int seedSweetAndSavorySamples() {
        SweetFood cake = new SweetFood("Rich Chocolate Cake", "A moist, decadent chocolate cake perfect for celebrations.", Category.DESSERT, 42.0, 380, 55.0);
        cake.addIngredients(new Ingredient("all-purpose flour", 2.0, "cups"));
        cake.addIngredients(new Ingredient("cocoa powder", 0.75, "cups"));
        cake.addIngredients(new Ingredient("sugar", 2.0, "cups"));
        cake.addIngredients(new Ingredient("eggs", 2.0, ""));
        cake.addIngredients(new Ingredient("milk", 1.0, "cups"));
        cake.addSteps("Preheat oven to 350F and grease two 9-inch cake pans.");
        cake.addSteps("Mix dry ingredients, then add eggs and milk until smooth.");
        cake.addSteps("Divide batter into pans and bake for 30-35 minutes.");
        cake.addSteps("Let cool completely before frosting.");
        DatabaseManager.insertRecipe(cake);

        SweetFood cookies = new SweetFood("Classic Sugar Cookies", "Soft, chewy sugar cookies with crisp golden edges.", Category.SNACK, 18.0, 140, 20.0);
        cookies.addIngredients(new Ingredient("all-purpose flour", 2.75, "cups"));
        cookies.addIngredients(new Ingredient("butter", 1.0, "cups"));
        cookies.addIngredients(new Ingredient("sugar", 1.5, "cups"));
        cookies.addIngredients(new Ingredient("egg", 1.0, ""));
        cookies.addSteps("Preheat oven to 375F and line baking sheets with parchment paper.");
        cookies.addSteps("Cream butter and sugar, then beat in egg.");
        cookies.addSteps("Mix in flour until just combined, then roll into balls.");
        cookies.addSteps("Bake for 8-10 minutes until edges are lightly golden.");
        DatabaseManager.insertRecipe(cookies);

        SavoryFood curry = new SavoryFood("Chicken Curry", "A warm, spiced chicken curry simmered in a rich tomato-based sauce.", Category.DINNER,
                SavoryFood.SpiceLevel.HOT, 32.0, 640.0);
        curry.addIngredients(new Ingredient("chicken thighs", 600.0, "g"));
        curry.addIngredients(new Ingredient("onion", 1.0, ""));
        curry.addIngredients(new Ingredient("garlic cloves", 3.0, ""));
        curry.addIngredients(new Ingredient("curry powder", 2.0, "tbsp"));
        curry.addIngredients(new Ingredient("coconut milk", 1.0, "cups"));
        curry.addSteps("Sauté chopped onion and garlic until soft.");
        curry.addSteps("Stir in curry powder, then add chicken and brown on all sides.");
        curry.addSteps("Add coconut milk and simmer for 20-25 minutes until tender.");
        curry.addSteps("Serve hot with rice or naan.");
        DatabaseManager.insertRecipe(curry);

        SavoryFood soup = new SavoryFood("Spicy Vegetable Soup", "A hearty, warming soup with a gentle kick of spice.", Category.LUNCH,
                SavoryFood.SpiceLevel.MEDIUM, 8.0, 480.0);
        soup.addIngredients(new Ingredient("carrots", 3.0, ""));
        soup.addIngredients(new Ingredient("celery stalks", 2.0, ""));
        soup.addIngredients(new Ingredient("vegetable broth", 4.0, "cups"));
        soup.addIngredients(new Ingredient("chili flakes", 1.0, "tsp"));
        soup.addSteps("Chop all vegetables into bite-sized pieces.");
        soup.addSteps("Sauté vegetables in a large pot for 5 minutes.");
        soup.addSteps("Add broth and chili flakes, bring to a boil.");
        soup.addSteps("Reduce heat and simmer for 20 minutes until vegetables are tender.");
        DatabaseManager.insertRecipe(soup);

        return 4;
    }
}