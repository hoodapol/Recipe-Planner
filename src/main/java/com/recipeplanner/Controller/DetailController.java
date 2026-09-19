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
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DetailController {

    @FXML
    private StackPane headerStack;

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
    private Label ingredientsHeaderLabel;

    @FXML
    private Label stepsHeaderLabel;

    @FXML
    private ListView<String> ingredientsListView;

    @FXML
    private ListView<String> stepsListView;

    private Recipe currentRecipe;

    private String returnTab = "home";

    private static final String BACK_BTN_NORMAL = "-fx-background-color: #A97C50; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 5 16 5 16;";
    private static final String BACK_BTN_HOVER = "-fx-background-color: #8C6239; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 5 16 5 16;";

    private static final String FAV_BTN_NORMAL = "-fx-background-color: #A97C50; -fx-background-radius: 21; -fx-cursor: hand;";
    private static final String FAV_BTN_HOVER = "-fx-background-color: #8C6239; -fx-background-radius: 21; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        backButton.setOnMouseEntered(e -> backButton.setStyle(BACK_BTN_HOVER));
        backButton.setOnMouseExited(e -> backButton.setStyle(BACK_BTN_NORMAL));

        favoriteButton.setOnMouseEntered(e -> favoriteButton.setStyle(FAV_BTN_HOVER));
        favoriteButton.setOnMouseExited(e -> favoriteButton.setStyle(FAV_BTN_NORMAL));
    }

    public void setReturnTab(String tab) {
        this.returnTab = tab;
    }

    public void setRecipe(Recipe recipe) {
        this.currentRecipe = recipe;

        titleLabel.setText(recipe.getTitle());
        categoryLabel.setText(recipe.getCategory().toString());
        descriptionLabel.setText(recipe.getDescription());
        nutritionLabel.setText(recipe.getNutritionSummary());

        List<String> ingredientStrings = recipe.getIngredients().stream()
                .map(Ingredient::toString)
                .collect(Collectors.toList());
        ingredientsListView.setItems(FXCollections.observableArrayList(ingredientStrings));
        ingredientsHeaderLabel.setText("INGREDIENTS (" + ingredientStrings.size() + ")");

        List<String> steps = recipe.getSteps();
        List<String> numberedSteps = new ArrayList<>();
        for (int i = 0; i < steps.size(); i++) {
            numberedSteps.add((i + 1) + ". " + steps.get(i));
        }
        stepsListView.setItems(FXCollections.observableArrayList(numberedSteps));
        stepsHeaderLabel.setText("STEPS (" + steps.size() + ")");

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
        favoriteButton.setText(isFav ? "♥  Remove from Favorites" : "♡  Save to Favorites");
    }

    @FXML
    private void handleBack() {
        try {
            String targetFxml = "search".equals(returnTab)
                    ? "/com/recipeplanner/search-view.fxml"
                    : "/com/recipeplanner/dashboard-view.fxml";

            FXMLLoader loader = new FXMLLoader(getClass().getResource(targetFxml));
            Parent root = loader.load();

            if (!"search".equals(returnTab)) {
                DashboardController dashboardController = loader.getController();
                dashboardController.showTab(returnTab);
            }

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 720, 500));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}