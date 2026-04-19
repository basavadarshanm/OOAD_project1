package com.onlinebanking.controller;

import java.io.IOException;
import java.util.Optional;

import com.onlinebanking.config.ApplicationContext;
import com.onlinebanking.model.User;
import com.onlinebanking.service.AuthService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    private final AuthService authService;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label infoLabel;

    public LoginController(AuthService authService) {
        this.authService = authService;
    }

    @FXML
    protected void handleLogin(ActionEvent event) throws IOException {
        clearMessages();
        String username = usernameField.getText();
        String password = passwordField.getText();
        Optional<User> userOpt = authService.login(username, password);
        if (userOpt.isEmpty()) {
            errorLabel.setText("Invalid credentials");
            return;
        }
        navigateToDashboard(userOpt.get());
    }

    @FXML
    protected void handleShowRegister(ActionEvent event) throws IOException {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/register.fxml"));
        loader.setControllerFactory(ApplicationContext.getInstance().getControllerFactory());
        Scene scene = new Scene(loader.load(), 480, 360);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Online Banking - Register");
    }

    private void navigateToDashboard(User user) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        loader.setControllerFactory(ApplicationContext.getInstance().getControllerFactory());
        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        DashboardController controller = loader.getController();
        controller.setCurrentUser(user);

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Online Banking - Dashboard");
    }

    public void setInfoMessage(String message) {
        clearMessages();
        infoLabel.setText(message);
    }

    private void clearMessages() {
        errorLabel.setText("");
        infoLabel.setText("");
    }
}
