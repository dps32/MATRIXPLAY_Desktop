package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;

public class CtrlWait implements Initializable {

    @FXML private Label waitLabel;
    @FXML private ImageView gifAdd; 

    private Font pressStart2PTitulo;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Cargar fuente
            pressStart2PTitulo = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/PressStart2P-Regular.ttf"), 50);
            
            if (pressStart2PTitulo != null) {
                waitLabel.setFont(pressStart2PTitulo);
            }

            // Cargar GIF
            loadGif();
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage()); 
            e.printStackTrace();
        }
    }

    private void loadGif() {
        try {
            // Cargar el GIF desde resources
            Image gifImage = new Image(getClass().getResourceAsStream("/png/arcadeGif.gif"));
            gifAdd.setImage(gifImage);
            
            // Ajustar tamaño y posición
            gifAdd.setFitWidth(400);
            gifAdd.setFitHeight(300);
            gifAdd.setPreserveRatio(true);
            
        } catch (Exception e) {
            System.err.println("Error cargando el GIF: " + e.getMessage());
            e.printStackTrace();
        }
    }
}