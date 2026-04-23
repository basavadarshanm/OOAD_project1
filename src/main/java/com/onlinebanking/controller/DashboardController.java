package com.onlinebanking.controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.onlinebanking.config.ApplicationContext;
import com.onlinebanking.model.Beneficiary;
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
import com.onlinebanking.service.AuthService;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class DashboardController {
    private final AccountService accountService;
    private final TransferService transferService;
    private final BeneficiaryService beneficiaryService;
    private final BillPayService billPayService;
    private final ManagerService managerService;
    private final ReceiptService receiptService;
    private final AuthService authService;

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
    private Button addBeneficiaryButton;
    @FXML
    private Button manageUsersButton;

    public DashboardController(AccountService accountService, TransferService transferService,
                               BeneficiaryService beneficiaryService, BillPayService billPayService,
                               ManagerService managerService, ReceiptService receiptService,
                               AuthService authService) {
        this.accountService = accountService;
        this.transferService = transferService;
        this.beneficiaryService = beneficiaryService;
        this.billPayService = billPayService;
        this.managerService = managerService;
        this.receiptService = receiptService;
        this.authService = authService;
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

        promptMpinSetupIfMissing();
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
            addBeneficiaryButton.setDisable(false);
            addBeneficiaryButton.setManaged(true);
            addBeneficiaryButton.setVisible(true);
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
        addBeneficiaryButton.setDisable(true);
        addBeneficiaryButton.setManaged(false);
        addBeneficiaryButton.setVisible(false);
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
        addBeneficiaryButton.setDisable(true);
        addBeneficiaryButton.setManaged(false);
        addBeneficiaryButton.setVisible(false);
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
    protected void handleAddBeneficiary(ActionEvent event) {
        if (!isLoggedIn()) {
            return;
        }
        if ("MANAGER".equalsIgnoreCase(currentUser.getRole())) {
            showError("Action not allowed", "Manager/admin users cannot add beneficiaries from this screen.");
            return;
        }

        if (!hasUsableAccount()) {
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Beneficiary");
        dialog.setHeaderText("Enter beneficiary user ID or 10-digit phone number");
        dialog.setContentText("Identifier:");

        Optional<String> identifierInput = dialog.showAndWait();
        if (identifierInput.isEmpty()) {
            return;
        }

        try {
            Beneficiary beneficiary = beneficiaryService.addBeneficiaryForUser(currentUser.getId(), identifierInput.get());
            showInfo("Beneficiary added", "Saved " + beneficiary.getName() + " (Account: " + beneficiary.getAccountNumber() + ")");
        } catch (Exception ex) {
            showError("Unable to add beneficiary", ex.getMessage());
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

        TextInputDialog actionDialog = new TextInputDialog("VIEW");
        actionDialog.setTitle("User Actions");
        actionDialog.setHeaderText("Enter action: VIEW, CHANGE_PASSWORD, SET_MPIN");
        actionDialog.setContentText("Action:");

        Optional<String> actionInput = actionDialog.showAndWait();
        if (actionInput.isEmpty()) {
            return;
        }

        String action = actionInput.get().trim().toUpperCase();
        try {
            if ("VIEW".equals(action)) {
                showCustomerDetails();
                return;
            }
            if ("CHANGE_PASSWORD".equals(action)) {
                handleChangePassword();
                return;
            }
            if ("SET_MPIN".equals(action)) {
                handleSetOrResetMpin();
                return;
            }
            showError("Unknown action", "Use VIEW, CHANGE_PASSWORD, or SET_MPIN.");
        } catch (Exception ex) {
            showError("User action failed", ex.getMessage());
        }
    }

    private void showCustomerDetails() {
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

    private void handleChangePassword() {
        Optional<String> currentPassword = promptMaskedInput("Change Password", "Verify your current password", "Current password:");
        if (currentPassword.isEmpty()) {
            return;
        }
        Optional<String> newPassword = promptMaskedInput("Change Password", "Enter your new password", "New password:");
        if (newPassword.isEmpty()) {
            return;
        }
        Optional<String> confirmPassword = promptMaskedInput("Change Password", "Confirm your new password", "Confirm password:");
        if (confirmPassword.isEmpty()) {
            return;
        }

        if (!newPassword.get().equals(confirmPassword.get())) {
            throw new IllegalArgumentException("New password and confirm password do not match");
        }

        authService.changePassword(currentUser.getId(), currentPassword.get(), newPassword.get());
        showInfo("Password Updated", "Your password has been changed successfully.");
    }

    private void handleSetOrResetMpin() {
        Optional<String> mpin = promptMaskedInput("Set MPIN", "Create a 4-digit MPIN for transfer verification", "MPIN (4 digits):");
        if (mpin.isEmpty()) {
            return;
        }
        Optional<String> confirm = promptMaskedInput("Set MPIN", "Confirm your 4-digit MPIN", "Confirm MPIN:");
        if (confirm.isEmpty()) {
            return;
        }

        if (!mpin.get().matches("\\d{4}")) {
            throw new IllegalArgumentException("MPIN must be exactly 4 digits");
        }
        if (!mpin.get().equals(confirm.get())) {
            throw new IllegalArgumentException("MPIN and confirm MPIN do not match");
        }

        authService.setMpin(currentUser.getId(), mpin.get());
        showInfo("MPIN Updated", "Your transfer MPIN has been set successfully.");
    }

    private void promptMpinSetupIfMissing() {
        if (authService.hasMpin(currentUser.getId())) {
            return;
        }

        Alert alert = new Alert(
                Alert.AlertType.INFORMATION,
                "You have not created an MPIN yet. Set a 4-digit MPIN now. It will be required for all money transfers.",
                ButtonType.OK
        );
        alert.setTitle("MPIN Setup Required");
        alert.setHeaderText("Create Transfer MPIN");
        alert.showAndWait();

        try {
            handleSetOrResetMpin();
        } catch (Exception ex) {
            showError("MPIN setup skipped", ex.getMessage());
        }
    }

    private Optional<String> promptMaskedInput(String title, String header, String labelText) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        ButtonType okButtonType = new ButtonType("Submit", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        PasswordField input = new PasswordField();
        input.setPromptText(labelText);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label(labelText), 0, 0);
        grid.add(input, 1, 0);

        dialog.getDialogPane().setContent(grid);
        dialog.setResultConverter(button -> button == okButtonType ? input.getText() : null);

        return dialog.showAndWait().map(String::trim).filter(value -> !value.isEmpty());
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

        showInfo("Users", table.isBlank() ? "No users found." : table);

        TextInputDialog actionDialog = new TextInputDialog("VIEW");
        actionDialog.setTitle("Manage Users");
        actionDialog.setHeaderText("Enter action: VIEW, DELETE:<id>, UPDATE_ROLE:<id>:<CUSTOMER|MANAGER>, BLOCK:<user id or account number>, UNBLOCK:<user id or account number>");
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

            if (action.toUpperCase().startsWith("BLOCK:")) {
                long userId = Long.parseLong(action.substring("BLOCK:".length()).trim());
                if (userId == currentUser.getId()) {
                    throw new IllegalStateException("You cannot block your own account");
                }
                managerService.blockUser(userId);
                showInfo("Manage Users", "User blocked: " + userId);
                enterAdminMode();
                return;
            }

            if (action.toUpperCase().startsWith("UNBLOCK:")) {
                long userId = Long.parseLong(action.substring("UNBLOCK:".length()).trim());
                managerService.unblockUser(userId);
                showInfo("Manage Users", "User unblocked: " + userId);
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

            throw new IllegalArgumentException("Unknown action. Use VIEW, DELETE:<id>, UPDATE_ROLE:<id>:<role>, BLOCK:<id>, or UNBLOCK:<id>");
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
