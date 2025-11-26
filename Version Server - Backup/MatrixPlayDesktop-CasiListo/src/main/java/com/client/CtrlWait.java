
package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;


public class CtrlWait implements Initializable{

    @FXML private Label waitLabel, stars;
    @FXML private ImageView lineaArriba, lineaAbajo; 

    private Font pressStart2PTitulo;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // texto
            pressStart2PTitulo = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/PressStart2P-Regular.ttf"), 50);
            
            if (pressStart2PTitulo != null) {
                waitLabel.setFont(pressStart2PTitulo);
                stars.setFont(pressStart2PTitulo);
            }

            //img
             try {
                Image image = new Image(getClass().getResourceAsStream("/png/lineWait.png"));
                lineaArriba.setImage(image);
                lineaAbajo.setImage(image);
                
            } catch (Exception e) {
                System.err.println("Error loading image: " + e.getMessage());
            }


        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage()); 
        }
    }

}