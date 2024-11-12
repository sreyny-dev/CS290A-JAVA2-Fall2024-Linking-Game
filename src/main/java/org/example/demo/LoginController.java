package org.example.demo;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class LoginController {

    @FXML
    private TextField username;
    @FXML
    private PasswordField password;
    @FXML
    private Label loginMessage;

    private PlayerHandler playerHandler;



    private final Map<String, String> users = new HashMap<>();
    private static final String userFile = "D:\\SUSTech\\Year4-Sem1\\cs209a-java2\\Assignment\\2024FallCS209A-A2Demo-main\\src\\main\\java\\org\\example\\demo\\users.txt";

    public LoginController() {
        loadUsers();
    }

    @FXML
    private void handleLogin() throws IOException {
        String usernameInput = this.username.getText();
        String passwordInput = this.password.getText();

        if(users.containsKey(usernameInput)){
            String storedPassword = users.get(usernameInput);
            if(storedPassword!= null && storedPassword.equals(passwordInput)){
                Application app = new Application();
                connectToServer(usernameInput);
                app.showStartScreen();
            }else{
                loginMessage.setText("Invalid Password");
            }
        }else{
            loginMessage.setText("Invalid Username or Password");
        }
    }

    private void connectToServer(String username) {
        try {
            Socket socket = new Socket("localhost", 4444); // Ensure the server is running
            PlayerHandler playerHandler = new PlayerHandler(socket);
            new Thread(playerHandler).start(); // Start the thread to listen for messages

            // Send the username to the server
            playerHandler.sendMessage(username);

            Application.setPlayerHandler(playerHandler); // Set the player handler

        } catch (IOException e) {
            e.printStackTrace();
            loginMessage.setText("Could not connect to server.");
        }
    }


    private void loadUsers() {
        try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    String username = parts[0].trim();
                    String password = parts[1].trim();
                    users.put(username, password);
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File not found: " + userFile);
        } catch (IOException e) {
            System.err.println("Error reading file: " + userFile);
            e.printStackTrace();
        }
    }

}

