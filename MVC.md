# MVC Implementation Details

## 1. MVC in This Project

This application implements layered MVC for JavaFX desktop:

- Model: domain and data structures.
- View: FXML screens and CSS.
- Controller: event handling and navigation.
- Service/Repository layers: support MVC by keeping business and persistence outside the controller.

## 2. Complete MVC Mapping

## 2.1 Model Layer

Core domain models:

- `src/main/java/com/onlinebanking/model/User.java`
- `src/main/java/com/onlinebanking/model/Account.java`
- `src/main/java/com/onlinebanking/model/Transaction.java`
- `src/main/java/com/onlinebanking/model/BillPayment.java`
- `src/main/java/com/onlinebanking/model/Beneficiary.java`

Supporting transfer objects for controller-service boundaries:

- `src/main/java/com/onlinebanking/dto/TransferRequestDto.java`
- `src/main/java/com/onlinebanking/dto/BillPaymentRequestDto.java`
- `src/main/java/com/onlinebanking/dto/ReceiptDto.java`
- `src/main/java/com/onlinebanking/dto/UserManagementDto.java`

## 2.2 View Layer

FXML screens:

- `src/main/resources/fxml/login.fxml`
- `src/main/resources/fxml/register.fxml`
- `src/main/resources/fxml/dashboard.fxml`
- `src/main/resources/fxml/transfer.fxml`
- `src/main/resources/fxml/billpay.fxml`
- `src/main/resources/fxml/account_creation.fxml`

Style definition:

- `src/main/resources/styles/app.css`

## 2.3 Controller Layer

- `src/main/java/com/onlinebanking/controller/LoginController.java`
- `src/main/java/com/onlinebanking/controller/RegisterController.java`
- `src/main/java/com/onlinebanking/controller/DashboardController.java`
- `src/main/java/com/onlinebanking/controller/TransferMoneyController.java`
- `src/main/java/com/onlinebanking/controller/PayBillsController.java`
- `src/main/java/com/onlinebanking/controller/AccountCreationController.java`

## 3. Controller-by-Controller Detailed Behavior

## 3.1 LoginController

Responsibilities:

- captures credentials from view
- delegates authentication to `AuthService`
- performs scene transition to dashboard after successful login
- handles register screen navigation

MVC rationale:

- no SQL in controller
- no complex business logic in controller

## 3.2 RegisterController

Responsibilities:

- validates username/password/confirm password
- delegates registration to `AuthService`
- shows validation and duplicate-user messages
- navigates back to login with info message

## 3.3 DashboardController

Responsibilities:

- stores authenticated `currentUser`
- loads account and transactions via `AccountService`
- toggles UI actions by role and account availability
- manager mode:
  - shows global transactions
  - enables user management action
- contains action methods for transfer, bill pay, create account, add money, user details, logout

Security controls:

- `isLoggedIn()` guard before actions
- manager/customer behavior split in `setCurrentUser` and `enterAdminMode`

## 3.4 TransferMoneyController

Responsibilities:

- validates recipient format (8-digit account or 10-digit phone)
- validates positive amount
- calls `TransferService.transfer`
- updates balance and receipt area
- handles navigation back to dashboard

## 3.5 PayBillsController

Responsibilities:

- validates biller name, reference, due date, and amount
- calls `BillPayService.payBill`
- updates balance and renders receipt
- handles navigation back to dashboard

## 3.6 AccountCreationController

Responsibilities:

- pre-fills username
- generates suggested account id
- validates user input and delegates create-account logic
- navigates back to dashboard with status message

## 4. MVC Sequence Diagrams (Textual)

## 4.1 Login Sequence

1. View `login.fxml` captures credentials.
2. `LoginController.handleLogin` invoked.
3. `AuthService.login` verifies user.
4. On success, `DashboardController.setCurrentUser` runs.
5. Dashboard view shown.

## 4.2 Transfer Sequence

1. User fills transfer form in `transfer.fxml`.
2. `TransferMoneyController.handleTransfer` validates input.
3. Calls `TransferService.transfer`.
4. Service delegates algorithm to transfer strategy.
5. Strategy updates balances and stores transaction.
6. Controller renders receipt and balance update.

## 4.3 Bill Payment Sequence

1. User fills bill details in `billpay.fxml`.
2. `PayBillsController.handlePayBill` validates input.
3. Calls `BillPayService.payBill`.
4. Service delegates to bill payment strategy.
5. Strategy deducts account, creates transaction, stores bill payment.
6. Controller updates view and receipt.

## 5. MVC Boundary Discipline

What stays in controllers:

- event handling
- scene navigation
- displaying validation errors and receipts

What stays out of controllers:

- SQL statements
- account/transaction rule calculations
- strategy selection internals

Where those are handled instead:

- Services: business orchestration
- Repositories: SQL execution
- Strategy classes: payment algorithm branching

## 6. Why This MVC Is Strong for Evaluation

- Clear layer split for easy explanation in viva.
- Functional use-cases map directly to controller action handlers.
- Domain rules are centralized and reusable.
- UI remains independent of persistence implementation details.

## 7. Requirements Satisfaction Table (MVC Perspective)

| Use Case Requirement | MVC Entry Point (Controller) | Service Layer | Repository/DB | Status |
|---|---|---|---|---|
| Register | `RegisterController.handleRegister` | `AuthService.register` | `UserRepository.create` | Satisfied |
| Login | `LoginController.handleLogin` | `AuthService.login` | `UserRepository.findByUsername` | Satisfied |
| Logout | `DashboardController.handleLogout` | N/A scene reset | N/A | Satisfied |
| Create Account | `AccountCreationController.handleCreateAccount` | `AccountService.createAccount` | `AccountRepository.create`, transaction insert | Satisfied |
| Check Balance | `DashboardController.loadFirstAccount` | `AccountService.getAccounts` | `AccountRepository.findByUserId` | Satisfied |
| Transfer Money | `TransferMoneyController.handleTransfer` | `TransferService.transfer` | account + transaction repositories | Satisfied |
| Pay Bills | `PayBillsController.handlePayBill` | `BillPayService.payBill` | account + bill payment + transaction repositories | Satisfied |
| View Transactions | `DashboardController.loadTransactions` | `AccountService.getRecentTransactions` | `TransactionRepository.findByAccountId` | Satisfied |
| Manager Login | `LoginController.handleLogin` | `AuthService.login` + role check | `UserRepository` | Satisfied |
| Manage Users | `DashboardController.handleManageUsers` | `ManagerService` methods | `UserRepository` update/delete/find | Satisfied |
| View All Transactions | `DashboardController.enterAdminMode` | `ManagerService.getAllTransactions` | `TransactionRepository.findAll` | Satisfied |

## 8. Professor Q&A (Extended)

Q1. Is this pure MVC or layered MVC?

A1. It is layered MVC. Controller handles UI interaction, then delegates to service and repository layers.

Q2. How do you prevent fat controller anti-pattern?

A2. Complex rules are placed in service and strategy classes, not in event handlers.

Q3. How are business constraints guaranteed?

A3. Validations are enforced in service/strategy methods, not only UI. This protects consistency even if another UI is added later.

Q4. Is controller tightly coupled to DB?

A4. No. Controllers only call services. SQL is isolated in repository JDBC implementations.

Q5. Can this MVC be upgraded to Spring Boot?

A5. Yes. The layer boundaries are already in place, so Spring integration mostly replaces wiring and repository implementation style.
