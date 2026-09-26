package com.recipeplanner.Controller;

import com.recipeplanner.model.DatabaseManager;
import com.recipeplanner.model.Favorites;
import com.recipeplanner.model.Ingredient;
import com.recipeplanner.model.ImageCache;
import com.recipeplanner.model.Recipe;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class DetailController {

    @FXML
    private StackPane headerStack;

    @FXML
    private StackPane imageContainer;

    @FXML
    private javafx.scene.image.ImageView recipeImageView;

    @FXML
    private Button backButton;

    @FXML
    private Button favoriteButton;

    @FXML
    private Button editDescriptionButton;

    @FXML
    private Button deleteButton;

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

    private final ExecutorService dbExecutor = Executors.newFixedThreadPool(1, runnable -> {
        Thread thread = new Thread(runnable, "detail-db-worker");
        thread.setDaemon(true);
        return thread;
    });

    private static final String BACK_BTN_NORMAL = "-fx-background-color: #A97C50; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 5 16 5 16;";
    private static final String BACK_BTN_HOVER = "-fx-background-color: #8C6239; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 5 16 5 16;";

    private static final String FAV_BTN_NORMAL = "-fx-background-color: #A97C50; -fx-background-radius: 21; -fx-cursor: hand;";
    private static final String FAV_BTN_HOVER = "-fx-background-color: #8C6239; -fx-background-radius: 21; -fx-cursor: hand;";

    private static final String DELETE_BTN_NORMAL = "-fx-background-color: transparent; -fx-border-color: #B33A3A; -fx-border-radius: 16; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 6 18 6 18;";
    private static final String DELETE_BTN_HOVER = "-fx-background-color: #FBEAEA; -fx-border-color: #B33A3A; -fx-border-radius: 16; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 6 18 6 18;";

    @FXML
    public void initialize() {
        backButton.setOnMouseEntered(e -> backButton.setStyle(BACK_BTN_HOVER));
        backButton.setOnMouseExited(e -> backButton.setStyle(BACK_BTN_NORMAL));

        favoriteButton.setOnMouseEntered(e -> favoriteButton.setStyle(FAV_BTN_HOVER));
        favoriteButton.setOnMouseExited(e -> favoriteButton.setStyle(FAV_BTN_NORMAL));

        deleteButton.setOnMouseEntered(e -> deleteButton.setStyle(DELETE_BTN_HOVER));
        deleteButton.setOnMouseExited(e -> deleteButton.setStyle(DELETE_BTN_NORMAL));

        Rectangle clip = new Rectangle(560, 220);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        recipeImageView.setClip(clip);
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

        loadRecipeImage(recipe.getImageUrl());

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

        updateFavoriteButtonText(Favorites.getInstance().isFavorite(recipe));
    }

    private void loadRecipeImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            imageContainer.setVisible(false);
            imageContainer.setManaged(false);
            return;
        }

        imageContainer.setVisible(true);
        imageContainer.setManaged(true);
        recipeImageView.setImage(ImageCache.get(imageUrl));
    }

    @FXML
    private void handleToggleFavorite() {
        Favorites favorites = Favorites.getInstance();
        if (favorites.isFavorite(currentRecipe)) {
            favorites.removeFavorite(currentRecipe);
        } else {
            favorites.addFavorite(currentRecipe);
        }
        updateFavoriteButtonText(favorites.isFavorite(currentRecipe));
    }

    private void updateFavoriteButtonText(boolean isFavorite) {
        favoriteButton.setText(isFavorite ? "♥  Remove from Favorites" : "♡  Save to Favorites");
    }

    @FXML
    private void handleEditDescription() {
        TextInputDialog dialog = new TextInputDialog(currentRecipe.getDescription());
        dialog.setTitle("Edit Description");
        dialog.setHeaderText("Update the description for \"" + currentRecipe.getTitle() + "\"");
        dialog.setContentText("Description:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newDescription -> {
            String trimmed = newDescription.trim();
            if (trimmed.isEmpty()) {
                return;
            }

            editDescriptionButton.setDisable(true);

            dbExecutor.execute(() -> {
                DatabaseManager.updateRecipeDescription(currentRecipe.getId(), trimmed);

                Platform.runLater(() -> {
                    currentRecipe.setDescription(trimmed);
                    descriptionLabel.setText(trimmed);
                    editDescriptionButton.setDisable(false);
                });
            });
        });
    }

    @FXML
    private void handleDeleteRecipe() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Recipe");
        confirm.setHeaderText("Delete \"" + currentRecipe.getTitle() + "\"?");
        confirm.setContentText("This will permanently remove the recipe, its ingredients, steps, and remove it from Favorites if saved there.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            deleteButton.setDisable(true);

            dbExecutor.execute(() -> {
                DatabaseManager.deleteRecipe(currentRecipe.getId());

                Platform.runLater(this::handleBack);
            });
        }
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
            stage.getScene().setRoot(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}