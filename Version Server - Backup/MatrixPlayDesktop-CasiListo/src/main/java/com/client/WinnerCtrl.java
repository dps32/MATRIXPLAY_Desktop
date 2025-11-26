package com.client;

import org.json.JSONObject;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

public class WinnerCtrl {
    
    @FXML private Label winnerLabel, winnerPoints, loserLabel, loserPoints;
    @FXML private Button againButton, exitButton;

    @FXML private ImageView imgOrg;

    private Font pressStart2PTitulo;

    @FXML
    public void initialize() {
        setupButtons();
        try {
            //txt
            pressStart2PTitulo = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/PressStart2P-Regular.ttf"), 40);

            if (pressStart2PTitulo != null) {
                winnerLabel.setFont(pressStart2PTitulo);
                winnerPoints.setFont(pressStart2PTitulo);
                loserLabel.setFont(pressStart2PTitulo);
                loserPoints.setFont(pressStart2PTitulo);
            }
            
            
            //img
            try {
                Image image = new Image(getClass().getResourceAsStream("/png/winnerView.png"));
                imgOrg.setImage(image);
                
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        

    }

    private void setupButtons() {
        againButton.setOnAction(e -> handleAgain());
        exitButton.setOnAction(e -> handleExit());
        
        againButton.setStyle(
            "-fx-background-color: #d46735ff; " +
            "-fx-text-fill: #e7a33dff; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5;"
        );
        
        exitButton.setStyle(
            "-fx-background-color: #d46735ff; " +
            "-fx-text-fill: #e7a33dff; " +
            "-fx-font-weight: bold; " +
            "-fx-background-radius: 5;"
        );
    }

    public void setGameResult(String winnerName, int winnerScore, String loserName, int loserScore) {
        winnerLabel.setText(winnerName);
        winnerPoints.setText(String.valueOf(winnerScore));
        loserLabel.setText(loserName);
        loserPoints.setText(String.valueOf(loserScore));
    }

    private void handleAgain() {
        System.out.println("Solicitando nueva partida...");
        
        // NO desconectar completamente mantener la conexión WebSocket, solo limpiar el estado del juego actual
        
        if (Main.gameCtrl != null) {
            Main.gameCtrl.cleanup();
        }
        
        Main.idPlayerDesktop = 0;
        
        // server jugar again?
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
        
        //revisar esto a ver si se puede agregar un delay antes que pase a la viewWait
        UtilsViews.setView("ViewWait"); //ViewWin
    }

    private void handleExit() {
        if (Main.wsClient != null) {
            Main.wsClient.forceExit();
        }
        System.exit(0);
    }
}