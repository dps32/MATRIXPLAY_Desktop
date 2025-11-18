package com.client;

import java.net.URL;
import java.util.ResourceBundle;

import org.json.JSONObject;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.AnchorPane;

public class GameCtrl implements Initializable {
    
    @FXML 
    private Canvas canvaPartida;
    
    @FXML
    private AnchorPane rootPane;

    private GraphicsContext gcGame;
    
    // Dimensiones de las palas
    private final double paddleWidth = 20;
    private final double paddleHeight = 150;
    private final double paddleMargin = 15;
    
    // Dimensiones de la pelota
    private final double ballRadius = 15;
    
    // Posiciones de las palas (0-1 como viene del servidor)
    private double leftPaddlePosition = 0.5;
    private double rightPaddlePosition = 0.5;
    
    // Posición de la pelota (0-1 como viene del servidor)
    private double ballX = 0.5;
    private double ballY = 0.5;
    
    // Puntuación
    private int player1Score = 0;
    private int player2Score = 0;
    
    // Estado del juego
    private boolean isGameRunning = false;
    
    // Identificador del jugador
    private int playerId = -1;
    
    // Controles
    private boolean wPressed = false;
    private boolean sPressed = false;
    
    private AnimationTimer gameLoop;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gcGame = canvaPartida.getGraphicsContext2D();
        
        // Configurar event handlers programáticamente
        if (rootPane != null) {
            rootPane.setOnKeyPressed(this::handleKeyPressed);
            rootPane.setOnKeyReleased(this::handleKeyReleased);
            rootPane.setFocusTraversable(true);
            rootPane.requestFocus();
        }
        
        // Configurar el bucle de juego
        setupGameLoop();
        
        // Configurar listeners para el tamaño del canvas
        canvaPartida.widthProperty().addListener((obs, oldVal, newVal) -> drawGame());
        canvaPartida.heightProperty().addListener((obs, oldVal, newVal) -> drawGame());
        
        drawGame();
    }
    
    private void setupGameLoop() {
        gameLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                drawGame();
                handleLocalInput();
            }
        };
        gameLoop.start();
    }
    
    private void handleLocalInput() {
        // Solo procesar input si el juego está corriendo y somos el jugador 1 o 2
        if (!isGameRunning || playerId == -1) return;
        
        double moveAmount = 0.02; // Velocidad de movimiento
        
        if (playerId == 1) {
            // Jugador 1 controla pala izquierda con W/S
            if (wPressed && leftPaddlePosition > 0) {
                leftPaddlePosition -= moveAmount;
                sendPaddleMove(leftPaddlePosition);
            }
            if (sPressed && leftPaddlePosition < 1) {
                leftPaddlePosition += moveAmount;
                sendPaddleMove(leftPaddlePosition);
            }
        } else if (playerId == 2) {
            // Jugador 2 controla pala derecha con W/S
            if (wPressed && rightPaddlePosition > 0) {
                rightPaddlePosition -= moveAmount;
                sendPaddleMove(rightPaddlePosition);
            }
            if (sPressed && rightPaddlePosition < 1) {
                rightPaddlePosition += moveAmount;
                sendPaddleMove(rightPaddlePosition);
            }
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
    
    /**
     * Actualiza el estado del juego con datos del servidor
     */
    public void updateGameState(JSONObject gameState) {
        try {
            // Actualizar pelota
            JSONObject ball = gameState.getJSONObject("ball");
            ballX = ball.getDouble("x");
            ballY = ball.getDouble("y");
            
            // Actualizar palas
            JSONObject paddle1 = gameState.getJSONObject("paddle1");
            JSONObject paddle2 = gameState.getJSONObject("paddle2");
            leftPaddlePosition = paddle1.getDouble("y");
            rightPaddlePosition = paddle2.getDouble("y");
            
            // Actualizar puntuación
            JSONObject score = gameState.getJSONObject("score");
            player1Score = score.getInt("player1");
            player2Score = score.getInt("player2");
            
            // Actualizar estado del juego
            isGameRunning = gameState.getBoolean("running");
            
        } catch (Exception e) {
            System.err.println("Error parsing game state: " + e.getMessage());
        }
    }
    
    /**
     * Establece el ID del jugador
     */
    public void setPlayerId(int id) {
        this.playerId = id;
        System.out.println("Player ID set to: " + id);
    }
    
    private void drawGame() {
        // Limpiar canvas
        gcGame.clearRect(0, 0, canvaPartida.getWidth(), canvaPartida.getHeight());
        
        // Fondo negro
        gcGame.setFill(Color.BLACK);
        gcGame.fillRect(0, 0, canvaPartida.getWidth(), canvaPartida.getHeight());
        
        // Verificar que el canvas tiene tamaño
        if (canvaPartida.getWidth() == 0 || canvaPartida.getHeight() == 0) {
            return;
        }
        
        // Dibujar zonas de gol
        gcGame.setFill(Color.rgb(255, 128, 0, 0.3));
        gcGame.fillRect(0, 0, 15, canvaPartida.getHeight());
        gcGame.fillRect(canvaPartida.getWidth() - 15, 0, 15, canvaPartida.getHeight());
        
        // Dibujar línea central punteada
        drawCenterLine();
        
        // Dibujar palas
        drawLeftPaddle();
        drawRightPaddle();
        
        // Dibujar pelota
        drawBall();
        
        // Dibujar puntuación
        drawScore();
        
        // Dibujar información del jugador
        drawPlayerInfo();
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
    
    private void drawLeftPaddle() {
        double paddleY = leftPaddlePosition * canvaPartida.getHeight();
        double top = paddleY - (paddleHeight / 2);
        
        // Asegurar que la pala no se salga de los límites
        top = Math.max(paddleMargin, Math.min(top, canvaPartida.getHeight() - paddleHeight - paddleMargin));
        
        gcGame.setFill(Color.WHITE);
        gcGame.fillRect(30, top, paddleWidth, paddleHeight);
    }
    
    private void drawRightPaddle() {
        double paddleY = rightPaddlePosition * canvaPartida.getHeight();
        double top = paddleY - (paddleHeight / 2);
        
        // Asegurar que la pala no se salga de los límites
        top = Math.max(paddleMargin, Math.min(top, canvaPartida.getHeight() - paddleHeight - paddleMargin));
        
        gcGame.setFill(Color.WHITE);
        gcGame.fillRect(canvaPartida.getWidth() - 30 - paddleWidth, top, paddleWidth, paddleHeight);
    }
    
    private void drawScore() {
        gcGame.setFill(Color.WHITE);
        gcGame.setFont(javafx.scene.text.Font.font("Arial", 30));
        
        // Puntuación jugador 1 (izquierda)
        gcGame.fillText(String.valueOf(player1Score), canvaPartida.getWidth() / 4, 50);
        
        // Puntuación jugador 2 (derecha)
        gcGame.fillText(String.valueOf(player2Score), 3 * canvaPartida.getWidth() / 4, 50);
    }
    
    private void drawPlayerInfo() {
        gcGame.setFill(Color.YELLOW);
        gcGame.setFont(javafx.scene.text.Font.font("Arial", 16));
        
        String playerText = "Jugador: " + (playerId == 1 ? "1 (Izquierda - W/S)" : playerId == 2 ? "2 (Derecha - W/S)" : "Esperando...");
        String gameState = "Estado: " + (isGameRunning ? "EN JUEGO" : "PAUSADO");
        
        gcGame.fillText(playerText, 20, canvaPartida.getHeight() - 40);
        gcGame.fillText(gameState, 20, canvaPartida.getHeight() - 20);
    }
    
    // Métodos para manejar input del teclado
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