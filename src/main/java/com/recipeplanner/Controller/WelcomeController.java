package com.recipeplanner.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;

public class WelcomeController {

    @FXML
    private Button getStartedButton;

    @FXML
    private StackPane headerStack;

    @FXML
    private Rectangle headerRect;

    @FXML
    public void initialize() {
        headerRect.widthProperty().bind(headerStack.widthProperty());
    }

    @FXML
    private void handleGetStarted() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/recipeplanner/dashboard-view.fxml"));
        Parent dashboardRoot = loader.load();

        Stage stage = (Stage) getStartedButton.getScene().getWindow();
        stage.setScene(new Scene(dashboardRoot, 720, 500));
    }
}