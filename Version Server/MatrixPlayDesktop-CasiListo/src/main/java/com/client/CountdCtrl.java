package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CountdCtrl implements Initializable {

    @FXML public Label countdownNumber;
    @FXML public Label playerUno;
    @FXML public Label playerDos;
    
    private boolean countdownInProgress = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        countdownNumber.setText("3");
        countdownInProgress = false;
        
        playerUno.setText("...");
        playerDos.setText("...");
    }

    public void setCountdownValue(String value) {
        if (countdownNumber != null) {
            countdownNumber.setText(value);
        }
    }
    
    public void setPlayerNames(String player1Name, String player2Name) {
        Platform.runLater(() -> {
            if (playerUno != null) {
                playerUno.setText(player1Name);
            }
            if (playerDos != null) {
                playerDos.setText(player2Name);
            }
        });
    }
    
    public void startCountdown() {
        if (countdownInProgress) return;
        
        countdownInProgress = true;
        startCountdownAnimation(3);
    }
    
    private void startCountdownAnimation(int count) {
        if (count > 0) {
            setCountdownValue(String.valueOf(count));
            
            new Thread(() -> {
                try {
                    Thread.sleep(1000);
                    Platform.runLater(() -> {
                        startCountdownAnimation(count - 1);
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    countdownInProgress = false;
                }
            }).start();
        } else {
            setCountdownValue("GO!");
            
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // 1 seg para go
                    Platform.runLater(() -> {
                        // Cambiar directamente a ViewGame sin verificación
                        UtilsViews.setView("ViewGame");
                        Stage stage = UtilsViews.getStage();
                        if (stage != null) {
                            stage.setWidth(1200);
                            stage.setHeight(700);
                            stage.centerOnScreen();
                        }
                        
                        if (Main.gameCtrl != null) {
                            Main.gameCtrl.startGame();
                        }
                        
                        countdownInProgress = false;
                    });
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    countdownInProgress = false;
                }
            }).start();
        }
    }
    
    public void resetCountdown() {
        countdownInProgress = false;
        countdownNumber.setText("3");
    }
}