package org.example.demo;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;


import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;

import java.io.*;
import java.util.Objects;

import javafx.scene.layout.VBox;


public class Controller {
    private static PlayerHandler playerHandler;
    @FXML
    public void initialize() {
        score = 0;
        scoreLabel.setText("0");
    }

    @FXML
    private Label gameOverMessage;

    @FXML
    private Label finalScoreLabel;

    public void setScore(int score) {
        finalScoreLabel.setText("Score: " + score);
    }

    public void setGameOverMessage(String message) {
        gameOverMessage.setText(message);
    }

    @FXML
    private Button backButton;


    @FXML
    private Label scoreLabel;

    @FXML
    private GridPane gameBoard;


    public static Game game;


    int[] position = new int[3];

    private static int score = 0;

    // Create an array of buttons to keep track of the button references
    private static Button[][] buttons;

    public void setPlayerHandler(PlayerHandler playerHandler) {
        this.playerHandler = playerHandler;
    }

    public void createGameBoard() {
        gameBoard.getChildren().clear();
        buttons = new Button[game.row][game.col]; // Initialize the button array

        for (int row = 0; row < game.row; row++) {
            for (int col = 0; col < game.col; col++) {
                Button button = new Button();
                button.setPrefSize(40, 40);
                ImageView imageView = addContent(game.board[row][col]);
                imageView.setFitWidth(30);
                imageView.setFitHeight(30);
                imageView.setPreserveRatio(true);
                button.setGraphic(imageView);
                buttons[row][col] = button; // Store the button reference
                int finalRow = row;
                int finalCol = col;
                button.setOnAction( _ -> handleButtonPress(finalRow, finalCol));
                gameBoard.add(button, col, row);
            }
        }
    }

    private void handleButtonPress(int row, int col) {

        if (game.board[row][col] == 0) {
            System.out.println("Ignored click on empty cell at: " + row + ", " + col);
            return; // Exit the method without doing anything
        }

        if(position[0] == 1 && position[1]== row && position[2] == col) {
            System.out.println("Ignored click on same cell at: " + row + ", " + col);
            return;
        }
        // Send the button press information to the server
        String message = "PRESS:" + row + "," + col;
        playerHandler.sendMessage(message);
        System.out.println("Button pressed at: " + row + ", " + col);
        if (position[0] == 0) {
            position[1] = row;
            position[2] = col;
            position[0] = 1;
        } else {

            // Check if the eating condition is met
            boolean change = game.judge(position[1], position[2], row, col);
            System.out.println(change);
            position[0] = 0;

            if (!game.hasAvailableMoves()){
                onGameOver();
            }

            if (change) {

                message = "REMOVE:" + row + "," + col + ","+ position[1]+ "," + position[2];
                playerHandler.sendMessage(message);
                playerHandler.sendMessage(message);

//                updateBoard(position[1], position[2], row, col);

            }

        }
    }

    //METHOD TO UPDATE THE BOARD
// Update the board if a valid move is made and items are eaten
    public void updateBoard(int row1, int col1, int row2, int col2) {
        game.board[row1][col1] = 0;
        game.board[row2][col2] = 0;

        ImageView firstReplacementImageView = new ImageView(imageReplacement);
        firstReplacementImageView.setFitWidth(30);
        firstReplacementImageView.setFitHeight(30);
        firstReplacementImageView.setPreserveRatio(true);

        ImageView secondReplacementImageView = new ImageView(imageReplacement);
        secondReplacementImageView.setFitWidth(30);
        secondReplacementImageView.setFitHeight(30);
        secondReplacementImageView.setPreserveRatio(true);

        buttons[row1][col1].setGraphic(firstReplacementImageView);
        buttons[row2][col2].setGraphic(secondReplacementImageView);

        score += 10;
//
//        // Check if scoreLabel is initialized
//        if (scoreLabel != null) {
//            scoreLabel.setText(String.valueOf(score));
//        } else {
//            System.out.println("scoreLabel is null");
//        }
    }


    public static Image imageReplacement = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/carambola.png")).toExternalForm());

    private void onGameOver() {
        System.out.println("Game Over! No moves available.");
        System.out.println(score);

        try {
            // Load the GameOver.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("gameOver.fxml"));
            Parent gameOverRoot = loader.load();
            Controller gameOverController = loader.getController();
            gameOverController.setScore(score);
            gameOverController.setGameOverMessage("You Lose");
            // Load the GameOver scene
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(gameOverRoot));
            stage.setTitle("Game Over!");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReset() {
        System.out.println("game is reset");
//        game = new Game(Game.SetupBoard(game.row, game.col));
        createGameBoard();
        position[0] = 0;
        score = 0;
        scoreLabel.setText("0");
    }

    public void handleBack() {
        try {
            // Load the StartScreen.fxml
            FXMLLoader loader = new FXMLLoader(getClass().getResource("StartScreen.fxml"));
            Parent startScreenRoot = loader.load();

            // Cast the root node to VBox to set the background
            VBox root = (VBox) startScreenRoot;

            // Load and set the background image
            BackgroundImage backgroundImage = new BackgroundImage(
                    new Image(getClass().getResource("/org/example/demo/bg.png").toExternalForm()),
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundRepeat.NO_REPEAT,
                    BackgroundPosition.CENTER,
                    new BackgroundSize(BackgroundSize.AUTO, BackgroundSize.AUTO, false, false, true, true)
            );

            root.setBackground(new Background(backgroundImage));

            // Get the current stage and set the new scene
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root, 400, 400)); // Set scene size as needed
            stage.setTitle("Select Board Size");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public ImageView addContent(int content){
        return switch (content) {
            case 0 -> new ImageView(imageCarambola);
            case 1 -> new ImageView(imageApple);
            case 2 -> new ImageView(imageMango);
            case 3 -> new ImageView(imageBlueberry);
            case 4 -> new ImageView(imageCherry);
            case 5 -> new ImageView(imageGrape);
            case 6 -> new ImageView(imageKiwi);
            case 7 -> new ImageView(imageOrange);
            case 8 -> new ImageView(imagePeach);
            case 9 -> new ImageView(imagePear);
            case 10 -> new ImageView(imagePineapple);
            case 11 -> new ImageView(imageWatermelon);
            default -> null;
        };
    }

    public static Image imageApple = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/apple.png")).toExternalForm());
    public static Image imageMango = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/mango.png")).toExternalForm());
    public static Image imageBlueberry = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/blueberry.png")).toExternalForm());
    public static Image imageCherry = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/cherry.png")).toExternalForm());
    public static Image imageGrape = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/grape.png")).toExternalForm());
    public static Image imageCarambola = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/carambola.png")).toExternalForm());
    public static Image imageKiwi = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/kiwi.png")).toExternalForm());
    public static Image imageOrange = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/orange.png")).toExternalForm());
    public static Image imagePeach = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/peach.png")).toExternalForm());
    public static Image imagePear = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/pear.png")).toExternalForm());
    public static Image imagePineapple = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/pineapple.png")).toExternalForm());
    public static Image imageWatermelon = new Image(Objects.requireNonNull(Game.class.getResource("/org/example/demo/watermelon.png")).toExternalForm());

}
