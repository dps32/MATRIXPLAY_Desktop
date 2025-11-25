package com.client;

import org.json.JSONObject;

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
        System.out.println("Solicitando nueva partida...");
        
        // NO desconectar completamente - mantener la conexión WebSocket
        // Solo limpiar el estado del juego actual
        
        if (Main.gameCtrl != null) {
            Main.gameCtrl.cleanup();
        }
        
        // Resetear variables de estado del juego
        Main.idPlayerDesktop = 0;
        
        // msg jugar otra vez
        if (Main.wsClient != null && Main.wsClient.isOpen()) {
            try {
                JSONObject newGameMsg = new JSONObject();
                newGameMsg.put("type", "clientConfirmation");
                newGameMsg.put("name", Main.namePlayerDesktop);
                Main.wsClient.safeSend(newGameMsg.toString());
                System.out.println("Enviada confirmación para nueva partida");
            } catch (Exception e) {
                System.err.println("Error enviando confirmación: " + e.getMessage());
            }
        }
        
        UtilsViews.setView("ViewWait");
    }

    private void handleExit() {
        if (Main.wsClient != null) {
            Main.wsClient.forceExit();
        }
        System.exit(0);
    }
}