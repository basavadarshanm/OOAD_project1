package com.onlinebanking.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.onlinebanking.config.ApplicationContext;
import com.onlinebanking.model.Account;
import com.onlinebanking.model.Transaction;
import com.onlinebanking.model.User;
import com.onlinebanking.service.AccountService;
import com.onlinebanking.service.BeneficiaryService;
import com.onlinebanking.service.BillPayService;
import com.onlinebanking.service.TransferService;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class DashboardController {
    private final AccountService accountService;
    private final TransferService transferService;
    private final BeneficiaryService beneficiaryService;
    private final BillPayService billPayService;

    private User currentUser;
    private Account currentAccount;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label accountLabel;
    @FXML
    private ListView<Transaction> transactionsList;

    public DashboardController(AccountService accountService, TransferService transferService,
                               BeneficiaryService beneficiaryService, BillPayService billPayService) {
        this.accountService = accountService;
        this.transferService = transferService;
        this.beneficiaryService = beneficiaryService;
        this.billPayService = billPayService;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername());
        loadFirstAccount();
    }

    private void loadFirstAccount() {
        List<Account> accounts = accountService.getAccounts(currentUser.getId());
        if (!accounts.isEmpty()) {
            currentAccount = accounts.get(0);
            accountLabel.setText("Account: " + currentAccount.getAccountNumber());
            balanceLabel.setText("Balance: Rs. " + String.format("%.2f", currentAccount.getBalance()));
            loadTransactions();
        }
    }

    private void loadTransactions() {
        List<Transaction> txs = accountService.getRecentTransactions(currentAccount.getId());
        transactionsList.setItems(FXCollections.observableArrayList(txs));
    }

    @FXML
    protected void handleTransfer(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transfer.fxml"));
        loader.setControllerFactory(ApplicationContext.getInstance().getControllerFactory());
        Scene scene = new Scene(loader.load(), 600, 400);
        Stage window = (Stage) welcomeLabel.getScene().getWindow();
        window.setScene(scene);
        window.setTitle("Transfer Money");
    }

    @FXML
    protected void handleBillPay(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/billpay.fxml"));
        loader.setControllerFactory(ApplicationContext.getInstance().getControllerFactory());
        Scene scene = new Scene(loader.load(), 600, 400);
        Stage window = (Stage) welcomeLabel.getScene().getWindow();
        window.setScene(scene);
        window.setTitle("Pay Bills");
    }

    @FXML
    protected void handleLogout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        loader.setControllerFactory(ApplicationContext.getInstance().getControllerFactory());
        Scene scene = new Scene(loader.load(), 480, 320);
        Stage window = (Stage) welcomeLabel.getScene().getWindow();
        window.setScene(scene);
        window.setTitle("Online Banking - Login");
    }

    @FXML
    protected void handleRefresh(ActionEvent event) {
        loadFirstAccount();
    }
}
