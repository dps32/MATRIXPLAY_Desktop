package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class LogCtrl implements Initializable {

    @FXML
    private AnchorPane anchorPane; 
    
    @FXML private Canvas canvasLog;
    @FXML private Canvas canvasTitulo;
    @FXML private Canvas canvasLineaNaranja;


    private GraphicsContext gcCanvasLog;
    private GraphicsContext gcCanvasTitulo;
    private GraphicsContext gcCanvasLineaNaranja;
    

    private Font pressStart2PTitulo;
    private Font pressStart2PTexto;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // fuente
            pressStart2PTexto = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/PressStart2P-Regular.ttf"), 18);
            pressStart2PTitulo = Font.loadFont(getClass().getResourceAsStream("/assets/fonts/PressStart2P-Regular.ttf"), 30);
            
            // Si la fuente no se carga, usar fallback
            if (pressStart2PTexto == null || pressStart2PTitulo == null) {
                System.out.println("No se pudo cargar Press Start 2P, usando fallback");
                pressStart2PTexto = Font.font("Consolas", 18);
                pressStart2PTitulo = Font.font("Consolas", 30);
            }
            
            //g2d
            gcCanvasLog = canvasLog.getGraphicsContext2D();
            gcCanvasTitulo = canvasTitulo.getGraphicsContext2D();
            gcCanvasLineaNaranja = canvasLineaNaranja.getGraphicsContext2D();

            
            // Dibujar después de que JavaFX haya renderizado
            Platform.runLater(() -> {
                drawInterface();
            });
            
        } catch (Exception e) {
            System.out.println("Error en initialize: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void drawInterface() {

        // medidas
        double widthLog = canvasLog.getWidth();
        double heightLog = canvasLog.getHeight();
        
        double widthTitle = canvasTitulo.getWidth();
        double heightTitle = canvasTitulo.getHeight();

        double widthLine = canvasLineaNaranja.getWidth();
        double heightLine = canvasLineaNaranja.getHeight();

        // Limpiar canvas
        gcCanvasLog.clearRect(0, 0, widthLog, heightLog);
        gcCanvasTitulo.clearRect(0, 0, widthTitle, heightTitle);
        gcCanvasLineaNaranja.clearRect(0, 0, widthLine, heightLine);

        // Fondo negro
        gcCanvasLog.setFill(Color.BLACK);
        gcCanvasLog.fillRect(0, 0, widthLog, heightLog);

        gcCanvasTitulo.setFill(Color.BLACK);
        gcCanvasTitulo.fillRect(0, 0, widthTitle, heightTitle);

        gcCanvasLineaNaranja.setFill(Color.BLACK);
        gcCanvasLineaNaranja.fillRect(0, 0, widthLine, heightLine);
        

        // Configurar la fuente Press Start 2P TITULO
        gcCanvasLog.setFont(pressStart2PTitulo);
        gcCanvasLog.setFill(Color.ORANGE);
        gcCanvasLog.setStroke(Color.ORANGE);
        gcCanvasLog.setLineWidth(1);
        
        // Configurar la fuente Press Start 2P TEXTO
        gcCanvasLog.setFont(pressStart2PTexto);
        gcCanvasLog.setFill(Color.ORANGE);
        gcCanvasLog.setStroke(Color.ORANGE);
        gcCanvasLog.setLineWidth(1);


        
        // Calcular posición centrada
        double centerX = widthLog / 2;
        double startY = widthLog / 3;
        
        // PLAYER NAME:
        gcCanvasLog.fillText("PLAYER NAME:", centerX - 200, startY);
        
        // Línea para el campo de texto (más larga como en la imagen)
        gcCanvasLog.setStroke(Color.ORANGE);
        gcCanvasLog.setLineWidth(2);
        gcCanvasLog.strokeLine(centerX - 200, startY + 30, centerX + 200, startY + 30);
        
        // URL:
        gcCanvasLog.fillText("URL:", centerX - 200, startY + 100);
        
        // Línea para URL (más larga)
        gcCanvasLog.strokeLine(centerX - 200, startY + 130, centerX + 200, startY + 130);
        
        // Botón CONNECT - estilo arcade
        double buttonX = centerX - 75;
        double buttonY = startY + 180;
        double buttonWidth = 150;
        double buttonHeight = 40;
        
        // Fondo del botón
        gcCanvasLog.setFill(Color.BLACK);
        gcCanvasLog.fillRect(buttonX, buttonY, buttonWidth, buttonHeight);
        
        // Borde del botón
        gcCanvasLog.setStroke(Color.GREEN);
        gcCanvasLog.setLineWidth(3);
        gcCanvasLog.strokeRect(buttonX, buttonY, buttonWidth, buttonHeight);
        
        // Texto del botón centrado
        gcCanvasLog.setFill(Color.GREEN);
        gcCanvasLog.fillText("CONNECT", centerX - 70, buttonY + 25);
    }
}