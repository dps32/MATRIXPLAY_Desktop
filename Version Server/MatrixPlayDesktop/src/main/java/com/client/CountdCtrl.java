package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class CountdCtrl implements Initializable {

    @FXML private Label countdownNumber;
    @FXML private Label playerUno;
    @FXML Label playerDos;

    public String namePlayer1S, namePlayer2S;
    public static String nPWinLose1 , nPWinLose2;
    
    private boolean countdownInProgress = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cleanup();
        countdownNumber.setText("3");
        countdownInProgress = false;
    }

     // wait jugadores

    public void receiveNamesPlayers(String namePlayer1, String namePlayer2) {
        Platform.runLater(() -> {
            this.namePlayer1S = namePlayer1;
            this.namePlayer2S = namePlayer2;
            playerUno.setText(namePlayer1S != null && !namePlayer1.isEmpty() ? namePlayer1 : "?");
            playerDos.setText(namePlayer2S != null && !namePlayer2.isEmpty() ? namePlayer2 : "?");

            nPWinLose1 = namePlayer1S;
            nPWinLose2 = namePlayer2S;

        });
    }

    public void clearNames() {
        namePlayer1S = "?";
        namePlayer2S = "?";

        playerUno.setText(namePlayer1S);
        playerDos.setText(namePlayer2S);
    }

    public String getPlayer1Name() {
        return playerUno.getText();
    }

    public String getPlayer2Name() {
        return playerDos.getText();
    }



    // countdown
    public void setCountdownValue(String value) {
        if (countdownNumber != null) {
            countdownNumber.setText(value);
        }
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
            setCountdownValue("VS");
            
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // 1 seg para vs
                    Platform.runLater(() -> {
                        UtilsViews.setViewAnimating("ViewGame");
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
        cleanup();
    }
    
    public void cleanup() {
        countdownInProgress = false;
        namePlayer1S = null;
        namePlayer2S = null;
        
        if (countdownNumber != null) {
            countdownNumber.setText("3");
        }
        if (playerUno != null) {
            playerUno.setText("?");
        }
        if (playerDos != null) {
            playerDos.setText("?");
        }
    }

   

}