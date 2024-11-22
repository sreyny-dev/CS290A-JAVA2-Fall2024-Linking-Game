package org.example.demo;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.stream.Collectors;

public class GameServer {
    private static final int PORT = 4444;

    private static Map<String, Queue<ClientHandler>> waitingPlayers = new HashMap<>();
    private static List<ClientHandler> connectedPlayers = Collections.synchronizedList(new ArrayList<>());


    public static void main(String[] args) {
        System.out.println("Game server started...");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Server is shutting down...");
            synchronized (connectedPlayers) {
                for (ClientHandler player : connectedPlayers) {
                    try {
                        player.out.println("SERVER_SHUTDOWN");
                        player.socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }));
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                ClientHandler clientHandler = new ClientHandler(serverSocket.accept());
                synchronized (connectedPlayers) {
                    connectedPlayers.add(clientHandler);
                }
                clientHandler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static synchronized void addPlayerToQueue(ClientHandler player, String boardSize) {
        Queue<ClientHandler> queue = waitingPlayers.computeIfAbsent(boardSize, k -> new LinkedList<>());

        if (!queue.contains(player)) {
            queue.add(player);
        }

        queue.removeIf(client -> client.socket.isClosed());


        if (queue.size() >= 2) {
            // Match two players and pass boardSize to startGame
            ClientHandler player1 = queue.poll();
            ClientHandler player2 = queue.poll();
            startGame(player1, player2, boardSize);
        }
    }

    private static void startGame(ClientHandler player1, ClientHandler player2, String boardSize) {
        System.out.println("Matching players: " + player1.username + " and " + player2.username);

        // Notify both players about the match
        player1.out.println("MATCH_FOUND");
        player2.out.println("MATCH_FOUND");

        // Generate the game board based on boardSize and send it to both players
        String[] dimensions = boardSize.split("x");
        int rows = Integer.parseInt(dimensions[0]);
        int cols = Integer.parseInt(dimensions[1]);
        int[][] boardGame = generateBoard(rows, cols);

        // Set the game board for both players
        player1.setGame(boardGame, player2);
        player2.setGame(boardGame, player1);

//        player1.isTurn = true;
//        player2.isTurn = false;


        player1.setTurn(true);
        player2.setTurn(false);

//        player1.out.println("YOUR_TURN");
//        player2.out.println("NOT_YOUR_TURN");

        // Send the board to both players
        player1.sendBoard(boardGame);
        player2.sendBoard(boardGame);

        System.out.println("Sent MATCH_FOUND and board to both players.");
    }

    private static int[][] generateBoard(int rows, int cols) {
        int[][] board = new int[rows][cols];
        Random random = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                board[i][j] = random.nextInt(12);
            }
        }
        return board;
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private String boardSize;
        private ClientHandler opponent;
        private int[][] boardGame;
        private static int actionsInTurn = 0;
        private ArrayList<String> tempCoordinates =  new ArrayList<>();

        private boolean isTurn = false;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void setTurn(boolean turn){
            this.isTurn = turn;
            out.println(turn? "YOUR_TURN" : "NOT_YOUR_TURN");// Notify player of their turn status
        }

        public void setGame(int[][] board, ClientHandler opponent) {
            this.boardGame = board;
            this.opponent = opponent;
        }

        public void sendBoard(int[][] board) {
            String boardConfig = convertBoardToString(board); // Convert board to string for sending
            out.println("BOARD:" + boardConfig);
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                // Read username and board size from client
                username = in.readLine();
                System.out.println(username+ " connected to the server...");

                boardSize = in.readLine(); // e.g., "4x4", "6x8", "8x8"
                System.out.println(username + " selected board size " + boardSize);

                // Add player to matchmaking queue
                addPlayerToQueue(this, boardSize);

                // Wait for further commands or disconnection
                String message;
                while ((message = in.readLine()) != null) {


                    System.out.println(username + " sent: " + message);


                    if (message.equals("disconnect")) {
                        handleDisconnection();
                        break;

                    }
                    if (message.startsWith("PRESS:")){
                        actionsInTurn++;
                        String[] values = message.split(":")[1].split(",");
                        for(String value : values){
                            tempCoordinates.add(value);
                        }
                    }

                    if (actionsInTurn >= 2) {

                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < tempCoordinates.size(); i++) {
                            sb.append(tempCoordinates.get(i));
                            if (i < tempCoordinates.size() - 1) {
                                sb.append(","); // Add a comma between elements
                            }
                        }

                        String lineCoordinate = sb.toString();
                        String drawLineMessage = "DRAW_LINE:"+lineCoordinate;
                        this.out.println(drawLineMessage);       // Notify the player
                        if(opponent!=null) {
                            opponent.out.println(drawLineMessage);  // Notify the opponent
                        }
                        tempCoordinates.clear();
                        passTurnToOpponent(this);
                    }

                    if(message.startsWith("REMOVE:")){

                        String boardState = message.substring("REMOVE:".length());  // Extract board state
                        String updatedBoard = "UPDATED_BOARD:" + boardState;
                        this.out.println(updatedBoard);
                        opponent.out.println(updatedBoard);
                        passTurnToOpponent(this);
                    }

                    if(message.startsWith("SCORE:")){
                        String score = message.substring("SCORE:".length());
                        this.out.println("YOUR_SCORE:" + score);
                    }
//
//                    if(message.startsWith("GAME_OVER")){
//                        this.out.println("YOU_LOSE");
//                        opponent.out.println("YOU_WIN");
//                        break;
//                    }

                    if (message.startsWith("GAME_OVER")) {
                        this.out.println("YOU_LOSE");
                        System.out.println(username+" lose");
                        System.out.println(opponent.username+" win");
                        opponent.out.println("YOU_WIN");

                        // Reset both players
                        this.resetGame();
                        if (opponent != null) {
                            opponent.resetGame();
                        }
                        continue; // Continue listening for new messages
                    }


                }
            } catch (IOException e) {
                System.out.println("Client disconnected: " + socket.getInetAddress().getHostAddress());
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void resetGame() {
            this.boardGame = null;  // Clear the game board
            this.opponent = null;   // Clear the opponent reference
            this.isTurn = false;    // Reset turn state
            actionsInTurn = 0;      // Reset actions
            tempCoordinates.clear(); // Clear temporary data

            // Notify the player
            out.println("GAME_RESET");
            out.println("Do you want to play again? Type 'rematch' or 'disconnect'");
        }

        private void handleDisconnection() {
            System.out.println(username + " is disconnecting...");

            synchronized (connectedPlayers) {
                connectedPlayers.remove(this); // Remove from connected players
            }

            synchronized (waitingPlayers) {
                Queue<ClientHandler> queue = waitingPlayers.get(boardSize);
                if (queue != null) {
                    queue.remove(this); // Remove from matchmaking queue
                    if (queue.isEmpty()) {
                        waitingPlayers.remove(boardSize); // Clean up empty queues
                    }
                }
            }

            if (opponent != null) {
                opponent.out.println("opponent_disconnected"); // Notify opponent
                opponent.opponent = null;
            }

            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        private static String convertBoardToString(int[][] board) {
            StringBuilder sb = new StringBuilder();
            for (int[] row : board) {
                for (int cell : row) {
                    sb.append(cell).append(",");
                }
                sb.append(";"); // Row separator
            }
            return sb.toString();
        }
        private static synchronized void passTurnToOpponent(ClientHandler currentPlayer) {
            currentPlayer.setTurn(false);
            currentPlayer.opponent.setTurn(true); // Pass the turn to the opponent

            currentPlayer.out.println("NOT_YOUR_TURN");
            currentPlayer.opponent.out.println("YOUR_TURN");
            actionsInTurn = 0;
        }

    }
}
