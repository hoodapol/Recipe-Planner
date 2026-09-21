package com.recipeplanner.model;

import java.util.List;

public class Favorites {

    private static final Favorites INSTANCE = new Favorites();

    private Favorites() {
    }

    public static Favorites getInstance() {
        return INSTANCE;
    }

    public void addFavorite(Recipe recipe) {
        if (recipe.getId() == -1) {
            return;
        }
        DatabaseManager.addFavoriteRecipe(recipe.getId());
    }

    public void removeFavorite(Recipe recipe) {
        if (recipe.getId() == -1) {
            return;
        }
        DatabaseManager.removeFavoriteRecipe(recipe.getId());
    }

    public boolean isFavorite(Recipe recipe) {
        if (recipe.getId() == -1) {
            return false;
        }
        return DatabaseManager.isFavorite(recipe.getId());
    }

    public List<Recipe> getFavorites() {
        return DatabaseManager.getFavoriteRecipes();
    }
}