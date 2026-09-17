package com.recipeplanner.Controller;

import com.recipeplanner.model.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DashboardController {

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Button homeNavButton;

    @FXML
    private Button favoritesNavButton;

    @FXML
    private Button categoriesNavButton;

    @FXML
    private VBox contentArea;

    private static final List<Recipe> mockRecipes = new ArrayList<>();

    private String currentTab = "home";

    private final ExecutorService searchExecutor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "search-worker");
        thread.setDaemon(true);
        return thread;
    });

    @FXML
    public void initialize() {
        if (mockRecipes.isEmpty()) {
            buildMockData();
        }
        showHome();
        searchField.setOnAction(e -> handleSearch());
    }

    private void buildMockData() {
        Recipe toast = new Recipe("Plain Toast", "Just bread", Category.BREAKFAST);
        toast.addIngredients(new Ingredient("bread", 2.0, "slices"));
        toast.addSteps("Toast until golden");
        mockRecipes.add(toast);

        SweetFood cake = new SweetFood("Chocolate Cake", "Rich chocolate dessert", Category.DESSERT, 45.0, 350, 60.0);
        cake.addIngredients(new Ingredient("cocoa powder", 0.5, "cups"));
        cake.addSteps("Bake at 350F for 30 minutes");
        mockRecipes.add(cake);

        SavoryFood curry = new SavoryFood("Chicken Curry", "Spicy chicken curry", Category.DINNER,
                SavoryFood.SpiceLevel.HOT, 28.0, 620.0);
        curry.addIngredients(new Ingredient("chicken", 500.0, "g"));
        curry.addSteps("Simmer for 20 minutes");
        mockRecipes.add(curry);

        SweetFood cookies = new SweetFood("Sugar Cookies", "Classic sweet cookies", Category.SNACK, 20.0, 150, 22.0);
        cookies.addIngredients(new Ingredient("sugar", 1.0, "cups"));
        cookies.addSteps("Bake at 375F for 10 minutes");
        mockRecipes.add(cookies);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim().toLowerCase();

        searchExecutor.execute(() -> {
            List<Recipe> matches = new ArrayList<>();
            for (Recipe r : mockRecipes) {
                boolean titleMatches = r.getTitle().toLowerCase().contains(query);
                boolean categoryMatches = r.getCategory().toString().toLowerCase().contains(query);
                if (titleMatches || categoryMatches) {
                    matches.add(r);
                }
            }

            Platform.runLater(() -> displaySearchResults(query, matches));
        });
    }

    private void displaySearchResults(String query, List<Recipe> matches) {
        currentTab = "search";
        setActiveButton(null);
        contentArea.getChildren().clear();

        Label title = new Label("Results for: " + query);
        title.setTextFill(Color.web("#5c3a21"));
        contentArea.getChildren().add(title);

        if (matches.isEmpty()) {
            Label noResults = new Label("No recipes found.");
            noResults.setTextFill(Color.web("#6b5b4d"));
            contentArea.getChildren().add(noResults);
        } else {
            for (Recipe r : matches) {
                contentArea.getChildren().add(createRecipeRow(r));
            }
        }
    }

    @FXML
    private void showFavorites() {
        currentTab = "favorites";
        setActiveButton(favoritesNavButton);
        contentArea.getChildren().clear();
        Label title = new Label("Your Favorites");
        title.setTextFill(Color.web("#5c3a21"));
        contentArea.getChildren().add(title);

        List<Recipe> favorites = Favorites.getInstance().getFavorites();
        if (favorites.isEmpty()) {
            Label placeholder = new Label("(no favorites saved yet)");
            placeholder.setTextFill(Color.web("#6b5b4d"));
            contentArea.getChildren().add(placeholder);
        } else {
            for (Recipe r : favorites) {
                contentArea.getChildren().add(createRecipeRow(r));
            }
        }
    }

    private javafx.scene.layout.HBox createRecipeRow(Recipe recipe) {
        Label nameLabel = new Label(recipe.getTitle() + "  (" + recipe.getCategory() + ")");
        nameLabel.setTextFill(Color.web("#5c3a21"));

        Button viewButton = new Button("View");
        viewButton.setStyle("-fx-background-color: #A97C50;");
        viewButton.setTextFill(Color.WHITE);
        viewButton.setOnAction(e -> openDetail(recipe));

        javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(12, nameLabel, viewButton);
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        return row;
    }

    private void openDetail(Recipe recipe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/recipeplanner/detail-view.fxml"));
            Parent detailRoot = loader.load();

            DetailController detailController = loader.getController();
            detailController.setRecipe(recipe);
            detailController.setReturnTab(currentTab);

            javafx.stage.Stage stage = (javafx.stage.Stage) contentArea.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(detailRoot, 720, 500));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showHome() {
        currentTab = "home";
        setActiveButton(homeNavButton);
        contentArea.getChildren().clear();
        Label title = new Label("Recommended Recipes");
        title.setTextFill(Color.web("#5c3a21"));
        contentArea.getChildren().add(title);
        for (Recipe r : mockRecipes) {
            contentArea.getChildren().add(createRecipeRow(r));
        }
    }

    @FXML
    private void showCategories() {
        currentTab = "categories";
        setActiveButton(categoriesNavButton);
        contentArea.getChildren().clear();
        Label title = new Label("Browse by Category");
        title.setTextFill(Color.web("#5c3a21"));
        Label placeholder = new Label("(category list coming soon)");
        placeholder.setTextFill(Color.web("#6b5b4d"));
        contentArea.getChildren().addAll(title, placeholder);
    }

    /**
     * Called from DetailController after reloading the dashboard,
     * so Back navigation restores whichever tab was active before.
     */
    public void showTab(String tab) {
        switch (tab) {
            case "favorites" -> showFavorites();
            case "categories" -> showCategories();
            default -> showHome();
        }
    }

    private void setActiveButton(Button active) {
        homeNavButton.setStyle("-fx-background-color: transparent;");
        favoritesNavButton.setStyle("-fx-background-color: transparent;");
        categoriesNavButton.setStyle("-fx-background-color: transparent;");
        if (active != null) {
            active.setStyle("-fx-background-color: #EADFCF;");
        }
    }
}