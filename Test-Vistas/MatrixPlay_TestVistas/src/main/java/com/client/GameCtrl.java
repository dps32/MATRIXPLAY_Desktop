package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

public class GameCtrl implements Initializable {

    @FXML 
    private Canvas canvaPartida;
    
    @FXML
    private AnchorPane rootPane;

    private GraphicsContext gcGame;
    
    // dimensiones de las palas
    private final double paddleWidth = 20;
    private final double paddleHeight = 150;
    private final double paddleMargin = 15;

    // posiciones de las palas (0-1 como viene del servidor)
    private double leftPaddlePosition = 0.5;
    private double rightPaddlePosition = 0.5;

     // dimensiones de la pelota
    private final double ballRadius = 15;

    // posicion de la pelota 0-1 como viene del servidor
    private double ballX = 0.5;
    private double ballY = 0.5;


    // estado del juego - mov teclas
    private boolean isGameRunning = false;
    private AnimationTimer gameLoop;

    private boolean wPressed = false;
    private boolean sPressed = false;


    // teclas moviendose bool

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        gcGame = canvaPartida.getGraphicsContext2D();

        drawGame();

        
    }




    // server y mov
    private void sendPaddleMove(double position) { // envia posiciones al server 
        if (Main.wsClient != null) {
            JSONObject moveMsg = new JSONObject();
            moveMsg.put("type", "paddleMove");
            moveMsg.put("y", position);
            Main.wsClient.safeSend(moveMsg.toString());
        }
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                drawGame();
                //handleLocalInput();
            }
        };
        gameLoop.start();
    }



    

    // canvas tablero 
    private void drawGame(){
    
        gcGame.clearRect(0, 0, canvaPartida.getWidth(), canvaPartida.getHeight()); // limpia tablero

        // fondo 
        gcGame.setFill(Color.BLACK);
        gcGame.fillRect(0, 0, canvaPartida.getWidth(), canvaPartida.getHeight());

        if (canvaPartida.getWidth() == 0 || canvaPartida.getHeight() == 0) {
            return;
        }

        drawCenterLine();

        // palas 
        drawLeftPaddle();
        drawRightPaddle();

        drawBall();

    }


    private void drawLeftPaddle() {
        double paddleY = leftPaddlePosition * canvaPartida.getHeight();
        double limitTop = paddleY - (paddleHeight / 2);
        
        limitTop = Math.max(paddleMargin, Math.min(limitTop, canvaPartida.getHeight() - paddleHeight - paddleMargin));
        
        gcGame.setFill(Color.WHITE);
        gcGame.fillRect(30, limitTop, paddleWidth, paddleHeight);
    }

    private void drawRightPaddle() {
        double paddleY = rightPaddlePosition * canvaPartida.getHeight();
        double limitTop = paddleY - (paddleHeight / 2);
    
        limitTop = Math.max(paddleMargin, Math.min(limitTop, canvaPartida.getHeight() - paddleHeight - paddleMargin));
        
        gcGame.setFill(Color.WHITE);
        gcGame.fillRect(canvaPartida.getWidth() - 30 - paddleWidth, limitTop, paddleWidth, paddleHeight);
    }

    private void drawBall() {
        double pixelX = ballX * canvaPartida.getWidth();
        double pixelY = ballY * canvaPartida.getHeight();
        
        gcGame.setFill(Color.WHITE);
        gcGame.fillOval(pixelX - ballRadius, pixelY - ballRadius, ballRadius * 2, ballRadius * 2);
    }

    private void drawCenterLine() {
        double centerX = canvaPartida.getWidth() / 2;
        double dashHeight = 20;
        double dashGap = 20;
        double y = 0;
        
        gcGame.setStroke(Color.GRAY);
        gcGame.setLineWidth(2);
        
        while (y < canvaPartida.getHeight()) {
            gcGame.strokeLine(centerX, y, centerX, y + dashHeight);
            y += dashHeight + dashGap;
        }
    }

    // teclas boolean
    public void handleKeyPressed(KeyEvent event) {
            if (event.getCode() == KeyCode.W) {
                wPressed = true;
            } else if (event.getCode() == KeyCode.S) {
                sPressed = true;
            }
        }
    
    public void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.W) {
            wPressed = false;
        } else if (event.getCode() == KeyCode.S) {
            sPressed = false;
        }
    }

     // Método para limpiar recursos
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }



}
