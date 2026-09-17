package com.recipeplanner.model;

import java.util.ArrayList;
import java.util.List;

public class Favorites {

    private static final Favorites INSTANCE = new Favorites();

    private final List<Recipe> favorites = new ArrayList<>();

    private Favorites() {
    }

    public static Favorites getInstance() {
        return INSTANCE;
    }

    public void addFavorite(Recipe recipe) {
        if (!favorites.contains(recipe)) {
            favorites.add(recipe);
        }
    }

    public void removeFavorite(Recipe recipe) {
        favorites.remove(recipe);
    }

    public boolean isFavorite(Recipe recipe) {
        return favorites.contains(recipe);
    }

    public List<Recipe> getFavorites() {
        return favorites;
    }
}