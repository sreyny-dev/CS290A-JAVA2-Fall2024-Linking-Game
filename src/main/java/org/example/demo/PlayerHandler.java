package org.example.demo;

import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class PlayerHandler implements Runnable {
    private Controller controller; // Controller is initially null
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean active;
    private volatile boolean isPlayerTurn = false;
    private int playerScore = 0;

    public PlayerHandler(Socket socket) {
        this.socket = socket;
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            active = true; // Set the handler as active
        } catch (IOException e) {
            e.printStackTrace();
            active = false; // Mark as inactive if there's an error
        }
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    private BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();


    @Override
    public void run() {
        try {
            String message;
            while (active && (message = in.readLine()) != null) {
                messageQueue.offer(message);
                System.out.println("Received from server: " + message);

                if(message.startsWith("YOUR_TURN")){
                    isPlayerTurn = true;
                    System.out.println("it is your turn");
                }else if(message.startsWith("NOT_YOUR_TURN")){
                    isPlayerTurn = false;
                    System.out.println("It is not your turn");
                }else if(message.equals("YOU_WIN")){
                    System.out.println("You Win!");
                    Platform.runLater(() -> {
                        try {
                            Application.onGameOverWinner();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    break;
                }else if(message.equals("YOU_LOSE")){
                    System.out.println("You Lose!");
                    Platform.runLater(() -> {
                        try {
                            Application.onGameOverLoser();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    break;
                }else if(message.startsWith("YOUR_SCORE:")){
                    playerScore = Integer.parseInt(message.split(" ")[1]);
                }


                if (message.startsWith("UPDATED_BOARD:")) {
                    String[] toRemove = message.split(":")[1].split(",");
                    int row1 = Integer.parseInt(toRemove[0]);
                    int col1 = Integer.parseInt(toRemove[1]);
                    int row2 = Integer.parseInt(toRemove[2]);
                    int col2 = Integer.parseInt(toRemove[3]);

                    // Check if controller is set before calling updateBoard
                    if (controller != null) {
                        Platform.runLater(() -> controller.updateBoard(row1, col1, row2, col2));                    } else {
                        System.err.println("Controller is not set!");
                    }
                }
            }
        } catch (IOException e) {
            if (active) {
                e.printStackTrace();
            }
        } finally {
            close();
        }
    }

    public void sendMessage(String message) {
        if (active) {
            out.println(message);
        }
    }

    public void close() {
        if (!active) return;
        try {
            active = false;
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendBoardSize(int rows, int columns) {
        if (active) {
            out.println(rows + "x" + columns);
        }
    }

    public String getNextMessage() throws InterruptedException {
        return messageQueue.take();
    }
    public boolean isPlayerTurn() {
        return isPlayerTurn;
    }

    public void sendScore(int score){
        sendMessage("SCORE: " + score);
    }

    public static int[][] parseBoardData(String boardContent) {
        String data = boardContent.replace("BOARD:", "");
        String[] rows = data.split(";");
        int[][] board = new int[rows.length][];

        for (int i = 0; i < rows.length; i++) {
            String[] cells = rows[i].split(",");
            board[i] = new int[cells.length];
            for (int j = 0; j < cells.length; j++) {
                board[i][j] = Integer.parseInt(cells[j].trim());
            }
        }
        return board;
    }

}