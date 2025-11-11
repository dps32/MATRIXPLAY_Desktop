package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class LogCtrl implements Initializable {

    @FXML
    private AnchorPane anchorPane; 
    
    @FXML private Canvas canvasLog;
    @FXML private Canvas canvasTitulo;
    @FXML private Canvas canvasLineaNaranja;

    // interactivos
    @FXML private TextField playerNameField;
    @FXML private TextField urlField;
    @FXML private Button connectButton;

    private GraphicsContext gcCanvasLog;
    private GraphicsContext gcCanvasTitulo;
    private GraphicsContext gcCanvasLineaNaranja;

    private Font pressStart2PTitulo;
    private Font pressStart2PTexto;
    public static String playerName;
    public static String url;

    public static UtilsWS wsClient;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // fuente
            pressStart2PTexto = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/PressStart2P-Regular.ttf"), 18);
            pressStart2PTitulo = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/PressStart2P-Regular.ttf"), 90);
            
            if (pressStart2PTexto == null || pressStart2PTitulo == null) {
                System.out.println("No se pudo cargar Press Start 2P, usando fallback");
                pressStart2PTexto = Font.font("Consolas", 18);
                pressStart2PTitulo = Font.font("Consolas", 30);
            }
            
            //g2d
            gcCanvasLog = canvasLog.getGraphicsContext2D();
            gcCanvasTitulo = canvasTitulo.getGraphicsContext2D();
            gcCanvasLineaNaranja = canvasLineaNaranja.getGraphicsContext2D();

            setupConnectButton();
            drawInterface();

            
        } catch (Exception e) {
            System.out.println("Error en initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void setupConnectButton() {
        if (connectButton == null) 
            return;

        // boton sin presionar
        connectButton.setStyle(
            "-fx-background-color: #f5c264ff; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Press Start 2P'; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
        );
        
        connectButton.setPrefWidth(200);
        connectButton.setPrefHeight(45);

        // hover
        connectButton.setOnMouseEntered(e -> {
            if (!connectButton.isDisabled()) {
                connectButton.setStyle(
                    "-fx-background-color: #ecaa2dff; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-family: 'Press Start 2P'; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 5; " +
                    "-fx-border-radius: 5; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.4), 8, 0, 0, 3);"
                );
            }
        });

        connectButton.setOnMouseExited(e -> {
            if (!connectButton.isDisabled()) {
                connectButton.setStyle(
                    "-fx-background-color: #f5c264ff;" +
                    "-fx-text-fill: white; " +
                    "-fx-font-family: 'Press Start 2P'; " +
                    "-fx-font-size: 14px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-background-radius: 5; " +
                    "-fx-border-radius: 5; " +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);"
                );
            }
        });

        // evento
        connectButton.setOnAction(e -> handleConnect());
    }

    private void handleConnect() {
        playerName = playerNameField.getText().trim();
        url = urlField.getText().trim();

        if (playerName.isEmpty() || url.isEmpty()) {
            showAlert("Error", "Por favor completa todos los campos");
            return;
        }

        setConnectingState();

        // conexion simulador
        new Thread(() -> {
            try {
                
                Thread.sleep(2000); // tiempo conex
                
                Platform.runLater(() -> {
                    setConnectedState();
                    Main.connectToServer();
                    showAlert("Conexión Exitosa", "Conectado como: " + playerName + "\n" + "Servidor: " + url);
                    UtilsViews.setViewAnimating("ViewWait");
                    
                
                });
                
            } catch (InterruptedException ex) {
                Platform.runLater(() -> {
                    setErrorState();
                    showAlert("Error de Conexión", "No se pudo conectar al servidor");
                });
            }
        }).start();
    }

    private void setConnectingState() {
        connectButton.setText("CONECTANDO...");
        connectButton.setDisable(true);
        connectButton.setStyle(
            "-fx-background-color: #FF9800; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Press Start 2P'; " +
            "-fx-font-size: 12px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5;"
        );
    }

    private void setConnectedState() {
        connectButton.setText("CONECTADO!");
        connectButton.setDisable(false);
        connectButton.setStyle(
            "-fx-background-color: #e6ce8fff; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Press Start 2P'; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5;"
        );
    }

    private void setErrorState() {
        connectButton.setText("CONNECT");
        connectButton.setDisable(false);
        connectButton.setStyle(
            "-fx-background-color: #F44336; " +
            "-fx-text-fill: white; " +
            "-fx-font-family: 'Press Start 2P'; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5; " +
            "-fx-border-radius: 5;"
        );
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void drawInterface() {
        // medidas ---------------------------------------------------------
        double widthLog = canvasLog.getWidth();
        double heightLog = canvasLog.getHeight();
        
        double widthTitle = canvasTitulo.getWidth();
        double heightTitle = canvasTitulo.getHeight();

        double widthLine = canvasLineaNaranja.getWidth();
        double heightLine = canvasLineaNaranja.getHeight();

        // Limpiar canvas 
        gcCanvasLog.clearRect(0, 0, widthLog, heightLog);
        gcCanvasTitulo.clearRect(0, 0, widthTitle, heightTitle);
        gcCanvasLineaNaranja.clearRect(0, 0, widthLine, heightLine);

        // Fondo negro
        gcCanvasLog.setFill(Color.BLACK);
        gcCanvasLog.fillRect(0, 0, widthLog, heightLog);

        gcCanvasTitulo.setFill(Color.BLACK);
        gcCanvasTitulo.fillRect(0, 0, widthTitle, heightTitle);

        gcCanvasLineaNaranja.setFill(Color.DARKORANGE);
        gcCanvasLineaNaranja.fillRect(0, 0, widthLine, heightLine);
        
        // tamaño letra 
        fontSize(gcCanvasTitulo,"Title");
        fontSize(gcCanvasLog,"Text");
    
        // posicion centrada
        double centerX = widthLog / 2;
        double startY = widthLog / 3;
        
        // TITULO 
        gcCanvasTitulo.fillText("P O N G ☆", (centerX / 2) - 170,  startY / 2);

        // PLAYER NAME + campo input
        gcCanvasLog.fillText("PLAYER NAME:", centerX - 420, startY - 250);
        gcCanvasLog.setStroke(Color.ORANGE);
        gcCanvasLog.setLineWidth(32);
        
        gcCanvasLog.strokeLine(centerX - 170, startY - 260, centerX + 220, startY - 260);
        
        // URL + campo input
        gcCanvasLog.fillText("URL:", centerX - 274, startY - 175);
        gcCanvasLog.strokeLine(centerX - 170, startY - 185, centerX + 220, startY - 185);

    }

    private void fontSize(GraphicsContext g2Context, String type){
        if(type.equals("Title")){
            g2Context.setFont(pressStart2PTitulo);
            g2Context.setFill(Color.DARKORANGE);
            g2Context.setStroke(Color.DARKORANGE);
            g2Context.setLineWidth(1);

        } else if (type.equals("Text")){
            g2Context.setFont(pressStart2PTexto);
            g2Context.setFill(Color.ORANGE);
            g2Context.setStroke(Color.ORANGE);
            g2Context.setLineWidth(1);
        }
    }

    // getters

    public String getUrl(){
        return url;
    }

    public String getUserName(){
        return playerName;
    }
}



// Revisar AlertDialogue para conexion (dice que ha conectado sin haber conectado >:( ))