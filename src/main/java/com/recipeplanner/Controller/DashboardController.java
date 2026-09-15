package com.recipeplanner.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

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

    @FXML
    public void initialize() {
        showHome();
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        contentArea.getChildren().clear();
        Label resultLabel = new Label("Search results for: " + query + " (coming in Phase 3)");
        resultLabel.setTextFill(javafx.scene.paint.Color.web("#6b5b4d"));
        contentArea.getChildren().add(resultLabel);
    }

    @FXML
    private void showHome() {
        setActiveButton(homeNavButton);
        contentArea.getChildren().clear();
        Label title = new Label("Recommended Recipes");
        title.setTextFill(javafx.scene.paint.Color.web("#5c3a21"));
        Label placeholder = new Label("(coming soon — recipe cards will appear here)");
        placeholder.setTextFill(javafx.scene.paint.Color.web("#6b5b4d"));
        contentArea.getChildren().addAll(title, placeholder);
    }

    @FXML
    private void showFavorites() {
        setActiveButton(favoritesNavButton);
        contentArea.getChildren().clear();
        Label title = new Label("Your Favorites");
        title.setTextFill(javafx.scene.paint.Color.web("#5c3a21"));
        Label placeholder = new Label("(no favorites saved yet)");
        placeholder.setTextFill(javafx.scene.paint.Color.web("#6b5b4d"));
        contentArea.getChildren().addAll(title, placeholder);
    }

    @FXML
    private void showCategories() {
        setActiveButton(categoriesNavButton);
        contentArea.getChildren().clear();
        Label title = new Label("Browse by Category");
        title.setTextFill(javafx.scene.paint.Color.web("#5c3a21"));
        Label placeholder = new Label("(category list coming soon)");
        placeholder.setTextFill(javafx.scene.paint.Color.web("#6b5b4d"));
        contentArea.getChildren().addAll(title, placeholder);
    }

    private void setActiveButton(Button active) {
        homeNavButton.setStyle("-fx-background-color: transparent;");
        favoritesNavButton.setStyle("-fx-background-color: transparent;");
        categoriesNavButton.setStyle("-fx-background-color: transparent;");
        active.setStyle("-fx-background-color: #EADFCF;");
    }
}