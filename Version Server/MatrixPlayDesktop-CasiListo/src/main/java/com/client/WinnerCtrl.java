package com.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class WinnerCtrl {
    
    @FXML private Label winnerLabel;
    @FXML private Label winnerPoints;
    @FXML private Label loserLabel;
    @FXML private Label loserPoints;
    @FXML private Button againButton;
    @FXML private Button exitButton;

    @FXML
    public void initialize() {
        setupButtons();
    }

    private void setupButtons() {
        // Configurar botón AGAIN
        againButton.setOnAction(e -> handleAgain());
        
        // Configurar botón EXIT
        exitButton.setOnAction(e -> handleExit());
        
        // Estilos para los botones (opcional)
        againButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5;"
        );
        
        exitButton.setStyle(
            "-fx-background-color: #F44336; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5;"
        );
    }

    // Método para actualizar la información del ganador y perdedor
    public void setGameResult(String winnerName, int winnerScore, String loserName, int loserScore) {
        winnerLabel.setText(winnerName);
        winnerPoints.setText(String.valueOf(winnerScore));
        loserLabel.setText(loserName);
        loserPoints.setText(String.valueOf(loserScore));
    }

    private void handleAgain() {
        // Volver a la vista de conexión para jugar otra vez
        UtilsViews.setViewAnimating("ViewLog");
        
        // Limpiar estado anterior si es necesario
        if (Main.gameCtrl != null) {
            Main.gameCtrl.cleanup();
        }
        
        // Reiniciar conexión WebSocket
        Main.resetWebSocket();
    }

    private void handleExit() {
        // Cerrar la aplicación
        if (Main.wsClient != null) {
            Main.wsClient.forceExit();
        }
        System.exit(0);
    }
}