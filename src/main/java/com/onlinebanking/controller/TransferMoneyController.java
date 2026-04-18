package com.onlinebanking.controller;

import java.io.IOException;
import java.math.BigDecimal;

import com.onlinebanking.config.ApplicationContext;
import com.onlinebanking.model.Account;
import com.onlinebanking.model.Transaction;
import com.onlinebanking.model.User;
import com.onlinebanking.service.AccountService;
import com.onlinebanking.service.ReceiptService;
import com.onlinebanking.service.TransferService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class TransferMoneyController {
    private User currentUser;
    private Account currentAccount;

    private final AccountService accountService;
    private final TransferService transferService;
    private final ReceiptService receiptService;

    @FXML private Label balanceLabel;
    @FXML private TextField toAccountField;
    @FXML private TextField amountField;
    @FXML private TextField descriptionField;
    @FXML private TextArea receiptArea;
    @FXML private Label messageLabel;

    public TransferMoneyController(AccountService accountService, TransferService transferService, ReceiptService receiptService) {
        this.accountService = accountService;
        this.transferService = transferService;
        this.receiptService = receiptService;
    }

    @FXML
    public void initialize() {}

    public void setCurrentUser(User user, Account account) {
        this.currentUser = user;
        this.currentAccount = account;
        if (currentAccount == null) {
            balanceLabel.setText("Balance: N/A");
            showError("No account is available for this user.");
            return;
        }
        balanceLabel.setText("Balance: Rs. " + String.format("%.2f", currentAccount.getBalance()));
    }

    @FXML
    protected void handleTransfer(ActionEvent event) {
        try {
            if (currentAccount == null) {
                showError("No account available. Please return to dashboard and refresh.");
                return;
            }

            String toAccount = toAccountField.getText().trim();
            String description = descriptionField.getText() == null ? "" : descriptionField.getText().trim();
            BigDecimal amount = new BigDecimal(amountField.getText().trim());

            if (toAccount.isEmpty()) { showError("Enter recipient account ID or phone number"); return; }
            if (!toAccount.matches("\\d{8}|\\d{10}")) {
                showError("Recipient must be 8-digit account ID or 10-digit phone number");
                return;
            }
            if (amount.signum() <= 0) { showError("Amount must be positive"); return; }

            Transaction tx = transferService.transfer(
                    currentAccount.getAccountNumber(),
                    toAccount,
                    amount,
                    description.isEmpty() ? "Transfer" : description
            );
                BigDecimal newBalance = currentAccount.getBalance().subtract(amount);
                currentAccount = new Account(
                    currentAccount.getId(),
                    currentAccount.getUserId(),
                    currentAccount.getAccountNumber(),
                    newBalance
                );
                balanceLabel.setText("Balance: Rs. " + String.format("%.2f", newBalance));
            
            showSuccess("Transfer successful!");
            receiptArea.setText(receiptService.generateTransactionReceipt(tx, currentAccount.getAccountNumber(), toAccount));
            
            toAccountField.clear();
            amountField.clear();
            descriptionField.clear();
        } catch (Exception e) {
            showError("Error: " + e.getMessage());
        }
    }

    @FXML
    protected void handleBack(ActionEvent event) throws IOException {
        goToDashboard();
    }

    private void goToDashboard() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
        loader.setControllerFactory(ApplicationContext.getInstance().getControllerFactory());
        Scene scene = new Scene(loader.load(), 980, 700);
        DashboardController controller = loader.getController();
        controller.setCurrentUser(currentUser);
        Stage stage = (Stage) balanceLabel.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Dashboard");
    }

    private void showSuccess(String msg) { messageLabel.setStyle("-fx-text-fill: green;"); messageLabel.setText(msg); }
    private void showError(String msg) { messageLabel.setStyle("-fx-text-fill: red;"); messageLabel.setText(msg); }
}
