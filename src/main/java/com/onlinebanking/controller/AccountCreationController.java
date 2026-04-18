package com.onlinebanking.controller;

import java.io.IOException;
import java.math.BigDecimal;

import com.onlinebanking.config.ApplicationContext;
import com.onlinebanking.model.Account;
import com.onlinebanking.model.User;
import com.onlinebanking.service.AccountService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AccountCreationController {
    private final AccountService accountService;

    private User currentUser;

    @FXML private TextField usernameField;
    @FXML private TextField accountIdField;
    @FXML private TextField phoneField;
    @FXML private TextField initialDepositField;
    @FXML private Label messageLabel;

    public AccountCreationController(AccountService accountService) {
        this.accountService = accountService;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        usernameField.setText(user.getUsername());
        accountIdField.setText(accountService.generateSuggestedAccountNumber());
        messageLabel.setText("");
    }

    @FXML
    protected void handleCreateAccount(ActionEvent event) {
        try {
            String phone = phoneField.getText() == null ? "" : phoneField.getText().trim();
            String accountId = accountIdField.getText() == null ? "" : accountIdField.getText().trim();
            String depositText = initialDepositField.getText() == null ? "0" : initialDepositField.getText().trim();
            BigDecimal initialDeposit = new BigDecimal(depositText);

            Account created = accountService.createAccount(currentUser.getId(), phone, initialDeposit, accountId);
            goToDashboard("Account created: " + created.getAccountNumber());
        } catch (NumberFormatException ex) {
            showError("Initial deposit must be a valid number.");
        } catch (Exception ex) {
            showError(ex.getMessage());
        }
    }

    @FXML
    protected void handleBack(ActionEvent event) throws IOException {
        goToDashboard(null);
    }

    private void goToDashboard(String infoMessage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        loader.setControllerFactory(ApplicationContext.getInstance().getControllerFactory());
        Scene scene = new Scene(loader.load(), 980, 700);
        DashboardController controller = loader.getController();
        controller.setCurrentUser(currentUser);
        if (infoMessage != null && !infoMessage.isBlank()) {
            controller.setTransientMessage(infoMessage);
        }

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Dashboard");
    }

    private void showError(String msg) {
        messageLabel.setStyle("-fx-text-fill: #d64541;");
        messageLabel.setText(msg);
    }
}
