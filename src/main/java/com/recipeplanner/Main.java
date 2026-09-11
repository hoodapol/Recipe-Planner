package com.recipeplanner;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label placeholder = new Label("Starting");
        StackPane root = new StackPane(placeholder);
        Scene scene = new Scene(root, 600, 400);

        primaryStage.setTitle("Recipe Planner");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}