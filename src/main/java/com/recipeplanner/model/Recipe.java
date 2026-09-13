package com.recipeplanner.model;

import java.util.ArrayList;
import java.util.List;

public class Recipe {
    private String title, description;
    private Category category;
    private List<Ingredient> ingredients;
    private List<String> steps;

    public Recipe(String title, String description, Category category) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.ingredients = new ArrayList<>();
        this.steps = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void addIngredients(Ingredient ingredient) {
        ingredients.add(ingredient);
    }

    public List<String> getSteps() {
        return steps;
    }

    public void addSteps(String step) {
        steps.add(step);
    }

    @Override
    public String toString() {
        return title + " (" + category + ") - " + description
                + "\nIngredients: " + ingredients
                + "\nSteps: " + steps;
    }

    public String getNutritionSummary() {
        return "No detailed nutrition info available for this recipe.";
    }
}
