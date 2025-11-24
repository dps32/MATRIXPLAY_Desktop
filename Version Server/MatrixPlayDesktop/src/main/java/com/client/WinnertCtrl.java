package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class WinnertCtrl implements Initializable {

    @FXML public Label winnerLabel, winnerPoints, loserLabel, loserPoints; // Quitar static
    @FXML public Button againButton, exitButton;

    public String p1WinLose, p2WinLose; // Quitar static

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        p1WinLose = CountdCtrl.nPWinLose1;
        p2WinLose = CountdCtrl.nPWinLose2;
        setWinner(); // Llamar aquí después de inicializar
    }
    
    public void setWinner() { // Quitar static
        if (GameCtrl.player1Score == 10){
            winnerLabel.setText(p1WinLose);
            loserLabel.setText(p2WinLose);
            winnerPoints.setText(String.valueOf(GameCtrl.player1Score));
            loserPoints.setText(String.valueOf(GameCtrl.player2Score));
        } else if (GameCtrl.player2Score == 10){
            winnerLabel.setText(p2WinLose);
            loserLabel.setText(p1WinLose);
            winnerPoints.setText(String.valueOf(GameCtrl.player2Score));
            loserPoints.setText(String.valueOf(GameCtrl.player1Score));
        }
    }
}
