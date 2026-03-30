package com.onlinebanking.controller;

import java.math.BigDecimal;
import java.util.List;

import com.onlinebanking.model.Account;
import com.onlinebanking.model.Beneficiary;
import com.onlinebanking.model.Transaction;
import com.onlinebanking.model.User;
import com.onlinebanking.service.AccountService;
import com.onlinebanking.service.BeneficiaryService;
import com.onlinebanking.service.BillPayService;
import com.onlinebanking.service.TransferService;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class DashboardController {
    private final AccountService accountService;
    private final TransferService transferService;
    private final BeneficiaryService beneficiaryService;
    private final BillPayService billPayService;

    private User currentUser;

    @FXML
    private Label welcomeLabel;
    @FXML
    private ListView<Account> accountsList;
    @FXML
    private ListView<Transaction> transactionsList;
    @FXML
    private Label statusLabel;

    @FXML
    private TextField transferToField;
    @FXML
    private TextField transferAmountField;

    @FXML
    private TextField billerField;
    @FXML
    private TextField billAmountField;

    @FXML
    private TextField beneficiaryNameField;
    @FXML
    private TextField beneficiaryAccountField;
    @FXML
    private TextField beneficiaryBankField;

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
        loadAccounts();
    }

    @FXML
    protected void handleRefresh() {
        loadAccounts();
    }

    @FXML
    protected void handleTransfer() {
        Account selected = accountsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an account first");
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(transferAmountField.getText());
            transferService.transfer(selected.getAccountNumber(), transferToField.getText(), amount);
            statusLabel.setText("Transfer successful");
            loadAccounts();
        } catch (Exception e) {
            statusLabel.setText("Transfer failed: " + e.getMessage());
        }
    }

    @FXML
    protected void handleBillPay() {
        Account selected = accountsList.getSelectionModel().getSelectedItem();
        if (selected == null) {
            statusLabel.setText("Select an account first");
            return;
        }
        try {
            BigDecimal amount = new BigDecimal(billAmountField.getText());
            billPayService.payBill(selected.getAccountNumber(), amount, billerField.getText());
            statusLabel.setText("Bill paid");
            loadAccounts();
        } catch (Exception e) {
            statusLabel.setText("Bill pay failed: " + e.getMessage());
        }
    }

    @FXML
    protected void handleAddBeneficiary() {
        try {
            Beneficiary beneficiary = new Beneficiary(0, currentUser.getId(), beneficiaryNameField.getText(),
                    beneficiaryAccountField.getText(), beneficiaryBankField.getText());
            beneficiaryService.add(beneficiary);
            statusLabel.setText("Beneficiary added");
        } catch (Exception e) {
            statusLabel.setText("Add failed: " + e.getMessage());
        }
    }

    private void loadAccounts() {
        List<Account> accounts = accountService.getAccounts(currentUser.getId());
        accountsList.setItems(FXCollections.observableArrayList(accounts));
        accountsList.refresh();
        if (!accounts.isEmpty()) {
            loadTransactions(accounts.get(0).getId());
        }
    }

    @FXML
    protected void handleAccountSelection() {
        Account selected = accountsList.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadTransactions(selected.getId());
        }
    }

    private void loadTransactions(long accountId) {
        List<Transaction> txs = accountService.getRecentTransactions(accountId);
        transactionsList.setItems(FXCollections.observableArrayList(txs));
        transactionsList.refresh();
    }
}
