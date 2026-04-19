package com.onlinebanking.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.onlinebanking.config.ApplicationContext;
import com.onlinebanking.dto.UserManagementDto;
import com.onlinebanking.model.Account;
import com.onlinebanking.model.Transaction;
import com.onlinebanking.model.User;
import com.onlinebanking.service.AccountService;
import com.onlinebanking.service.BeneficiaryService;
import com.onlinebanking.service.BillPayService;
import com.onlinebanking.service.ManagerService;
import com.onlinebanking.service.ReceiptService;
import com.onlinebanking.service.TransferService;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class DashboardController {
    private final AccountService accountService;
    private final TransferService transferService;
    private final BeneficiaryService beneficiaryService;
    private final BillPayService billPayService;
    private final ManagerService managerService;
    private final ReceiptService receiptService;

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
    @FXML
    private Label actionMessageLabel;
    @FXML
    private Button transferButton;
    @FXML
    private Button billPayButton;
    @FXML
    private Button createAccountButton;
    @FXML
    private Button userDetailsButton;
    @FXML
    private Button addMoneyButton;
    @FXML
    private Button manageUsersButton;

    public DashboardController(AccountService accountService, TransferService transferService,
                               BeneficiaryService beneficiaryService, BillPayService billPayService,
                               ManagerService managerService, ReceiptService receiptService) {
        this.accountService = accountService;
        this.transferService = transferService;
        this.beneficiaryService = beneficiaryService;
        this.billPayService = billPayService;
        this.managerService = managerService;
        this.receiptService = receiptService;
    }

    public void setCurrentUser(User user) {
        // MVC Pattern: controller receives authenticated user context and coordinates view state.
        this.currentUser = user;
        welcomeLabel.setText("Welcome, " + user.getUsername());
        actionMessageLabel.setText("");

        if ("MANAGER".equalsIgnoreCase(user.getRole())) {
            enterAdminMode();
            return;
        }

        loadFirstAccount();
    }

    public void setTransientMessage(String message) {
        if (message == null) {
            actionMessageLabel.setText("");
            return;
        }
        actionMessageLabel.setStyle("-fx-text-fill: #1f7a45;");
        actionMessageLabel.setText(message);
    }

    private void loadFirstAccount() {
        List<Account> accounts = accountService.getAccounts(currentUser.getId());
        if (!accounts.isEmpty()) {
            currentAccount = accounts.get(0);
            accountLabel.setText("Account: " + currentAccount.getAccountNumber());
            balanceLabel.setText("Balance: Rs. " + String.format("%.2f", currentAccount.getBalance()));
            loadTransactions();
            transferButton.setDisable(false);
            billPayButton.setDisable(false);
            addMoneyButton.setDisable(false);
            createAccountButton.setDisable(true);
            createAccountButton.setManaged(false);
            createAccountButton.setVisible(false);
            userDetailsButton.setDisable(false);
            userDetailsButton.setManaged(true);
            userDetailsButton.setVisible(true);
            manageUsersButton.setDisable(true);
            manageUsersButton.setManaged(false);
            manageUsersButton.setVisible(false);
            return;
        }

        currentAccount = null;
        accountLabel.setText("Account: Not available");
        balanceLabel.setText("Balance: N/A");
        transactionsList.setItems(FXCollections.observableArrayList());
        transferButton.setDisable(true);
        billPayButton.setDisable(true);
        addMoneyButton.setDisable(true);
        createAccountButton.setDisable(false);
        createAccountButton.setManaged(true);
        createAccountButton.setVisible(true);
        userDetailsButton.setDisable(true);
        userDetailsButton.setManaged(false);
        userDetailsButton.setVisible(false);
        manageUsersButton.setDisable(true);
        manageUsersButton.setManaged(false);
        manageUsersButton.setVisible(false);
    }

    private void loadTransactions() {
        List<Transaction> txs = accountService.getRecentTransactions(currentAccount.getId());
        transactionsList.setItems(FXCollections.observableArrayList(txs));
    }

    private void enterAdminMode() {
        currentAccount = null;
        accountLabel.setText("Admin Mode: Global transaction monitor");
        balanceLabel.setText("Balance: N/A (Manager)");
        transactionsList.setItems(FXCollections.observableArrayList(managerService.getAllTransactions()));

        transferButton.setDisable(true);
        billPayButton.setDisable(true);
        addMoneyButton.setDisable(true);
        createAccountButton.setDisable(true);
        createAccountButton.setManaged(false);
        createAccountButton.setVisible(false);
        userDetailsButton.setDisable(false);
        userDetailsButton.setManaged(true);
        userDetailsButton.setVisible(true);
        manageUsersButton.setDisable(false);
        manageUsersButton.setManaged(true);
        manageUsersButton.setVisible(true);

        actionMessageLabel.setStyle("-fx-text-fill: #2b6a9a;");
        actionMessageLabel.setText("Manager account: Create/Transfer/Bill Pay are disabled. Showing all transactions.");
    }

    @FXML
    protected void handleTransfer(ActionEvent event) throws IOException {
        if (!isLoggedIn()) {
            return;
        }
        if (!hasUsableAccount()) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/transfer.fxml"));
        loader.setControllerFactory(ApplicationContext.getInstance().getControllerFactory());
        Scene scene = new Scene(loader.load(), 980, 700);
        TransferMoneyController controller = loader.getController();
        controller.setCurrentUser(currentUser, currentAccount);
        Stage window = (Stage) welcomeLabel.getScene().getWindow();
        window.setScene(scene);
        window.setTitle("Transfer Money");
    }

    @FXML
    protected void handleBillPay(ActionEvent event) throws IOException {
        if (!isLoggedIn()) {
            return;
        }
        if (!hasUsableAccount()) {
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/billpay.fxml"));
        loader.setControllerFactory(ApplicationContext.getInstance().getControllerFactory());
        Scene scene = new Scene(loader.load(), 980, 700);
        PayBillsController controller = loader.getController();
        controller.setCurrentUser(currentUser, currentAccount);
        Stage window = (Stage) welcomeLabel.getScene().getWindow();
        window.setScene(scene);
        window.setTitle("Pay Bills");
    }

    @FXML
    protected void handleLogout(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
        loader.setControllerFactory(ApplicationContext.getInstance().getControllerFactory());
        Scene scene = new Scene(loader.load(), 480, 320);
        scene.getStylesheets().add(getClass().getResource("/styles/app.css").toExternalForm());
        Stage window = (Stage) welcomeLabel.getScene().getWindow();
        window.setScene(scene);
        window.setTitle("Online Banking - Login");
    }

    @FXML
    protected void handleRefresh(ActionEvent event) {
        if (!isLoggedIn()) {
            return;
        }
        if ("MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            enterAdminMode();
            return;
        }
        loadFirstAccount();
    }

    @FXML
    protected void handleCreateAccount(ActionEvent event) throws IOException {
        if (!isLoggedIn()) {
            return;
        }
        if ("MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            showError("Action not allowed", "Manager/admin users cannot create customer accounts from this screen.");
            return;
        }

        if (currentAccount != null) {
            showInfo("Account already exists", "This user already has an active account.");
            return;
        }

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/account_creation.fxml"));
        loader.setControllerFactory(ApplicationContext.getInstance().getControllerFactory());
        Scene scene = new Scene(loader.load(), 980, 700);
        AccountCreationController controller = loader.getController();
        controller.setCurrentUser(currentUser);
        Stage window = (Stage) welcomeLabel.getScene().getWindow();
        window.setScene(scene);
        window.setTitle("Create Account");
    }

    @FXML
    protected void handleAddMoney(ActionEvent event) {
        if (!isLoggedIn()) {
            return;
        }
        if ("MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            showError("Action not allowed", "Manager/admin users cannot deposit from this screen.");
            return;
        }

        if (!hasUsableAccount()) {
            return;
        }

        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog("1000.00");
        dialog.setTitle("Add Money");
        dialog.setHeaderText("Deposit amount into your account");
        dialog.setContentText("Amount (Rs.):");

        Optional<String> amountInput = dialog.showAndWait();
        if (amountInput.isEmpty()) {
            return;
        }

        try {
            BigDecimal amount = new BigDecimal(amountInput.get().trim());
            currentAccount = accountService.addMoney(currentAccount, amount);
            loadFirstAccount();

                List<Transaction> recent = accountService.getRecentTransactions(currentAccount.getId());
                Optional<Transaction> depositTx = recent.stream()
                    .filter(tx -> "DEPOSIT".equalsIgnoreCase(tx.getTransactionType()))
                    .findFirst();

                String receipt = depositTx
                    .map(tx -> receiptService.generateTransactionReceipt(tx, currentAccount.getAccountNumber(), null))
                    .orElse("Rs. " + amount + " has been added.");
                showInfo("Deposit successful", receipt);
        } catch (NumberFormatException ex) {
            showError("Invalid amount", "Enter a valid number such as 1000 or 1000.50.");
        } catch (Exception ex) {
            showError("Unable to add money", ex.getMessage());
        }
    }

    @FXML
    protected void handleUserDetails(ActionEvent event) {
        if (!isLoggedIn()) {
            return;
        }
        if ("MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            String details = "Username: " + currentUser.getUsername() + "\n"
                    + "Role: " + currentUser.getRole() + "\n"
                    + "Access: Admin transaction view";

            Alert alert = new Alert(Alert.AlertType.INFORMATION, details, ButtonType.OK);
            alert.setTitle("User Details");
            alert.setHeaderText("Admin Profile");
            alert.showAndWait();
            return;
        }

        if (!hasUsableAccount()) {
            return;
        }

        String phone = accountService.getPhoneNumber(currentAccount.getAccountNumber());
        String details = "Username: " + currentUser.getUsername() + "\n"
                + "Role: " + currentUser.getRole() + "\n"
                + "Account ID: " + currentAccount.getAccountNumber() + "\n"
                + "Phone: " + phone + "\n"
                + "Balance: Rs. " + String.format("%.2f", currentAccount.getBalance());

        Alert alert = new Alert(Alert.AlertType.INFORMATION, details, ButtonType.OK);
        alert.setTitle("User Details");
        alert.setHeaderText("Account Profile");
        alert.showAndWait();
    }

    @FXML
    protected void handleManageUsers(ActionEvent event) {
        if (!isLoggedIn()) {
            return;
        }
        if (!"MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            showError("Action not allowed", "Only manager users can manage users.");
            return;
        }

        List<UserManagementDto> users = managerService.getAllUserSummaries();
        String table = users.stream()
                .map(u -> u.id() + " | " + u.username() + " | " + u.role() + " | blocked=" + u.blocked())
                .collect(Collectors.joining("\n"));

        TextInputDialog actionDialog = new TextInputDialog("VIEW");
        actionDialog.setTitle("Manage Users");
        actionDialog.setHeaderText("Enter action: VIEW, DELETE:<id>, UPDATE_ROLE:<id>:<CUSTOMER|MANAGER>");
        actionDialog.setContentText("Action:");

        Optional<String> actionInput = actionDialog.showAndWait();
        if (actionInput.isEmpty()) {
            return;
        }

        String action = actionInput.get().trim();
        try {
            if ("VIEW".equalsIgnoreCase(action)) {
                showInfo("Users", table.isBlank() ? "No users found." : table);
                return;
            }

            if (action.toUpperCase().startsWith("DELETE:")) {
                long userId = Long.parseLong(action.substring("DELETE:".length()).trim());
                managerService.deleteUser(userId);
                showInfo("Manage Users", "User deleted: " + userId);
                enterAdminMode();
                return;
            }

            if (action.toUpperCase().startsWith("UPDATE_ROLE:")) {
                String[] parts = action.split(":");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Use UPDATE_ROLE:<id>:<CUSTOMER|MANAGER>");
                }
                long userId = Long.parseLong(parts[1].trim());
                String role = parts[2].trim();
                managerService.updateUserRole(userId, role);
                showInfo("Manage Users", "User role updated for id " + userId + " to " + role.toUpperCase());
                enterAdminMode();
                return;
            }

            throw new IllegalArgumentException("Unknown action. Use VIEW, DELETE:<id>, or UPDATE_ROLE:<id>:<role>");
        } catch (Exception ex) {
            showError("Manage Users failed", ex.getMessage());
        }
    }

    private boolean isLoggedIn() {
        if (currentUser != null) {
            return true;
        }
        showError("Login required", "Please log in again to continue.");
        return false;
    }

    private boolean hasUsableAccount() {
        if (currentAccount != null) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.WARNING, "No account found for this user. Create one first from the Dashboard.", ButtonType.OK);
        alert.setTitle("Account Not Available");
        alert.setHeaderText("Transactions are unavailable");
        alert.showAndWait();
        return false;
    }

    private void showInfo(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        alert.setTitle("Online Banking");
        alert.setHeaderText(header);
        alert.showAndWait();
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR, content, ButtonType.OK);
        alert.setTitle("Online Banking");
        alert.setHeaderText(header);
        alert.showAndWait();
    }
}
