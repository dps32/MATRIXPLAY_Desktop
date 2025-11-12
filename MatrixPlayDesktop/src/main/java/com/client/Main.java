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
    

    public static String protocol = "wss";
    public static String port = "443";

    public static String activeView;

    public static LogCtrl logCtrl;
    public static CtrlWait waitCtrl;
    public static CountdCtrl ctrlCount;
    public static GameCtrl gameCtrl;



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
            UtilsViews.addView(getClass(), "ViewCountD", "/assets/countdownView.fxml");
            UtilsViews.addView(getClass(), "ViewGame", "/assets/gameView.fxml");


            logCtrl = (LogCtrl) UtilsViews.getController("ViewLog");
            waitCtrl = (CtrlWait) UtilsViews.getController("ViewWait");
            ctrlCount = (CountdCtrl) UtilsViews.getController("ViewCountD");
            gameCtrl = (GameCtrl) UtilsViews.getController("ViewGame");

            
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
            wsClient = UtilsWS.getSharedInstance(protocol + "://"+ logCtrl.getUrl() + ":" + port)  ; // url para conectar 

            wsClient.onMessage((response) -> { 
                Platform.runLater(() -> { 
                    wsMessage(response); 
                }); 
            });
            
            // info usuario
            pauseDuring(2000, () -> {
                if (wsClient != null && wsClient.isOpen()) {
                    JSONObject userInfo = new JSONObject();
                    userInfo.put("type", "userInfo");
                    userInfo.put("userName", logCtrl.getUserName().trim()); // nombre registrado 
                    wsClient.safeSend(userInfo.toString());
                    System.out.println("Enviando nombre de usuario: " + logCtrl.getUserName().trim());
                } 
            });
        });
    }    

    private static void wsMessage(String response) {
        Platform.runLater(()->{ 
            JSONObject msgObj = new JSONObject(response);
            String type = msgObj.optString("type","");

            switch (type) {
                case "waiting" -> {
                    //String message = msgObj.getString("message");
                    //String origin = msgObj.getString("origin");
                    //String destination = msgObj.getString("destination");

                    //ctrlSockets.invitationMessages(origin, destination, message); 
                    break;
                }
            
                case "countdown" -> {
                    int countdownValue = msgObj.optInt("value", 0);
                    activeView = UtilsViews.getActiveView();

                    Platform.runLater(() -> {
                        if (activeView == null) 
                            activeView = "";

                        if (!"ViewCountD".equals(activeView)) {
                            UtilsViews.setView("ViewCountD");
                        }

                        String txt = (countdownValue == 0) ? "GO!" : String.valueOf(countdownValue);
                        ctrlCount.setCountdownValue(txt);

                        if (countdownValue == 0) {
                            new Thread(() -> {
                                try {
                                    Thread.sleep(1000);
                                    Platform.runLater(() -> {
                                        UtilsViews.setViewAnimating("ViewGame");
                                        Stage stage = UtilsViews.getStage();
                                        stage.setWidth(1400);
                                        stage.setHeight(900);
                                        stage.centerOnScreen();
                                    });
                                } catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                }
                            }).start();
                        }
                    });
                    break;
                    }


            }
            
        });
    }
      
}