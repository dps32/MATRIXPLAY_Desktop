package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class GameCtrl implements Initializable {

    @FXML Label labelGame;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        labelGame.setText("GAME START :)");
    }
    
}
