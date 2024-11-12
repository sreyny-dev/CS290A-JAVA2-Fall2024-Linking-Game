package org.example.demo;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class SignUpController {
    @FXML
    private TextField usernameReg;
    @FXML
    private PasswordField passwordReg;
    @FXML
    private PasswordField confirmPassword;
    @FXML
    private Label registerMessage;

    private static final String dataPath = "D:\\SUSTech\\Year4-Sem1\\cs209a-java2\\Assignment\\2024FallCS209A-A2Demo-main\\src\\main\\java\\org\\example\\demo\\users.txt";


    @FXML
    public void handleRegister() {
        String usernameText = usernameReg.getText();
        String passwordText = passwordReg.getText();
        String confirmPasswordText = confirmPassword.getText();

        if (usernameText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
            registerMessage.setText("Please fill in all fields.");
            return;
        }

        if (!passwordText.equals(confirmPasswordText)) {
            registerMessage.setText("Passwords do not match.");
            return;
        }

        saveUserToFile(usernameText, passwordText);
        registerMessage.setText("Registration successful! Returning to login...");

        // Delay for user to see message and then go back to login
        new Thread(() -> {
            try {
                Thread.sleep(2000);
                goToLoginScreen();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void goToLoginScreen() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("login.fxml"));
                Parent loginRoot = loader.load();
                Stage stage = (Stage) usernameReg.getScene().getWindow();
                stage.setScene(new Scene(loginRoot));
                stage.setTitle("Login");
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private void saveUserToFile(String username, String password) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(dataPath, true))) {
            writer.write(username + "," + password);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
            registerMessage.setText("Error saving user information.");
        }
    }
}
