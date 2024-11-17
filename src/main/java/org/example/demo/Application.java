package org.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;

import static org.example.demo.PlayerHandler.parseBoardData;


public class Application extends javafx.application.Application {

    private static Stage primaryStage;
    private static PlayerHandler playerHandler;


    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        showLoginScreen();

        // Setup the window close event to disconnect the client
        primaryStage.setOnCloseRequest(event -> {
            try {
                disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void disconnect() throws IOException {
        if (playerHandler != null) {
            playerHandler.sendMessage("disconnect"); // Send disconnect message to server
            playerHandler.close(); // Close the player handler
        }
        System.out.println("Disconnected from server.");
    }


    public static void showStartScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("StartScreen.fxml"));

        Parent rootParent = fxmlLoader.load();
        VBox root = (VBox) rootParent;

        // Load the background image
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image(Application.class.getResource("/org/example/demo/bg.png").toExternalForm()),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
        );

        root.setBackground(new Background(backgroundImage));

        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Select Board Size");
        primaryStage.show();
    }

    public static void showLoginScreen() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("Login.fxml"));
        VBox root = fxmlLoader.load();
        Scene scene = new Scene(root, 400, 400);

        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    public static void showWaitingScreen(int rows, int columns) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("waitingScreen.fxml"));
        VBox root = fxmlLoader.load();
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Waiting to match");
        primaryStage.show();
        System.out.println("Waiting...");

        // Send board size to server
        playerHandler.sendBoardSize(rows, columns);

        new Thread(() -> {
            try {
                String response;
                boolean matchFound = false;

                while (true) {
                    response = playerHandler.getNextMessage(); // Retrieve the next message from queue

                    if ("MATCH_FOUND".equals(response)) {
                        matchFound = true;
                    } else if (matchFound && response.startsWith("BOARD:")) {
                        // Now we expect the board data after MATCH_FOUND
                        String boardContent = response; // e.g., "BOARD:3,1,0,3,;3,8,6,8,;3,1,8,1,;5,4,2,6,;"

                        // Display the game screen with the received board data
                        Platform.runLater(() -> showGameScreen(boardContent));
                        break;
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

        public static void showGameScreen(String boardDataFromServer) {
        try {

            Controller.game = new Game(parseBoardData(boardDataFromServer));

            FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("board.fxml"));
            VBox root = fxmlLoader.load();

            // Access the controller to initialize the game board
            Controller controller = fxmlLoader.getController();
            controller.createGameBoard();


            Scene scene = new Scene(root, 400, 400);


            // Center the stage on the screen (optional)
            primaryStage.setScene(scene);
            primaryStage.setTitle("LianlianKan Game");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void onGameOverWinner() throws IOException {
        System.out.println("Game Over! No moves available.");

        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("gameOver.fxml"));

        Parent rootParent = fxmlLoader.load();


        // Get the controller and pass data
        Controller controller = fxmlLoader.getController();
//        controller.setScore(score);
        controller.setGameOverMessage("YOU WIN");

        VBox root = (VBox) rootParent;
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Game Over!");
        primaryStage.show();
    }

    public static void onGameOverLoser() throws IOException {

        System.out.println("Game Over! No moves available.");

        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("gameOver.fxml"));

        Parent rootParent = fxmlLoader.load();


        // Get the controller and pass data
        Controller controller = fxmlLoader.getController();
//        controller.setScore(score);
        controller.setGameOverMessage("YOU LOSE");

        VBox root = (VBox) rootParent;
        Scene scene = new Scene(root, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Game Over!");
        primaryStage.show();
    }

    public static void setPlayerHandler(PlayerHandler handler) {
        playerHandler = handler; // Allow other classes to set the player handler
    }
    public static void main(String[] args) {
        launch();
    }
}