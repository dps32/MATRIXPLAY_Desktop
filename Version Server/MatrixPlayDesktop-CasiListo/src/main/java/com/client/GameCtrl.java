package com.client;

import java.net.URL;
import java.util.ResourceBundle;
import org.json.JSONObject;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;

public class GameCtrl implements Initializable {

    @FXML private Canvas canvaPartida;
    @FXML private BorderPane rootPane;

    private GraphicsContext gcGame;
    
    // elementos partida
    private final double paddleWidth = 20;
    private final double paddleHeight = 150;
    private final double paddleMargin = 15;
    private final double paddleSpeed = 0.003; 

    private boolean upPressed = false;
    private boolean downPressed = false;
    
    private final double ballRadius = 15;
    private double ballX = 0.5;
    private double ballY = 0.5;

    private int player1Score;
    private int player2Score;
    private AnimationTimer gameLoop;

    private int lastPlayer1Score = 0;
    private int lastPlayer2Score = 0;
    private boolean gameFinished = false;

    // pos server
    private double leftPaddlePosition = 0.5;
    private double rightPaddlePosition = 0.5;
    
    // pos locales
    private double localLeftPaddlePosition = 0.5;
    private double localRightPaddlePosition = 0.5;

    // extras game
    private int playerId = 0;
    private boolean gameStarted = false;

    // esto ayuda a que el server no se sature
    private long lastMoveTime = 0;
    private static final long MOVE_THROTTLE_MS = 16; // 60fps

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gcGame = canvaPartida.getGraphicsContext2D();
        setupFocus();

        canvaPartida.widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                drawGame();
            }
        });
        canvaPartida.heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                drawGame();
            }
        });
    }

      public void startGame() {
        gameStarted = true;
        gameFinished = false;
        player1Score = 0;
        player2Score = 0;
        lastPlayer1Score = 0;
        lastPlayer2Score = 0;
        drawGame();
        setupGameLoop();
    }

    private void setupFocus() {
        rootPane.setFocusTraversable(true);
        rootPane.requestFocus();
        
        rootPane.setOnMouseClicked(e -> rootPane.requestFocus());
        canvaPartida.setOnMouseClicked(e -> rootPane.requestFocus());
    }

    private void setupGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }
        
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
        if (!gameStarted || playerId == 0) 
            return;

        boolean positionChanged = false;
        double newPosition = 0;

        // p1 flechas
        if (playerId == 1) {
            if (upPressed && !downPressed) {
                localLeftPaddlePosition = Math.max(0, localLeftPaddlePosition - paddleSpeed);
                positionChanged = true;
            }
            if (downPressed && !upPressed) {
                localLeftPaddlePosition = Math.min(1, localLeftPaddlePosition + paddleSpeed);
                positionChanged = true;
            }
            if (positionChanged) {
                newPosition = localLeftPaddlePosition;
            }
        }
        // p2 flechas 
        else if (playerId == 2) {
            if (upPressed && !downPressed) {
                localRightPaddlePosition = Math.max(0, localRightPaddlePosition - paddleSpeed);
                positionChanged = true;
            }
            if (downPressed && !upPressed) {
                localRightPaddlePosition = Math.min(1, localRightPaddlePosition + paddleSpeed);
                positionChanged = true;
            }
            if (positionChanged) {
                newPosition = localRightPaddlePosition;
            }
        }

        // mejor flujo server
        if (positionChanged) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastMoveTime >= MOVE_THROTTLE_MS) {
                sendPaddleMove(newPosition);
                lastMoveTime = currentTime;
            }
        }
    }

    private void sendPaddleMove(double position) { 
        if (Main.wsClient != null && Main.wsClient.isOpen()) {
            try {
                JSONObject moveMsg = new JSONObject();
                moveMsg.put("type", "paddleMove");
                moveMsg.put("y", position);
                Main.wsClient.safeSend(moveMsg.toString());
            } catch (Exception e) {
                System.err.println("Error enviando movimiento: " + e.getMessage());
            }
        }
    }

    public void updateGameState(JSONObject gameState) {
        try {
            
            JSONObject ball = gameState.getJSONObject("ball");
            this.ballX = ball.getDouble("x");
            this.ballY = ball.getDouble("y");
            
            
            JSONObject paddle1 = gameState.getJSONObject("paddle1");
            JSONObject paddle2 = gameState.getJSONObject("paddle2");
            
            this.leftPaddlePosition = paddle1.getDouble("y");
            this.rightPaddlePosition = paddle2.getDouble("y");
            
            if (playerId == 1) {
                if (!upPressed && !downPressed) {
                    localLeftPaddlePosition = leftPaddlePosition;
                }
            } else if (playerId == 2) {
                if (!upPressed && !downPressed) {
                    localRightPaddlePosition = rightPaddlePosition;
                }
            }

            // puntuacion
            JSONObject score = gameState.getJSONObject("score");
            player1Score = score.getInt("player1");
            player2Score = score.getInt("player2");

             if (!gameFinished && (player1Score >= 10 || player2Score >= 10)) {
                gameFinished = true;
                determineWinner();
            }
            
            lastPlayer1Score = player1Score;
            lastPlayer2Score = player2Score;
            
        } catch (Exception e) {
            System.err.println("Error actualizando gameState: " + e.getMessage());
        }
    }

    private void determineWinner() {
        String winnerName;
        String loserName;
        int winnerScore;
        int loserScore;

        if (player1Score >= 10) {
            winnerName = getPlayerName(1);
            loserName = getPlayerName(2);
            winnerScore = player1Score;
            loserScore = player2Score;
        } else {
            winnerName = getPlayerName(2);
            loserName = getPlayerName(1);
            winnerScore = player2Score;
            loserScore = player1Score;
        }

        // Parar el game loop
        if (gameLoop != null) {
            gameLoop.stop();
        }

        // NO desconectar el WebSocket - mantener la conexión activa
        // El servidor manejará la desconexión/reconexión si es necesario

        // Mostrar pantalla de ganador
        Platform.runLater(() -> {
            UtilsViews.setView("ViewWin");
            
            WinnerCtrl winnerCtrl = (WinnerCtrl) UtilsViews.getController("ViewWin");
            if (winnerCtrl != null) {
                winnerCtrl.setGameResult(winnerName, winnerScore, loserName, loserScore);
            }
        });
    }

    private String getPlayerName(int playerId) { // CHECA ESTO 
        // Aquí necesitas una manera de obtener los nombres de los jugadores
        // Puedes guardarlos cuando llegue el mensaje "playerNames" del servidor
        if (playerId == 1) {
            return Main.namePlayerDesktop != null ? Main.namePlayerDesktop : "Player 1";
        } else {
            return Main.namePlayerMobile; // O como obtengas el nombre del otro jugador
        }
    }


    public void setPlayerId(int playerId) {
        this.playerId = playerId;
        System.out.println("Asignado como Jugador " + playerId);
    }

    // dibujo partida
    private void drawGame() {
        gcGame.clearRect(0, 0, canvaPartida.getWidth(), canvaPartida.getHeight());

        gcGame.setFill(Color.BLACK);
        gcGame.fillRect(0, 0, canvaPartida.getWidth(), canvaPartida.getHeight());

        if (canvaPartida.getWidth() == 0 || canvaPartida.getHeight() == 0) {
            return;
        }

        drawScore();
        drawCenterLine();
        drawLeftPaddle();
        drawRightPaddle();
        drawBall();
    }

    private void drawLeftPaddle() {
        double drawPosition;

        if (playerId == 1) {
            drawPosition = localLeftPaddlePosition;  
        } else {
            drawPosition = leftPaddlePosition;       
        }
        
        double paddleCenterY = drawPosition * canvaPartida.getHeight();
        double limitTop = paddleCenterY - (paddleHeight / 2);
        
        limitTop = Math.max(paddleMargin, Math.min(limitTop, canvaPartida.getHeight() - paddleHeight - paddleMargin));
        
        if (playerId == 1) {
            gcGame.setFill(Color.WHITESMOKE);
        } else {
            gcGame.setFill(Color.WHITESMOKE);
        }
        gcGame.fillRect(30, limitTop, paddleWidth, paddleHeight);
    }

    private void drawRightPaddle() {
        double drawPosition;

        if (playerId == 2) {
            drawPosition = localRightPaddlePosition;  
        } else {
            drawPosition = rightPaddlePosition;       
        }
            
        double paddleCenterY = drawPosition * canvaPartida.getHeight();
        double limitTop = paddleCenterY - (paddleHeight / 2);

        limitTop = Math.max(paddleMargin, Math.min(limitTop, canvaPartida.getHeight() - paddleHeight - paddleMargin));
        
        if (playerId == 2) {
            gcGame.setFill(Color.WHITESMOKE);
        } else {
            gcGame.setFill(Color.WHITESMOKE);
        }
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

    private void drawScore() {
        gcGame.setFill(Color.WHITE);
        gcGame.setFont(javafx.scene.text.Font.font("Arial", 30));
        
        gcGame.fillText(String.valueOf(player1Score), canvaPartida.getWidth() / 4, 50);
        gcGame.fillText(String.valueOf(player2Score), 3 * canvaPartida.getWidth() / 4, 50);
    }


    // controlar
    public void handleKeyPressed(KeyEvent event) {
        if (!gameStarted) 
            return;
        
        switch (event.getCode()) {
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