
package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;


public class CtrlWait implements Initializable{
    
    @FXML private Label player1;
    @FXML private Label player2;

    private String namePlayer1S, namePlayer2S;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        namePlayer1S = "?";
        namePlayer2S = "?";

        player1.setText(namePlayer1S);
        player2.setText(namePlayer2S);
    }

    public void receiveNamesPlayers(String namePlayer1, String namePlayer2) {
        Platform.runLater(() -> {
            this.namePlayer1S = namePlayer1;
            this.namePlayer2S = namePlayer2;
            player1.setText(namePlayer1S != null && !namePlayer1.isEmpty() ? namePlayer1 : "?");
            player2.setText(namePlayer2S != null && !namePlayer2.isEmpty() ? namePlayer2 : "?");
        });
    }

    // public void entersFirstPlayer() {
    //     player1.setText(namePlayer1S);
    // }

    // public void entersSecondPlayer() {
    //     player2.setText(namePlayer2S);
    // }

    public void clearNames() {
        namePlayer1S = "?";
        namePlayer2S = "?";

        player1.setText(namePlayer1S);
        player2.setText(namePlayer2S);
    }

    // Getters de los nombres
    public String getPlayer1Name() {
        return player1.getText();
    }

    public String getPlayer2Name() {
        return player2.getText();
    }
}
