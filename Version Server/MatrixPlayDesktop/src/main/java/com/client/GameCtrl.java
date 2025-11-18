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
    
    // Posiciones locales de las palas (para input del jugador)
    private double localLeftPaddlePosition = 0.5;
    private double localRightPaddlePosition = 0.5;
    
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
    
    // Controles - ahora con flechas
    private boolean upPressed = false;
    private boolean downPressed = false;
    
    // Tiempo del último movimiento enviado (para throttling)
    private long lastMoveTime = 0;
    private static final long MOVE_THROTTLE_MS = 16; // ~60fps
    
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
        boolean positionChanged = false;
        double newPosition = 0;
        
        if (playerId == 1) {
            // Jugador 1 controla pala izquierda con FLECHAS
            if (upPressed && localLeftPaddlePosition > 0) {
                localLeftPaddlePosition -= moveAmount;
                localLeftPaddlePosition = Math.max(0, localLeftPaddlePosition);
                positionChanged = true;
            }
            if (downPressed && localLeftPaddlePosition < 1) {
                localLeftPaddlePosition += moveAmount;
                localLeftPaddlePosition = Math.min(1, localLeftPaddlePosition);
                positionChanged = true;
            }
            
            if (positionChanged) {
                newPosition = localLeftPaddlePosition;
            }
        } else if (playerId == 2) {
            // Jugador 2 controla pala derecha con FLECHAS
            if (upPressed && localRightPaddlePosition > 0) {
                localRightPaddlePosition -= moveAmount;
                localRightPaddlePosition = Math.max(0, localRightPaddlePosition);
                positionChanged = true;
            }
            if (downPressed && localRightPaddlePosition < 1) {
                localRightPaddlePosition += moveAmount;
                localRightPaddlePosition = Math.min(1, localRightPaddlePosition);
                positionChanged = true;
            }
            
            if (positionChanged) {
                newPosition = localRightPaddlePosition;
            }
        }
        
        // Enviar movimiento con throttling para no saturar el servidor
        if (positionChanged) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMoveTime >= MOVE_THROTTLE_MS) {
                sendPaddleMove(newPosition);
                lastMoveTime = currentTime;
            }
        }
    }
    
    private void sendPaddleMove(double position) {
        if (Main.wsClient != null) {
            try {
                JSONObject moveMsg = new JSONObject();
                moveMsg.put("type", "paddleMove");
                moveMsg.put("y", position);
                Main.wsClient.safeSend(moveMsg.toString());
                System.out.println("Enviando movimiento de paleta - Jugador " + playerId + ": " + position);
            } catch (Exception e) {
                System.err.println("Error enviando movimiento: " + e.getMessage());
            }
        }
    }
    
    /**
     * Actualiza el estado del juego con datos del servidor
     */
    public void updateGameState(JSONObject gameState) {
        try {
            // El servidor envía el gameState con un campo "type" en el nivel raíz
            // Extraer los datos del objeto gameState interno
            JSONObject ball = gameState.getJSONObject("ball");
            ballX = ball.getDouble("x");
            ballY = ball.getDouble("y");
            
            // Actualizar palas DESDE EL SERVIDOR
            JSONObject paddle1 = gameState.getJSONObject("paddle1");
            JSONObject paddle2 = gameState.getJSONObject("paddle2");
            
            leftPaddlePosition = paddle1.getDouble("y");
            rightPaddlePosition = paddle2.getDouble("y");
            
            // Para el jugador local, usar posición local para feedback inmediato
            // pero también actualizar la posición del servidor para referencia
            if (playerId == 1) {
                // Solo actualizar si no estamos moviendo activamente
                if (!upPressed && !downPressed) {
                    localLeftPaddlePosition = leftPaddlePosition;
                }
            } else if (playerId == 2) {
                // Solo actualizar si no estamos moviendo activamente
                if (!upPressed && !downPressed) {
                    localRightPaddlePosition = rightPaddlePosition;
                }
            } else {
                // Espectador: sincronizar completamente
                localLeftPaddlePosition = leftPaddlePosition;
                localRightPaddlePosition = rightPaddlePosition;
            }
            
            // Actualizar puntuación
            JSONObject score = gameState.getJSONObject("score");
            player1Score = score.getInt("player1");
            player2Score = score.getInt("player2");
            
            // Actualizar estado del juego
            isGameRunning = gameState.getBoolean("running");
            
            System.out.println("GameState actualizado - Ball: (" + ballX + "," + ballY + 
                             "), P1: " + leftPaddlePosition + ", P2: " + rightPaddlePosition);
            
        } catch (Exception e) {
            System.err.println("Error parsing game state: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Establece el ID del jugador
     */
    public void setPlayerId(int id) {
        this.playerId = id;
        System.out.println("Player ID set to: " + id);
        
        // Resetear posiciones locales cuando se asigna un nuevo jugador
        if (id == 1) {
            localLeftPaddlePosition = 0.5;
            leftPaddlePosition = 0.5;
        } else if (id == 2) {
            localRightPaddlePosition = 0.5;
            rightPaddlePosition = 0.5;
        } else {
            localLeftPaddlePosition = 0.5;
            localRightPaddlePosition = 0.5;
            leftPaddlePosition = 0.5;
            rightPaddlePosition = 0.5;
        }
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
        
        // Dibujar información de debug
        drawDebugInfo();
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
        double drawPosition = (playerId == 1) ? localLeftPaddlePosition : leftPaddlePosition;
        double paddleY = drawPosition * canvaPartida.getHeight();
        double top = paddleY - (paddleHeight / 2);
        
        // Asegurar que la pala no se salga de los límites
        top = Math.max(paddleMargin, Math.min(top, canvaPartida.getHeight() - paddleHeight - paddleMargin));
        
        // Color diferente para el jugador local
        if (playerId == 1) {
            gcGame.setFill(Color.CYAN);
        } else {
            gcGame.setFill(Color.WHITE);
        }
        gcGame.fillRect(30, top, paddleWidth, paddleHeight);
    }
    
    private void drawRightPaddle() {
        double drawPosition = (playerId == 2) ? localRightPaddlePosition : rightPaddlePosition;
        double paddleY = drawPosition * canvaPartida.getHeight();
        double top = paddleY - (paddleHeight / 2);
        
        // Asegurar que la pala no se salga de los límites
        top = Math.max(paddleMargin, Math.min(top, canvaPartida.getHeight() - paddleHeight - paddleMargin));
        
        // Color diferente para el jugador local
        if (playerId == 2) {
            gcGame.setFill(Color.CYAN);
        } else {
            gcGame.setFill(Color.WHITE);
        }
        gcGame.fillRect(canvaPartida.getWidth() - 30 - paddleWidth, top, paddleWidth, paddleHeight);
    }
    
    private void drawDebugInfo() {
        gcGame.setFill(Color.YELLOW);
        gcGame.setFont(javafx.scene.text.Font.font("Arial", 14));
        
        String playerText = "Jugador: " + (playerId == 1 ? "1 (Izquierda)" : playerId == 2 ? "2 (Derecha)" : "Espectador");
        String controls = "Controles: FLECHAS ARRIBA/ABAJO";
        String state = "Estado: " + (isGameRunning ? "JUGANDO" : "PAUSA");
        
        gcGame.fillText(playerText, 20, 30);
        gcGame.fillText(controls, 20, 50);
        gcGame.fillText(state, 20, 70);
        
        // Info de posiciones para debug
        if (playerId == 1) {
            String posInfo = String.format("Pos: %.2f (local) / %.2f (server)", localLeftPaddlePosition, leftPaddlePosition);
            gcGame.fillText(posInfo, 20, 90);
        } else if (playerId == 2) {
            String posInfo = String.format("Pos: %.2f (local) / %.2f (server)", localRightPaddlePosition, rightPaddlePosition);
            gcGame.fillText(posInfo, 20, 90);
        }
    }
    
    // Métodos para manejar input del teclado - AHORA CON FLECHAS
    public void handleKeyPressed(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            upPressed = true;
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN) {
            downPressed = true;
            event.consume();
        }
    }
    
    public void handleKeyReleased(KeyEvent event) {
        if (event.getCode() == KeyCode.UP) {
            upPressed = false;
            event.consume();
        } else if (event.getCode() == KeyCode.DOWN) {
            downPressed = false;
            event.consume();
        }
    }
    
    // Método para limpiar recursos
    public void cleanup() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
    }

    public void requestFocus() {
        if (rootPane != null) {
            rootPane.requestFocus();
        }
    }
    
    public void resetGame() {
        leftPaddlePosition = 0.5;
        rightPaddlePosition = 0.5;
        localLeftPaddlePosition = 0.5;
        localRightPaddlePosition = 0.5;
        ballX = 0.5;
        ballY = 0.5;
        player1Score = 0;
        player2Score = 0;
        isGameRunning = false;
        upPressed = false;
        downPressed = false;
        
        drawGame();
    }
    
    /**
     * Obtener el ID del jugador actual
     */
    public int getPlayerId() {
        return playerId;
    }
}