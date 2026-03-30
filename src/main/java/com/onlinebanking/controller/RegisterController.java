package com.onlinebanking.controller;

import java.io.IOException;

import com.onlinebanking.config.ApplicationContext;
import com.onlinebanking.service.AuthService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class RegisterController {
    private final AuthService authService;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label messageLabel;

    public RegisterController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    protected void handleRegister(ActionEvent event) throws IOException {
        messageLabel.setText("");
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirm = confirmPasswordField.getText();

        if (username == null || username.isBlank()) {
            messageLabel.setText("Username is required");
            return;
        }
        if (password == null || password.isBlank()) {
            messageLabel.setText("Password is required");
            return;
        }
        if (!password.equals(confirm)) {
            messageLabel.setText("Passwords do not match");
            return;
        }

        try {
            authService.register(username, password);
            goToLogin("Registration successful. Please log in.");
        } catch (Exception e) {
            messageLabel.setText(e.getMessage());
        }
    }

    @FXML
    protected void handleBackToLogin(ActionEvent event) throws IOException {
        goToLogin(null);
    }

    private void goToLogin(String infoMessage) throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        loader.setControllerFactory(param -> ApplicationContext.getInstance().getControllerFactory().apply(param));
        Scene scene = new Scene(loader.load(), 480, 320);
        stage.setScene(scene);
        stage.setTitle("Online Banking");
        LoginController controller = loader.getController();
        if (infoMessage != null) {
            controller.setInfoMessage(infoMessage);
        }
    }
}
