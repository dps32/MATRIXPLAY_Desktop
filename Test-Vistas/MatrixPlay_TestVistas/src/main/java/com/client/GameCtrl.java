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
    
    private final double paddleWidth = 20;
    private final double paddleHeight = 150;
    private final double paddleMargin = 15;

    private double leftPaddlePosition = 0.5;
    private double rightPaddlePosition = 0.5;

    private final double ballRadius = 15;
    private double ballX = 0.5;
    private double ballY = 0.5;

    private AnimationTimer gameLoop;

    private boolean wPressed = false;
    private boolean sPressed = false;
    private boolean upPressed = false;
    private boolean downPressed = false;

    // REDUCIDO: Velocidad más lenta para mejor control
    private final double paddleSpeed = 0.005;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gcGame = canvaPartida.getGraphicsContext2D();
        
        // Configurar el foco para que capture eventos de teclado
        setupFocus();
        
        drawGame();
        setupGameLoop();
        
        System.out.println("GameCtrl inicializado - Listo para recibir teclas");
    }

    private void setupFocus() {
        // Hacer que el AnchorPane pueda recibir foco
        rootPane.setFocusTraversable(true);
        
        // Solicitar foco inicial para el AnchorPane
        rootPane.requestFocus();
        
        // Cuando se haga clic en cualquier lugar, solicitar foco
        rootPane.setOnMouseClicked(e -> {
            rootPane.requestFocus();
            System.out.println("Foco solicitado para RootPane");
        });
        
        canvaPartida.setOnMouseClicked(e -> {
            rootPane.requestFocus();
            System.out.println("Foco solicitado para RootPane (desde Canvas)");
        });
    }

    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                handleLocalInput();
                drawGame();
            }
        };
        gameLoop.start();
    }

    private void handleLocalInput() {
        // Mover paleta izquierda con W y S
        if (wPressed && !sPressed) {
            leftPaddlePosition = Math.max(0, leftPaddlePosition - paddleSpeed);
            System.out.println("Moviendo izquierda ARRIBA: " + leftPaddlePosition);
        }
        if (sPressed && !wPressed) {
            leftPaddlePosition = Math.min(1, leftPaddlePosition + paddleSpeed);
            System.out.println("Moviendo izquierda ABAJO: " + leftPaddlePosition);
        }

        // Mover paleta derecha con FLECHAS ARRIBA y ABAJO
        if (upPressed && !downPressed) {
            rightPaddlePosition = Math.max(0, rightPaddlePosition - paddleSpeed);
            System.out.println("Moviendo derecha ARRIBA: " + rightPaddlePosition);
        }
        if (downPressed && !upPressed) {
            rightPaddlePosition = Math.min(1, rightPaddlePosition + paddleSpeed);
            System.out.println("Moviendo derecha ABAJO: " + rightPaddlePosition);
        }
    }

    private void sendPaddleMove(double position) {
        if (Main.wsClient != null) {
            JSONObject moveMsg = new JSONObject();
            moveMsg.put("type", "paddleMove");
            moveMsg.put("y", position);
            Main.wsClient.safeSend(moveMsg.toString());
        }
    }

    private void drawGame() {
        gcGame.clearRect(0, 0, canvaPartida.getWidth(), canvaPartida.getHeight());

        gcGame.setFill(Color.BLACK);
        gcGame.fillRect(0, 0, canvaPartida.getWidth(), canvaPartida.getHeight());

        if (canvaPartida.getWidth() == 0 || canvaPartida.getHeight() == 0) {
            return;
        }

        drawCenterLine();
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

    // Estos métodos son llamados automáticamente por FXML
    public void handleKeyPressed(KeyEvent event) {
        
        switch (event.getCode()) {
            case W:
                wPressed = true;
                break;
            case S:
                sPressed = true;
                break;
            case UP:
                upPressed = true;
                break;
            case DOWN:
                downPressed = true;
                break;
        }
        
        event.consume();
    }
    
    public void handleKeyReleased(KeyEvent event) {
        
        switch (event.getCode()) {
            case W:
                wPressed = false;
                break;
            case S:
                sPressed = false;
                break;
            case UP:
                upPressed = false;
                break;
            case DOWN:
                downPressed = false;
                break;
        }
        
        event.consume();
    }

    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }
}