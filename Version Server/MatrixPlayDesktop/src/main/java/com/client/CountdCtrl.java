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
    
    private boolean countdownInProgress = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        countdownNumber.setText("3");
        countdownInProgress = false;
    }

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
            
            // 1 seg de espera
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
                    Thread.sleep(1000); // go por 1 segundo
                    Platform.runLater(() -> {
                        UtilsViews.setViewAnimating("ViewGame");
                        Stage stage = UtilsViews.getStage();
                        if (stage != null) {
                            stage.setWidth(1400); //verif tamaño
                            stage.setHeight(900);
                            stage.centerOnScreen();
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