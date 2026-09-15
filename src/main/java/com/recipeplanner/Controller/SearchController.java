package com.recipeplanner.Controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

public class SearchController {

    @FXML
    private StackPane headerStack;

    @FXML
    private Rectangle headerRect;

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Label resultLabel;

    @FXML
    private ListView<String> resultsListView;

    @FXML
    public void initialize() {
        headerRect.widthProperty().bind(headerStack.widthProperty());

        resultsListView.setItems(FXCollections.observableArrayList(
                "Search results will appear here"
        ));
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        resultLabel.setText("You searched for: " + query);
    }
}