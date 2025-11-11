package com.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    public static LogCtrl logCtrl;

    public static void main(String[] args) {
        // Iniciar app JavaFX   
        launch(args);
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        try {
            final int windowWidth = 1200;
            final int windowHeight = 650;

            UtilsViews.parentContainer.setStyle("-fx-font: 14 arial;");
            UtilsViews.addView(getClass(), "ViewLog", "/assets/logView.fxml");

            logCtrl = (LogCtrl) UtilsViews.getController("ViewLog");
            
            Scene scene = new Scene(UtilsViews.parentContainer, windowWidth, windowHeight);

            UtilsViews.setStage(stage);
            stage.setScene(scene);
            stage.setTitle("MatrixPlayPong");
            stage.setMinWidth(windowWidth);
            stage.setMinHeight(windowHeight);
            
            // Add icon (fixed path)
            try {
                Image icon = new Image(getClass().getResourceAsStream("/icons/icon.png"));
                stage.getIcons().add(icon);
            } catch (Exception e) {
                System.err.println("No se pudo cargar el icono: " + e.getMessage());
            }

            stage.show();

        } catch (Exception e) {
            System.err.println("Error no controlado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}