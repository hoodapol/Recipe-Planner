package com.recipeplanner.Controller;

import com.recipeplanner.model.*;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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

    private String currentTab = "home";

    private Button activeNavButton;

    private static final String NAV_INACTIVE = "-fx-background-color: transparent; -fx-background-radius: 10;";
    private static final String NAV_HOVER = "-fx-background-color: #F3E9DC; -fx-background-radius: 10;";
    private static final String NAV_ACTIVE = "-fx-background-color: #EADFCF; -fx-background-radius: 10;";

    private static final String VIEW_BTN_NORMAL = "-fx-background-color: #A97C50; -fx-background-radius: 14; -fx-cursor: hand;";
    private static final String VIEW_BTN_HOVER = "-fx-background-color: #8C6239; -fx-background-radius: 14; -fx-cursor: hand;";

    @FXML
    public void initialize() {
        applyNavHover(homeNavButton);
        applyNavHover(favoritesNavButton);
        applyNavHover(categoriesNavButton);

        showHome();

        searchField.setOnMouseClicked(e -> openSearchScreen());
        searchButton.setOnAction(e -> openSearchScreen());
    }

    private void applyNavHover(Button button) {
        button.setOnMouseEntered(e -> {
            if (button != activeNavButton) {
                button.setStyle(NAV_HOVER);
            }
        });
        button.setOnMouseExited(e -> {
            if (button != activeNavButton) {
                button.setStyle(NAV_INACTIVE);
            }
        });
    }

    private void openSearchScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/recipeplanner/search-view.fxml"));
            Parent searchRoot = loader.load();

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(searchRoot, 600, 540));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showFavorites() {
        currentTab = "favorites";
        setActiveButton(favoritesNavButton);
        contentArea.getChildren().clear();

        contentArea.getChildren().add(sectionTitle("Your Favorites"));

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

    @FXML
    private void showHome() {
        currentTab = "home";
        setActiveButton(homeNavButton);
        contentArea.getChildren().clear();

        List<Recipe> allRecipes = RecipeRepository.getInstance().getAllRecipes();

        Label greeting = new Label("Good to see you!");
        greeting.setTextFill(Color.web("#5c3a21"));
        greeting.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        Label subGreeting = new Label("Explore your recipes and plan your next meal.");
        subGreeting.setTextFill(Color.web("#6b5b4d"));
        subGreeting.setFont(Font.font("Segoe UI", 13));

        HBox statsRow = new HBox(14,
                statCard("Recipes", String.valueOf(allRecipes.size())),
                statCard("Favorites", String.valueOf(Favorites.getInstance().getFavorites().size())),
                statCard("Categories", String.valueOf(Category.values().length))
        );

        contentArea.getChildren().addAll(greeting, subGreeting, statsRow, sectionTitle("Recommended Recipes"));
        for (Recipe r : allRecipes) {
            contentArea.getChildren().add(createRecipeRow(r));
        }
    }

    @FXML
    private void showCategories() {
        currentTab = "categories";
        setActiveButton(categoriesNavButton);
        contentArea.getChildren().clear();
        contentArea.getChildren().add(sectionTitle("Browse by Category"));

        Label placeholder = new Label("(category list coming soon)");
        placeholder.setTextFill(Color.web("#6b5b4d"));
        contentArea.getChildren().add(placeholder);
    }

    public void showTab(String tab) {
        switch (tab) {
            case "favorites" -> showFavorites();
            case "categories" -> showCategories();
            default -> showHome();
        }
    }

    private VBox statCard(String label, String value) {
        Label valueLabel = new Label(value);
        valueLabel.setTextFill(Color.web("#5c3a21"));
        valueLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));

        Label captionLabel = new Label(label);
        captionLabel.setTextFill(Color.web("#8a7768"));
        captionLabel.setFont(Font.font("Segoe UI", 11));

        VBox card = new VBox(4, valueLabel, captionLabel);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(120);
        card.setPadding(new Insets(14, 10, 14, 10));
        card.setStyle("-fx-background-color: #FFFDF9; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);");
        return card;
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#5c3a21"));
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        return label;
    }

    private HBox createRecipeRow(Recipe recipe) {
        Label nameLabel = new Label(recipe.getTitle() + "  (" + recipe.getCategory() + ")");
        nameLabel.setTextFill(Color.web("#5c3a21"));

        Button viewButton = new Button("View");
        viewButton.setStyle(VIEW_BTN_NORMAL);
        viewButton.setTextFill(Color.WHITE);
        viewButton.setOnAction(e -> openDetail(recipe));
        viewButton.setOnMouseEntered(e -> viewButton.setStyle(VIEW_BTN_HOVER));
        viewButton.setOnMouseExited(e -> viewButton.setStyle(VIEW_BTN_NORMAL));

        HBox spacer = new HBox();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(12, nameLabel, spacer, viewButton);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10, 14, 10, 14));
        row.setStyle("-fx-background-color: #FFFDF9; -fx-background-radius: 10;");

        return row;
    }

    private void openDetail(Recipe recipe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/recipeplanner/detail-view.fxml"));
            Parent detailRoot = loader.load();

            DetailController detailController = loader.getController();
            detailController.setRecipe(recipe);
            detailController.setReturnTab(currentTab);

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.setScene(new Scene(detailRoot, 720, 500));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button active) {
        homeNavButton.setStyle(NAV_INACTIVE);
        favoritesNavButton.setStyle(NAV_INACTIVE);
        categoriesNavButton.setStyle(NAV_INACTIVE);
        activeNavButton = active;
        if (active != null) {
            active.setStyle(NAV_ACTIVE);
        }
    }
}