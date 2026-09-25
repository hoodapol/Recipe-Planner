package com.recipeplanner.Controller;

import com.recipeplanner.model.Recipe;
import com.recipeplanner.model.RecipeRepository;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class SearchController {

    @FXML
    private Button backButton;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Label resultLabel;

    @FXML
    private VBox resultsContainer;

    private final ExecutorService searchExecutor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "search-worker");
        thread.setDaemon(true);
        return thread;
    });

    private final AtomicLong requestCounter = new AtomicLong(0);

    private static final String BACK_BTN_NORMAL = "-fx-background-color: #A97C50; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 5 16 5 16;";
    private static final String BACK_BTN_HOVER = "-fx-background-color: #8C6239; -fx-background-radius: 16; -fx-cursor: hand; -fx-padding: 5 16 5 16;";

    private static final String SEARCH_BTN_NORMAL = "-fx-background-color: #A97C50; -fx-background-radius: 20; -fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 0 22 0 22;";
    private static final String SEARCH_BTN_HOVER = "-fx-background-color: #8C6239; -fx-background-radius: 20; -fx-font-family: 'Segoe UI Semibold'; -fx-font-size: 13px; -fx-cursor: hand; -fx-padding: 0 22 0 22;";

    @FXML
    public void initialize() {
        displayResults(RecipeRepository.getInstance().getAllRecipes());

        searchField.textProperty().addListener((obs, oldText, newText) -> runSearch(newText));
        searchField.setOnAction(e -> runSearch(searchField.getText()));

        backButton.setOnMouseEntered(e -> backButton.setStyle(BACK_BTN_HOVER));
        backButton.setOnMouseExited(e -> backButton.setStyle(BACK_BTN_NORMAL));

        searchButton.setOnMouseEntered(e -> searchButton.setStyle(SEARCH_BTN_HOVER));
        searchButton.setOnMouseExited(e -> searchButton.setStyle(SEARCH_BTN_NORMAL));
    }

    @FXML
    private void handleSearch() {
        runSearch(searchField.getText());
    }

    private void runSearch(String query) {
        String lowerQuery = query.trim().toLowerCase();
        long thisRequestId = requestCounter.incrementAndGet();

        searchExecutor.execute(() -> {
            List<Recipe> matches = new ArrayList<>();
            for (Recipe r : RecipeRepository.getInstance().getAllRecipes()) {
                boolean titleMatches = r.getTitle().toLowerCase().contains(lowerQuery);
                boolean categoryMatches = r.getCategory().toString().toLowerCase().contains(lowerQuery);
                if (lowerQuery.isEmpty() || titleMatches || categoryMatches) {
                    matches.add(r);
                }
            }

            Platform.runLater(() -> {
                if (thisRequestId != requestCounter.get()) {
                    return;
                }

                if (lowerQuery.isEmpty()) {
                    resultLabel.setText("");
                } else {
                    resultLabel.setText(matches.size() + " result(s) for \"" + query + "\"");
                }
                displayResults(matches);
            });
        });
    }

    private void displayResults(List<Recipe> recipes) {
        resultsContainer.getChildren().clear();

        if (recipes.isEmpty()) {
            Label noResults = new Label("No recipes found.");
            noResults.setTextFill(Color.web("#6b5b4d"));
            resultsContainer.getChildren().add(noResults);
            return;
        }

        for (Recipe r : recipes) {
            resultsContainer.getChildren().add(createRecipeRow(r));
        }
    }

    private HBox createRecipeRow(Recipe recipe) {
        Label nameLabel = new Label(recipe.getTitle() + "  (" + recipe.getCategory() + ")");
        nameLabel.setTextFill(Color.web("#5c3a21"));

        Button viewButton = new Button("View");
        viewButton.setStyle("-fx-background-color: #A97C50; -fx-background-radius: 14; -fx-cursor: hand;");
        viewButton.setTextFill(Color.WHITE);
        viewButton.setOnAction(e -> openDetail(recipe));
        viewButton.setOnMouseEntered(e -> viewButton.setStyle("-fx-background-color: #8C6239; -fx-background-radius: 14; -fx-cursor: hand;"));
        viewButton.setOnMouseExited(e -> viewButton.setStyle("-fx-background-color: #A97C50; -fx-background-radius: 14; -fx-cursor: hand;"));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, nameLabel, spacer, viewButton);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: #FDFBF7; -fx-background-radius: 10;");

        return row;
    }

    private void openDetail(Recipe recipe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/recipeplanner/detail-view.fxml"));
            Parent detailRoot = loader.load();

            DetailController detailController = loader.getController();
            detailController.setRecipe(recipe);
            detailController.setReturnTab("search");

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(detailRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/recipeplanner/dashboard-view.fxml"));
            Parent dashboardRoot = loader.load();

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.getScene().setRoot(dashboardRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}