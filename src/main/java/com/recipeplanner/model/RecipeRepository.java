package com.recipeplanner.model;

import java.util.ArrayList;
import java.util.List;

public class RecipeRepository {

    private static final RecipeRepository INSTANCE = new RecipeRepository();

    private final List<Recipe> recipes = new ArrayList<>();

    private RecipeRepository() {
        buildMockData();
    }

    public static RecipeRepository getInstance() {
        return INSTANCE;
    }

    public List<Recipe> getAllRecipes() {
        return recipes;
    }

    private void buildMockData() {
        Recipe toast = new Recipe("Plain Toast", "Just bread", Category.BREAKFAST);
        toast.addIngredients(new Ingredient("bread", 2.0, "slices"));
        toast.addSteps("Toast until golden");
        recipes.add(toast);

        SweetFood cake = new SweetFood("Chocolate Cake", "Rich chocolate dessert", Category.DESSERT, 45.0, 350, 60.0);
        cake.addIngredients(new Ingredient("cocoa powder", 0.5, "cups"));
        cake.addSteps("Bake at 350F for 30 minutes");
        recipes.add(cake);

        SavoryFood curry = new SavoryFood("Chicken Curry", "Spicy chicken curry", Category.DINNER,
                SavoryFood.SpiceLevel.HOT, 28.0, 620.0);
        curry.addIngredients(new Ingredient("chicken", 500.0, "g"));
        curry.addSteps("Simmer for 20 minutes");
        recipes.add(curry);

        SweetFood cookies = new SweetFood("Sugar Cookies", "Classic sweet cookies", Category.SNACK, 20.0, 150, 22.0);
        cookies.addIngredients(new Ingredient("sugar", 1.0, "cups"));
        cookies.addSteps("Bake at 375F for 10 minutes");
        recipes.add(cookies);
    }
}