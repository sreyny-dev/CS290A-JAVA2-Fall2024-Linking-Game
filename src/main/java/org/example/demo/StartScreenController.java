package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class StartScreenController {

    @FXML
    private ImageView image4x4;

    @FXML
    private ImageView image6x8;

    @FXML
    private ImageView image8x8;

    @FXML
    public void initialize() {
        // Set images for each ImageView
        image4x4.setImage(new Image(getClass().getResourceAsStream("/org/example/demo/button.png")));
        image6x8.setImage(new Image(getClass().getResourceAsStream("/org/example/demo/button.png")));
        image8x8.setImage(new Image(getClass().getResourceAsStream("/org/example/demo/button.png")));
    }

    @FXML
    private void handleBoard4x4() throws IOException {
        Application.showWaitingScreen(4,4);
//        Application.showGameScreen(4, 4);
    }

    @FXML
    private void handleBoard6x8() throws IOException {
        Application.showWaitingScreen(6,8);
    }

    @FXML
    private void handleBoard8x8() throws IOException {
        Application.showWaitingScreen(8,8);
    }



}
