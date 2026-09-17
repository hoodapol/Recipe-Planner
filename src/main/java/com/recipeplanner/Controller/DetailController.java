package com.recipeplanner.Controller;

import com.recipeplanner.model.Favorites;
import com.recipeplanner.model.Ingredient;
import com.recipeplanner.model.Recipe;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class DetailController {

    @FXML
    private StackPane headerStack;

    @FXML
    private Rectangle headerRect;

    @FXML
    private Button backButton;

    @FXML
    private Button favoriteButton;

    @FXML
    private Label titleLabel;

    @FXML
    private Label categoryLabel;

    @FXML
    private Label descriptionLabel;

    @FXML
    private Label nutritionLabel;

    @FXML
    private ListView<String> ingredientsListView;

    @FXML
    private ListView<String> stepsListView;

    private Recipe currentRecipe;

    @FXML
    public void initialize() {
        headerRect.widthProperty().bind(headerStack.widthProperty());
    }

    private String returnTab = "home";

    public void setReturnTab(String tab) {
        this.returnTab = tab;
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/recipeplanner/dashboard-view.fxml"));
            Parent dashboardRoot = loader.load();

            DashboardController dashboardController = loader.getController();
            dashboardController.showTab(returnTab);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(dashboardRoot, 720, 500));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRecipe(Recipe recipe) {
        this.currentRecipe = recipe;

        titleLabel.setText(recipe.getTitle());
        categoryLabel.setText("Category: " + recipe.getCategory());
        descriptionLabel.setText(recipe.getDescription());
        nutritionLabel.setText(recipe.getNutritionSummary());

        List<String> ingredientStrings = recipe.getIngredients().stream()
                .map(Ingredient::toString)
                .collect(Collectors.toList());
        ingredientsListView.setItems(FXCollections.observableArrayList(ingredientStrings));

        stepsListView.setItems(FXCollections.observableArrayList(recipe.getSteps()));

        updateFavoriteButtonText();
    }

    @FXML
    private void handleToggleFavorite() {
        Favorites favorites = Favorites.getInstance();
        if (favorites.isFavorite(currentRecipe)) {
            favorites.removeFavorite(currentRecipe);
        } else {
            favorites.addFavorite(currentRecipe);
        }
        updateFavoriteButtonText();
    }

    private void updateFavoriteButtonText() {
        boolean isFav = Favorites.getInstance().isFavorite(currentRecipe);
        favoriteButton.setText(isFav ? "Remove from Favorites" : "Save to Favorites");
    }

}