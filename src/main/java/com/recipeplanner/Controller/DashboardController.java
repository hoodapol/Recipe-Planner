package com.recipeplanner.Controller;

import com.recipeplanner.model.*;
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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

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

    private static final String PILL_NORMAL = "-fx-background-color: #EADFCF; -fx-background-radius: 18; -fx-cursor: hand; -fx-padding: 8 20 8 20;";
    private static final String PILL_HOVER = "-fx-background-color: #DCC9AE; -fx-background-radius: 18; -fx-cursor: hand; -fx-padding: 8 20 8 20;";

    private final ExecutorService dbExecutor = Executors.newFixedThreadPool(2, runnable -> {
        Thread thread = new Thread(runnable, "db-worker");
        thread.setDaemon(true);
        return thread;
    });

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
            stage.getScene().setRoot(searchRoot);
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

        Label loading = new Label("Loading...");
        loading.setTextFill(Color.web("#6b5b4d"));
        contentArea.getChildren().add(loading);

        dbExecutor.execute(() -> {
            List<Recipe> favorites = Favorites.getInstance().getFavorites();

            Platform.runLater(() -> {
                if (!"favorites".equals(currentTab)) {
                    return;
                }
                contentArea.getChildren().clear();
                contentArea.getChildren().add(sectionTitle("Your Favorites"));

                if (favorites.isEmpty()) {
                    Label placeholder = new Label("(no favorites saved yet)");
                    placeholder.setTextFill(Color.web("#6b5b4d"));
                    contentArea.getChildren().add(placeholder);
                } else {
                    FlowPane grid = new FlowPane();
                    grid.setHgap(16);
                    grid.setVgap(16);
                    for (Recipe r : favorites) {
                        grid.getChildren().add(createRecipeCard(r));
                    }
                    contentArea.getChildren().add(grid);
                }
            });
        });
    }

    @FXML
    private void showHome() {
        currentTab = "home";
        setActiveButton(homeNavButton);
        contentArea.getChildren().clear();

        Label loading = new Label("Loading...");
        loading.setTextFill(Color.web("#6b5b4d"));
        contentArea.getChildren().add(loading);

        dbExecutor.execute(() -> {
            List<Recipe> allRecipes = RecipeRepository.getInstance().getAllRecipes();

            Platform.runLater(() -> {
                if (!"home".equals(currentTab)) {
                    return;
                }
                contentArea.getChildren().clear();

                Label greeting = new Label("Good to see you!");
                greeting.setTextFill(Color.web("#5c3a21"));
                greeting.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

                Label subGreeting = new Label("Explore your recipes and plan your next meal.");
                subGreeting.setTextFill(Color.web("#6b5b4d"));
                subGreeting.setFont(Font.font("Segoe UI", 13));

                FlowPane grid = new FlowPane();
                grid.setHgap(16);
                grid.setVgap(16);
                for (Recipe r : allRecipes) {
                    grid.getChildren().add(createRecipeCard(r));
                }

                contentArea.getChildren().addAll(greeting, subGreeting, sectionTitle("Recommended Recipes"), grid);
            });
        });
    }

    @FXML
    private void showCategories() {
        currentTab = "categories";
        setActiveButton(categoriesNavButton);
        contentArea.getChildren().clear();

        contentArea.getChildren().add(sectionTitle("Browse by Category"));

        Label subtitle = new Label("Pick a category to see matching recipes.");
        subtitle.setTextFill(Color.web("#6b5b4d"));
        subtitle.setFont(Font.font("Segoe UI", 13));
        contentArea.getChildren().add(subtitle);

        FlowPane pillRow = new FlowPane();
        pillRow.setHgap(12);
        pillRow.setVgap(12);
        for (Category category : Category.values()) {
            pillRow.getChildren().add(categoryPill(category));
        }
        contentArea.getChildren().add(pillRow);
    }

    private Label categoryPill(Category category) {
        Label pill = new Label(formatCategoryName(category));
        pill.setTextFill(Color.web("#5c3a21"));
        pill.setFont(Font.font("Segoe UI Semibold", 13));
        pill.setStyle(PILL_NORMAL);

        pill.setOnMouseEntered(e -> pill.setStyle(PILL_HOVER));
        pill.setOnMouseExited(e -> pill.setStyle(PILL_NORMAL));
        pill.setOnMouseClicked(e -> showRecipesForCategory(category));

        return pill;
    }

    private String formatCategoryName(Category category) {
        String name = category.toString();
        return name.charAt(0) + name.substring(1).toLowerCase();
    }

    private void showRecipesForCategory(Category category) {
        contentArea.getChildren().clear();

        Button backToCategories = new Button("← Categories");
        backToCategories.setStyle(PILL_NORMAL);
        backToCategories.setTextFill(Color.web("#5c3a21"));
        backToCategories.setFont(Font.font("Segoe UI Semibold", 12));
        backToCategories.setOnAction(e -> showCategories());
        backToCategories.setOnMouseEntered(e -> backToCategories.setStyle(PILL_HOVER));
        backToCategories.setOnMouseExited(e -> backToCategories.setStyle(PILL_NORMAL));

        contentArea.getChildren().add(backToCategories);
        contentArea.getChildren().add(sectionTitle(formatCategoryName(category) + " Recipes"));

        Label loading = new Label("Loading...");
        loading.setTextFill(Color.web("#6b5b4d"));
        contentArea.getChildren().add(loading);

        dbExecutor.execute(() -> {
            List<Recipe> matches = RecipeRepository.getInstance().getAllRecipes().stream()
                    .filter(r -> r.getCategory() == category)
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                if (!"categories".equals(currentTab)) {
                    return;
                }
                contentArea.getChildren().remove(loading);

                if (matches.isEmpty()) {
                    Label placeholder = new Label("No recipes found in this category yet.");
                    placeholder.setTextFill(Color.web("#6b5b4d"));
                    contentArea.getChildren().add(placeholder);
                } else {
                    FlowPane grid = new FlowPane();
                    grid.setHgap(16);
                    grid.setVgap(16);
                    for (Recipe r : matches) {
                        grid.getChildren().add(createRecipeCard(r));
                    }
                    contentArea.getChildren().add(grid);
                }
            });
        });
    }

    public void showTab(String tab) {
        switch (tab) {
            case "favorites" -> showFavorites();
            case "categories" -> showCategories();
            default -> showHome();
        }
    }

    private Label sectionTitle(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#5c3a21"));
        label.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        return label;
    }

    private VBox createRecipeCard(Recipe recipe) {
        double imageSize = 130;

        ImageView imageView = new ImageView();
        imageView.setFitWidth(imageSize);
        imageView.setFitHeight(imageSize);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(true);

        Rectangle clip = new Rectangle(imageSize, imageSize);
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        imageView.setClip(clip);

        String url = recipe.getImageUrl();
        if (url != null && !url.isEmpty()) {
            imageView.setImage(ImageCache.get(url));
        }

        Label titleLabel = new Label(recipe.getTitle());
        titleLabel.setTextFill(Color.web("#5c3a21"));
        titleLabel.setFont(Font.font("Segoe UI Semibold", 12));
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(imageSize);
        titleLabel.setAlignment(Pos.CENTER);
        titleLabel.setStyle("-fx-text-alignment: center;");

        VBox card = new VBox(8, imageView, titleLabel);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(imageSize);
        card.setPadding(new Insets(8));
        card.setStyle("-fx-background-color: #FFFDF9; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); -fx-cursor: hand;");

        card.setOnMouseClicked(e -> openDetail(recipe));
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #FFF8EE; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.14), 10, 0, 0, 3); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: #FFFDF9; -fx-background-radius: 14; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2); -fx-cursor: hand;"));

        return card;
    }

    private void openDetail(Recipe recipe) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/recipeplanner/detail-view.fxml"));
            Parent detailRoot = loader.load();

            DetailController detailController = loader.getController();
            detailController.setRecipe(recipe);
            detailController.setReturnTab(currentTab);

            Stage stage = (Stage) contentArea.getScene().getWindow();
            stage.getScene().setRoot(detailRoot);
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