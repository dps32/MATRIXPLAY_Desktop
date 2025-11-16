
package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class CountdCtrl implements Initializable{

    @FXML public Label countdownNumber;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        countdownNumber.setText("3"); // ARREGLAR COUNTDOWN 
    }

    public void setCountdownValue(String value) {
        countdownNumber.setText(value);
    }

}
