package com.recipeplanner.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {

    @FXML
    private Button getStartedButton;

    @FXML
    public void initialize() {
        String normalStyle = "-fx-background-color: #A97C50; -fx-background-radius: 21; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #8C6239; -fx-background-radius: 21; -fx-font-family: 'Segoe UI'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-cursor: hand;";

        getStartedButton.setOnMouseEntered(e -> getStartedButton.setStyle(hoverStyle));
        getStartedButton.setOnMouseExited(e -> getStartedButton.setStyle(normalStyle));
    }

    @FXML
    private void handleGetStarted() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/recipeplanner/dashboard-view.fxml"));
        Parent dashboardRoot = loader.load();

        Stage stage = (Stage) getStartedButton.getScene().getWindow();
        stage.getScene().setRoot(dashboardRoot);
    }
}