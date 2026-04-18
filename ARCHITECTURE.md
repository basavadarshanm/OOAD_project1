# Online Banking System Architecture

## 1. System Architecture Overview

This project uses a layered, MVC-driven desktop architecture:

- Presentation Layer: JavaFX FXML views and controllers
- Business Layer: service classes that enforce rules
- Persistence Layer: repository interfaces and JDBC DAO implementations
- Data Layer: H2 embedded relational database

Main request-response pipeline:

`UI Event -> Controller -> Service -> Repository -> H2 DB -> Service -> Controller -> UI`

This structure ensures each layer has a single responsibility and keeps OOAD principles clear.

## 2. Startup and Dependency Wiring

### 2.1 Application Bootstrap

- Entry point: `src/main/java/com/onlinebanking/App.java`
- It loads the first JavaFX scene (`login.fxml`) and starts the app runtime.

### 2.2 Manual Dependency Injection

- Wiring class: `src/main/java/com/onlinebanking/config/ApplicationContext.java`
- Role:
  - Creates singleton application context.
  - Instantiates repositories, services, and controller factory.
  - Acts like a lightweight IoC container.

### 2.3 DataSource and Schema Initialization

- DB setup class: `src/main/java/com/onlinebanking/util/DataSourceFactory.java`
- Responsibilities:
  - Read DB config from `application.properties` and `.env` fallback.
  - Create HikariCP datasource.
  - Create tables if missing.
  - Run legacy schema compatibility migration (columns, defaults, indexes).

## 3. Layer-by-Layer Design

## 3.1 Presentation Layer

Controllers:

- `src/main/java/com/onlinebanking/controller/LoginController.java`
- `src/main/java/com/onlinebanking/controller/RegisterController.java`
- `src/main/java/com/onlinebanking/controller/DashboardController.java`
- `src/main/java/com/onlinebanking/controller/TransferMoneyController.java`
- `src/main/java/com/onlinebanking/controller/PayBillsController.java`
- `src/main/java/com/onlinebanking/controller/AccountCreationController.java`

FXML Views:

- `src/main/resources/fxml/login.fxml`
- `src/main/resources/fxml/register.fxml`
- `src/main/resources/fxml/dashboard.fxml`
- `src/main/resources/fxml/transfer.fxml`
- `src/main/resources/fxml/billpay.fxml`
- `src/main/resources/fxml/account_creation.fxml`

View styling:

- `src/main/resources/styles/app.css`

Responsibilities in this layer:

- Read user input.
- Do lightweight input checks.
- Delegate business logic to service layer.
- Display success/error/receipt output.
- Switch scenes for navigation.

## 3.2 Business Layer (Services)

Service classes:

- `src/main/java/com/onlinebanking/service/AuthService.java`
- `src/main/java/com/onlinebanking/service/AccountService.java`
- `src/main/java/com/onlinebanking/service/TransferService.java`
- `src/main/java/com/onlinebanking/service/BillPayService.java`
- `src/main/java/com/onlinebanking/service/ManagerService.java`
- `src/main/java/com/onlinebanking/service/ReceiptService.java`

Responsibilities:

- Implement core domain rules.
- Protect business consistency (balance checks, role checks, duplicate checks).
- Coordinate strategy/factory logic.
- Keep controllers free from domain complexity.

## 3.3 Persistence Layer (DAO)

Repository abstractions:

- `src/main/java/com/onlinebanking/repository/UserRepository.java`
- `src/main/java/com/onlinebanking/repository/AccountRepository.java`
- `src/main/java/com/onlinebanking/repository/TransactionRepository.java`
- `src/main/java/com/onlinebanking/repository/BillPaymentRepository.java`
- `src/main/java/com/onlinebanking/repository/BeneficiaryRepository.java`

JDBC implementations:

- `src/main/java/com/onlinebanking/repository/jdbc/JdbcUserRepository.java`
- `src/main/java/com/onlinebanking/repository/jdbc/JdbcAccountRepository.java`
- `src/main/java/com/onlinebanking/repository/jdbc/JdbcTransactionRepository.java`
- `src/main/java/com/onlinebanking/repository/jdbc/JdbcBillPaymentRepository.java`
- `src/main/java/com/onlinebanking/repository/jdbc/JdbcBeneficiaryRepository.java`

Responsibilities:

- Execute SQL operations.
- Map rows to model objects.
- Hide SQL details from service/controller layers.

## 3.4 Data Layer

- DB engine: H2 embedded (`online_banking.mv.db`)
- Config file: `src/main/resources/application.properties`
- Local DB file: `data/online_banking.mv.db`
- Schema migration/compatibility logic: `DataSourceFactory`

## 4. Cross-Cutting Pattern Modules

DTO module:

- `src/main/java/com/onlinebanking/dto/TransferRequestDto.java`
- `src/main/java/com/onlinebanking/dto/BillPaymentRequestDto.java`
- `src/main/java/com/onlinebanking/dto/ReceiptDto.java`
- `src/main/java/com/onlinebanking/dto/UserManagementDto.java`

Factory module:

- `src/main/java/com/onlinebanking/factory/AccountFactory.java`
- `src/main/java/com/onlinebanking/factory/TransactionFactory.java`

Strategy module:

- `src/main/java/com/onlinebanking/strategy/PaymentStrategy.java`
- `src/main/java/com/onlinebanking/strategy/TransferPaymentStrategy.java`
- `src/main/java/com/onlinebanking/strategy/BillPaymentStrategy.java`
- `src/main/java/com/onlinebanking/strategy/PaymentStrategyFactory.java`

Builder module:

- `src/main/java/com/onlinebanking/builder/TransactionBuilder.java`
- `src/main/java/com/onlinebanking/builder/ReceiptBuilder.java`

## 5. End-to-End Sequence Flows

## 5.1 Transfer Money

1. User enters recipient and amount in `transfer.fxml`.
2. `TransferMoneyController` validates format.
3. Controller calls `TransferService.transfer(...)`.
4. Service creates `TransferRequestDto`.
5. Strategy factory provides transfer strategy.
6. Strategy validates business rules:
   - positive amount
   - sender/receiver existence
   - no self transfer
   - sufficient balance
7. Strategy updates account balances via `AccountRepository`.
8. Strategy inserts transaction via `TransactionRepository`.
9. Controller updates UI balance and receipt area.

## 5.2 Bill Payment

1. User fills bill details in `billpay.fxml`.
2. `PayBillsController` validates required fields.
3. Calls `BillPayService.payBill(...)`.
4. Service creates `BillPaymentRequestDto`.
5. Strategy factory provides bill payment strategy.
6. Strategy validates account and funds.
7. Strategy creates BILL_PAYMENT transaction.
8. Strategy deducts account balance.
9. Strategy inserts bill payment record linked to transaction.
10. Controller shows receipt and updated balance.

## 5.3 Manager User Management

1. Manager logs in via `LoginController`.
2. `DashboardController` detects `MANAGER` role and enters admin mode.
3. Manager uses `handleManageUsers` action.
4. Controller calls `ManagerService` methods:
   - `getAllUserSummaries`
   - `deleteUser`
   - `updateUserRole`
5. Repository performs SQL update/delete/select.
6. Dashboard refreshes global view.

## 6. Data Model and Transaction Linkage

Core tables:

- `users`
- `accounts`
- `transactions`
- `bill_payments`
- `beneficiaries`

Linkage rules:

- `accounts.user_id` -> `users.id`
- `transactions.from_account_id` -> `accounts.id`
- `transactions.to_account_id` -> `accounts.id` (nullable)
- `bill_payments.account_id` -> `accounts.id`
- `bill_payments.transaction_id` -> `transactions.id`

This guarantees financial operations are auditable and linked.

## 7. Security and Validation Controls

Authentication and session context:

- login handled by `AuthService`
- blocked users cannot log in
- `DashboardController.isLoggedIn()` checks for action guard

Financial validation:

- transfer: amount > 0, funds check, self-transfer block
- bill payment: amount > 0, funds check
- account creation: one account per user, unique 10-digit phone, 8-digit account id

Role-based behavior:

- manager mode disables customer payment actions
- manager can view all transactions and manage users

## 8. Requirement Satisfaction Matrix

| Requirement | Status | Where Implemented |
|---|---|---|
| Customer Register | Satisfied | `RegisterController`, `AuthService`, `UserRepository` |
| Customer Login / Logout | Satisfied | `LoginController`, `AuthService`, dashboard->login scene flow |
| Create Account | Satisfied | `AccountCreationController`, `AccountService.createAccount` |
| Check Balance | Satisfied | `DashboardController.loadFirstAccount`, `AccountService.getAccounts` |
| Transfer Money | Satisfied | `TransferMoneyController`, `TransferService`, `TransferPaymentStrategy` |
| Pay Bills | Satisfied | `PayBillsController`, `BillPayService`, `BillPaymentStrategy` |
| View Transactions | Satisfied | `DashboardController`, `AccountService.getRecentTransactions` |
| Manager Login | Satisfied | `LoginController`, role check in `DashboardController.setCurrentUser` |
| Manage Users (view/delete/update) | Satisfied | `DashboardController.handleManageUsers`, `ManagerService` |
| View All Transactions (Manager) | Satisfied | `DashboardController.enterAdminMode`, `ManagerService.getAllTransactions` |
| Logged-in user required for actions | Satisfied | `DashboardController.isLoggedIn` guards |
| Insufficient balance transfer block | Satisfied | `TransferPaymentStrategy.execute` |
| Transaction stored for operations | Satisfied | `JdbcTransactionRepository.create` |
| Receipt generated for operations | Satisfied | `ReceiptService` used in transfer, bill pay, deposit |
| Bill payment deducts account balance | Satisfied | `BillPaymentStrategy.execute` |
| Transactions linked to account IDs | Satisfied | DB schema FKs + repository writes |
| MVC pattern | Satisfied | controller/model/fxml separation |
| DAO pattern | Satisfied (JDBC DAO) | repository interfaces + jdbc impl |
| Service Layer pattern | Satisfied | service package |
| Factory pattern | Satisfied | `AccountFactory`, `TransactionFactory`, `PaymentStrategyFactory` |
| Singleton pattern | Satisfied | `ApplicationContext` singleton |
| DTO pattern | Satisfied | `dto` package records |
| Strategy pattern | Satisfied | payment strategy module |
| Builder pattern | Satisfied | `TransactionBuilder`, `ReceiptBuilder` |
| DAO via Spring Data JPA | Not yet (technology caveat) | currently JDBC DAO, migration possible |

## 9. Architecture Strengths and Limitations

Strengths:

- Clear separation of concerns.
- Pattern-rich design aligned with OOAD goals.
- Easy to extend payment types due to strategy layer.
- Good maintainability through service/repository boundaries.

Limitation:

- DAO is implemented via JDBC, not Spring Data JPA.
- If professor requires JPA specifically, migration is required.

## 10. Migration Readiness (If Spring Boot/JPA Is Asked)

Minimal-impact migration path:

1. Add Spring Boot + Spring Data JPA dependencies.
2. Convert model classes to JPA entities.
3. Replace JDBC repository impl classes with `JpaRepository` interfaces.
4. Replace `ApplicationContext` manual wiring with Spring container.
5. Keep controllers and FXML flow mostly unchanged.

Because layering already exists, migration effort is moderate and controlled.
