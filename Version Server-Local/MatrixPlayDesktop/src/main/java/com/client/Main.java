package com.client;

import java.lang.reflect.Field;
import org.json.JSONObject;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
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
    }

    public static void pauseDuring(long milliseconds, Runnable action) {
        PauseTransition pause = new PauseTransition(Duration.millis(milliseconds));
        pause.setOnFinished(event -> Platform.runLater(action));
        pause.play();
    }

    public static void connectToServer() {
        Platform.runLater(() -> {
            logCtrl.setConnectingState();
        });

        resetWebSocket();

        pauseDuring(1000, () -> {
            try {
                String url = protocol + "://"+ logCtrl.getUrl() + ":" + port;
                wsClient = createNewWebSocketInstance(url);

                wsClient.onMessage((response) -> {
                    Platform.runLater(() -> {
                        wsMessage(response);
                    });
                });

                wsClient.onError((response) -> {
                    Platform.runLater(() -> {
                        System.out.println("Error de WebSocket: " + response);
                        setErrorState();
                        showAlert("Error de Conexión", "No se pudo conectar al servidor");
                        UtilsViews.setView("ViewLog");
                    });
                });

                wsClient.onOpen((response) -> {
                    Platform.runLater(() -> {
                        System.out.println("Conexión WebSocket abierta");
                        setConnectedState();
                        JSONObject userInfo = new JSONObject();
                        userInfo.put("type", "userInfo");
                        userInfo.put("userName", logCtrl.getUserName().trim());
                        wsClient.safeSend(userInfo.toString());
                    

                        JSONObject confirmation = new JSONObject();
                        confirmation.put("type", "clientConfirmation");
                        wsClient.safeSend(confirmation.toString());

                        UtilsViews.setViewAnimating("ViewCountD"); // viewWait
                        showAlert("Conexión Exitosa", "Conectado como: " + logCtrl.getUserName());
                    });
                });

                wsClient.onClose((response) -> {
                    Platform.runLater(() -> {
                        System.out.println("Conexión cerrada: " + response);
                        setErrorState();
                    });
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.out.println("Excepción: " + e.getMessage());
                    setErrorState();
                    showAlert("Error de Conexión", "Error: " + e.getMessage());
                    UtilsViews.setView("ViewLog");
                });
            }
        });
    }

    private static UtilsWS createNewWebSocketInstance(String url) {
        // nuevo intento de conex = nuevo ws
        try {
            Field field = UtilsWS.class.getDeclaredField("sharedInstance");
            field.setAccessible(true);
            field.set(null, null);
        } catch (Exception e) {
            System.out.println("No se pudo resetear singleton: " + e.getMessage());
        }
        return UtilsWS.getSharedInstance(url);
    }

    public static void resetWebSocket() {
        try {
            if (wsClient != null) {
                wsClient.forceExit();
                Thread.sleep(100);
                wsClient = null;
            }
        } catch (Exception e) {
            System.out.println("Error en reset: " + e.getMessage());
        }
    }

    private static void wsMessage(String response) {
        Platform.runLater(()->{
            JSONObject msgObj = new JSONObject(response);
            String type = msgObj.optString("type","");
            //System.out.println("RESPUESTAAA" + response);

            switch (type) {

                case "welcome" -> { 
                    String message = msgObj.optString("welcome" , "Hola"); System.out.println(message); 
                }
                
                case "playerAssigned" -> {
                    int playerId = msgObj.optInt("playerId", -1);
                    if (gameCtrl != null) {
                        gameCtrl.setPlayerId(playerId);
                    }
                    System.out.println("Jugador ID: " + playerId);
                }
                case "waiting" -> {
                    //String message = msgObj.getString("message");
                    //String origin = msgObj.getString("origin");
                    //String destination = msgObj.getString("destination");
                    break;
                }
                case "countdown" -> {
                    int countdownValue = msgObj.optInt("value", 0);
                    activeView = UtilsViews.getActiveView();

                    Platform.runLater(() -> {
                        if (activeView == null) activeView = "";
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

                case "gameState" -> {
                    if (gameCtrl != null) {
                        gameCtrl.updateGameState(msgObj);
                    }
                    break;
            }

            case "paddleMove" -> {
                // El servidor ya envía el gameState completo
                break;
            }

            }
          
        });          
    }

    private static void setConnectedState() {
        if (logCtrl != null) {
            logCtrl.setConnectedState();
        }
    }

    private static void setErrorState() {
        if (logCtrl != null) {
            logCtrl.setErrorState();
        }
    }

    public static void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.show();
        });
    }
}