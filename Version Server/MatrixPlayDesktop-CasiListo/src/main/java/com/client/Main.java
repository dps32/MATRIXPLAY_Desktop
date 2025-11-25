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

    public static String namePlayerMobile;
    public static String namePlayerDesktop;
    public static int idPlayerDesktop;

    public static LogCtrl logCtrl;
    public static CtrlWait waitCtrl;
    public static CountdCtrl ctrlCount;
    public static GameCtrl gameCtrl;
    public static WinnerCtrl winCtrl;
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
            UtilsViews.addView(getClass(), "ViewWin", "/assets/winnerView.fxml");

            logCtrl = (LogCtrl) UtilsViews.getController("ViewLog");
            waitCtrl = (CtrlWait) UtilsViews.getController("ViewWait");
            ctrlCount = (CountdCtrl) UtilsViews.getController("ViewCountD");
            gameCtrl = (GameCtrl) UtilsViews.getController("ViewGame");
            winCtrl = (WinnerCtrl) UtilsViews.getController("ViewWin");
            

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

                wsClient.onOpen((response) -> { // envia data a server 
                    Platform.runLater(() -> {
                        System.out.println("Conexión WebSocket abierta");
                        setConnectedState();
                        JSONObject userInfo = new JSONObject();
                        userInfo.put("type", "userInfo");
                        userInfo.put("userName", logCtrl.getUserName().trim());
                        wsClient.safeSend(userInfo.toString());
                    

                        JSONObject confirmation = new JSONObject();
                        confirmation.put("type", "clientConfirmation");
                        confirmation.put("name", namePlayerDesktop);
                        wsClient.safeSend(confirmation.toString());

                        UtilsViews.setViewAnimating("ViewWait"); // viewWait
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

    private static void wsMessage(String response) { // respuesta server
        Platform.runLater(()->{
            JSONObject msgObj = new JSONObject(response);
            String type = msgObj.optString("type","");

            //System.out.println("TYPEEEE" + type);
            //System.out.println("RESPUESTAAA" + response);

            String currentView = UtilsViews.getActiveView();
            if ("ViewWin".equals(currentView)) {
                if ("gameState".equals(type) || "countdown".equals(type) || "playerNames".equals(type)) {
                    System.out.println("Ignorando mensaje tipo: " + type + " porque estamos en ViewWin");
                    return; // ignorar estos mensajes
                }
            }

            switch (type) {

                case "welcome" -> { 
                    String message = msgObj.optString("message" , ""); 
                    System.out.println("Welcome: " + message); 
                }
                
                case "playerAssigned" -> {
                    int playerId = msgObj.optInt("playerId", -1);
                    if (gameCtrl != null) {
                        gameCtrl.setPlayerId(playerId);
                    }
                    //System.out.println("Jugador ID: " + playerId);
                    idPlayerDesktop = playerId;

                    if ("ViewWin".equals(currentView)) {
                        System.out.println("Reconexión después de winner - yendo a ViewWait");
                        UtilsViews.setView("ViewWait");
                    }
                }

                case "playerNames" -> {
                    String playerUno = msgObj.optString("player1","");
                    String playerDos = msgObj.optString("player2","");
                    
                    System.out.println("JUGADORES EN PARTIDA: " + playerUno + " " + playerDos);
                    System.out.println("idPlayerDesktop: " + idPlayerDesktop);
                    
                    // Asignar nombres correctamente según el playerId
                    if (idPlayerDesktop == 1) {
                        namePlayerDesktop = playerUno;
                        namePlayerMobile = playerDos;
                    } else if (idPlayerDesktop == 2) {
                        namePlayerDesktop = playerDos;
                        namePlayerMobile = playerUno;
                    }
                    
                    System.out.println("Asignados - Desktop: " + namePlayerDesktop + ", Mobile: " + namePlayerMobile);
                    
                    Platform.runLater(() -> {
                        if (ctrlCount != null) {
                            ctrlCount.setPlayerNames(playerUno, playerDos, idPlayerDesktop);
                            
                            // Si ya estamos en ViewCountD pero el countdown no ha empezado, iniciarlo
                            if ("ViewCountD".equals(UtilsViews.getActiveView())) {
                                System.out.println("Nombres asignados en countdown, iniciando countdown...");
                                ctrlCount.startCountdown();
                            }
                        }
                    });
                    break;
                }

                case "countdown" -> {
                    int countdownValue = msgObj.optInt("number", 0);
                    activeView = UtilsViews.getActiveView();
                    
                    Platform.runLater(() -> {
                        if (!"ViewCountD".equals(activeView)) {
                            UtilsViews.setView("ViewCountD"); 
                        }

                        if (ctrlCount != null) {
                            // Pequeña pausa para asegurar que los nombres se hayan asignado
                            PauseTransition pause = new PauseTransition(Duration.millis(100));
                            pause.setOnFinished(e -> {
                                if (countdownValue == 3) {
                                    ctrlCount.startCountdown();
                                }
                            });
                            pause.play();
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