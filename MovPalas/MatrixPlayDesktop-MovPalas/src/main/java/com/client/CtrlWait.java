
package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;


public class CtrlWait implements Initializable{
    
    @FXML private Label player1;
    @FXML private Label player2;

    private String namePlayer1, namePlayer2;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        namePlayer1 = "?";
        namePlayer2 = "?";

        player1.setText(namePlayer1);
        player2.setText(namePlayer2);
    }

    public void receiveNamesPlayers(String namePlayer1, String namePlayer2) {
        this.namePlayer1 = namePlayer1;
        this.namePlayer2 = namePlayer2;
    }

    public void entersFirstPlayer() {
        player1.setText(namePlayer1);
    }

    public void entersSecondPlayer() {
        player2.setText(namePlayer2);
    }

    public void clearNames() {
        namePlayer1 = "?";
        namePlayer2 = "?";

        player1.setText(namePlayer1);
        player2.setText(namePlayer2);
    }

    // Getters de los nombres
    public String getPlayer1Name() {
        return player1.getText();
    }

    public String getPlayer2Name() {
        return player2.getText();
    }
}
