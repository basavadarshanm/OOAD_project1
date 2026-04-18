package com.onlinebanking.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;

import com.onlinebanking.config.ApplicationContext;
import com.onlinebanking.model.Account;
import com.onlinebanking.model.BillPayment;
import com.onlinebanking.model.User;
import com.onlinebanking.service.BillPayService;
import com.onlinebanking.service.ReceiptService;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class PayBillsController {
    private User currentUser;
    private Account currentAccount;

    private final BillPayService billPayService;
    private final ReceiptService receiptService;

    @FXML private Label balanceLabel;
    @FXML private TextField billerField;
    @FXML private TextField billRefField;
    @FXML private TextField amountField;
    @FXML private DatePicker dueDate;
    @FXML private TextArea receiptArea;
    @FXML private Label messageLabel;

    public PayBillsController(BillPayService billPayService, ReceiptService receiptService) {
        this.billPayService = billPayService;
        this.receiptService = receiptService;
    }

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
    protected void handlePayBill(ActionEvent event) {
        try {
            if (currentAccount == null) {
                showError("No account available. Please return to dashboard and refresh.");
                return;
            }

            String biller = billerField.getText().trim();
            String billRef = billRefField.getText().trim();
            BigDecimal amount = new BigDecimal(amountField.getText().trim());
            LocalDate due = dueDate.getValue();

            if (biller.isEmpty() || billRef.isEmpty() || due == null) { showError("Fill all fields"); return; }
            if (amount.signum() <= 0) { showError("Amount must be positive"); return; }

            BillPayment payment = billPayService.payBill(currentAccount.getAccountNumber(), biller, amount, billRef, due);
            BigDecimal newBalance = currentAccount.getBalance().subtract(amount);
                currentAccount = new Account(
                    currentAccount.getId(),
                    currentAccount.getUserId(),
                    currentAccount.getAccountNumber(),
                    newBalance
                );
            balanceLabel.setText("Balance: Rs. " + String.format("%.2f", newBalance));
            
            showSuccess("Bill paid successfully!");
            receiptArea.setText(receiptService.generateBillPaymentReceipt(biller, billRef, currentAccount.getAccountNumber(), amount, String.valueOf(payment.getId())));
            
            billerField.clear();
            billRefField.clear();
            amountField.clear();
            dueDate.setValue(null);
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
