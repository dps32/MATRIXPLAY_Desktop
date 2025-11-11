package com.client;

import org.json.JSONObject;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Main extends Application {

    public static LogCtrl logCtrl;
    public static CtrlWait waitCtrl;
    public static UtilsWS wsClient;

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
            UtilsViews.addView(getClass(), "ViewWait", "/assets/waitView.fxml");

            logCtrl = (LogCtrl) UtilsViews.getController("ViewLog");
            waitCtrl = (CtrlWait) UtilsViews.getController("ViewWait");

            
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

      @Override
        public void stop() { 
            if (wsClient != null) {
                wsClient.forceExit();
            }
            System.exit(1); // kill executors
        }
        
        public static void pauseDuring(long milliseconds, Runnable action) {
            PauseTransition pause = new PauseTransition(Duration.millis(milliseconds));
            pause.setOnFinished(event -> Platform.runLater(action));
            pause.play();
        }

        public static void connectToServer(){
            pauseDuring(1500, () -> {
            wsClient = UtilsWS.getSharedInstance(logCtrl.getUrl());

            wsClient.onMessage((response) -> { 
                Platform.runLater(() -> { 
                    wsMessage(response); 
                }); 
            });
            
            // Enviar información del usuario después de conectar
            pauseDuring(2000, () -> {
                if (wsClient != null && wsClient.isOpen()) {
                    JSONObject userInfo = new JSONObject();
                    userInfo.put("type", "userInfo");
                    userInfo.put("userName", logCtrl.getUserName().trim()); // Usar el nombre ingresado
                    wsClient.safeSend(userInfo.toString());
                    System.out.println("Enviando nombre de usuario: " + logCtrl.getUserName().trim());
                } 
            });
        });
    }    

    private static void wsMessage(String response) {
        // blablalbalbal

        // URL: wss://matrixplay1.ieti.site:443
    }
      
}