package com.recipeplanner.model;

public abstract class NutritionalRecipe extends Recipe {

    public NutritionalRecipe(String title, String description, Category category) {
        super(title, description, category);
    }

    @Override
    public abstract String getNutritionSummary();
}