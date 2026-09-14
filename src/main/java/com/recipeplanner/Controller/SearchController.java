package com.recipeplanner.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class SearchController {

    @FXML
    private TextField searchField;

    @FXML
    private Button searchButton;

    @FXML
    private Label resultLabel;

    @FXML
    private void handleSearch() {
        String query = searchField.getText();
        resultLabel.setText("You searched for: " + query);
    }
}