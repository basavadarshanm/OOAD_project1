package com.onlinebanking.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import com.onlinebanking.config.ApplicationContext;
import com.onlinebanking.model.Account;
import com.onlinebanking.model.Transaction;
import com.onlinebanking.model.User;
import com.onlinebanking.service.AccountService;
import com.onlinebanking.service.AuthService;
import com.onlinebanking.service.ReceiptService;
import com.onlinebanking.service.TransferService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class TransferMoneyController {
    private User currentUser;
    private Account currentAccount;

    private final AccountService accountService;
    private final TransferService transferService;
    private final ReceiptService receiptService;
    private final AuthService authService;

    @FXML private Label balanceLabel;
    @FXML private TextField toAccountField;
    @FXML private TextField amountField;
    @FXML private TextField descriptionField;
    @FXML private TextArea receiptArea;
    @FXML private Label messageLabel;

    public TransferMoneyController(AccountService accountService, TransferService transferService, ReceiptService receiptService,
                                   AuthService authService) {
        this.accountService = accountService;
        this.transferService = transferService;
        this.receiptService = receiptService;
        this.authService = authService;
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
                if (!verifyTransferMpin()) { return; }

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

    private boolean verifyTransferMpin() {
        if (!authService.hasMpin(currentUser.getId())) {
            showError("Set your 4-digit MPIN from User Details before transferring money.");
            return false;
        }

        Optional<String> mpinInput = promptMaskedMpin();
        if (mpinInput.isEmpty()) {
            showError("Transfer cancelled: MPIN verification is required.");
            return false;
        }

        if (!authService.verifyMpin(currentUser.getId(), mpinInput.get())) {
            showError("Invalid MPIN. Transfer blocked.");
            return false;
        }
        return true;
    }

    private Optional<String> promptMaskedMpin() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("MPIN Verification");
        dialog.setHeaderText("Enter your 4-digit MPIN to continue");

        ButtonType verifyButtonType = new ButtonType("Verify", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(verifyButtonType, ButtonType.CANCEL);

        PasswordField mpinField = new PasswordField();
        mpinField.setPromptText("MPIN (4 digits)");
        mpinField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (!newValue.matches("\\d{0,4}")) {
                mpinField.setText(oldValue);
            }
        });

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("MPIN:"), 0, 0);
        grid.add(mpinField, 1, 0);

        dialog.getDialogPane().setContent(grid);

        javafx.scene.Node verifyButton = dialog.getDialogPane().lookupButton(verifyButtonType);
        verifyButton.setDisable(true);
        mpinField.textProperty().addListener((obs, oldValue, newValue) -> verifyButton.setDisable(newValue.length() != 4));

        dialog.setResultConverter(button -> button == verifyButtonType ? mpinField.getText() : null);
        return dialog.showAndWait();
    }
}
