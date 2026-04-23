# Online Banking System

A JavaFX desktop banking application with customer and admin flows, H2 persistence, and layered MVC-style architecture.

## Overview

This project demonstrates:
- JavaFX UI with FXML screens
- JDBC repositories with HikariCP pooling
- H2 embedded database persistence
- Layered architecture: Controller -> Service -> Repository -> Database
- Manual dependency injection through a central application context
- Input validation and transaction-safe operations

## Implemented Features

### Customer Features
- User registration and login
- Account creation via dedicated screen
- Auto-generated unique 8-digit account ID
- Unique 10-digit phone number binding per account
- Initial deposit at account creation
- Add Money (deposit)
- Add Beneficiary using a user ID or 10-digit phone number
- Beneficiary creation validates that the referenced user/account exists before saving
- Transfer money using recipient account ID or phone number
- Bill payment with receipt generation
- Dashboard transaction history view

### Admin (Manager) Features
- Role-based admin login mode
- Read-only global transaction monitor
- Manage users by ID from the dashboard
- Block and unblock a user by entering the user ID
- View the current user list with ID, username, role, and block status columns
- Customer financial actions disabled in admin mode

## Dashboard User Management

- Customer users see the Add Beneficiary action on the dashboard.
- Admin users can open Manage Users to view the full summary table first, then run actions such as `VIEW`, `DELETE:<id>`, `UPDATE_ROLE:<id>:<CUSTOMER|MANAGER>`, `BLOCK:<id>`, and `UNBLOCK:<id>`.
- Blocking and unblocking are validated against the entered user ID before the repository update is applied.

## Tech Stack

| Component | Technology |
|-----------|------------|
| UI | JavaFX 21 (FXML) |
| Language | Java 21 |
| Build | Maven |
| DB | H2 Embedded |
| Pooling | HikariCP |
| Logging | SLF4J |

## Project Structure

```text
src/main/java/com/onlinebanking/
  App.java
  config/ApplicationContext.java
  controller/
    LoginController.java
    RegisterController.java
    DashboardController.java
    AccountCreationController.java
    TransferMoneyController.java
    PayBillsController.java
  model/
    User.java
    Account.java
    Transaction.java
    BillPayment.java
    Beneficiary.java
    Manager.java
  repository/
    UserRepository.java
    AccountRepository.java
    TransactionRepository.java
    BillPaymentRepository.java
    BeneficiaryRepository.java
    jdbc/
      JdbcUserRepository.java
      JdbcAccountRepository.java
      JdbcTransactionRepository.java
      JdbcBillPaymentRepository.java
      JdbcBeneficiaryRepository.java
  service/
    AuthService.java
    AccountService.java
    TransferService.java
    BillPayService.java
    ManagerService.java
    ReceiptService.java
  util/DataSourceFactory.java

src/main/resources/
  application.properties
  fxml/
    login.fxml
    register.fxml
    dashboard.fxml
    account_creation.fxml
    transfer.fxml
    billpay.fxml
  styles/app.css
```

## Build and Run

```bash
mvn clean package
mvn javafx:run
```

## Database Notes

- Database file: `./data/online_banking.mv.db`
- URL in app: `jdbc:h2:./data/online_banking;MODE=MySQL;AUTO_SERVER=TRUE;LOCK_TIMEOUT=5000`
- Schema is auto-created/updated on startup by `DataSourceFactory`

## Login Notes

- Customers: use registered user credentials
- Admin: if missing after DB reset, insert manually in H2 shell:

```sql
INSERT INTO users (username, password_hash, role)
VALUES ('admin_user', 'admin123', 'MANAGER');
```

## Troubleshooting

### DB Lock Error

If you see `Database may be already in use`:
1. Close app and any open H2 shell sessions.
2. Start app again with `mvn javafx:run`.

### H2 Shell (Windows) Safe Command

```powershell
$h2Jar = Join-Path $env:USERPROFILE ".m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar"
$dbPath = "C:/Users/<your-user>/Desktop/OOAD/final_project/OOAD_project1/data/online_banking"
java -cp "$h2Jar" org.h2.tools.Shell -url "jdbc:h2:file:$dbPath" -user "sa"
```

## Design Patterns Used

- MVC-style UI separation (Controller/FXML/Model)
- Repository pattern for data access abstraction
- Service layer for business logic
- Singleton for `ApplicationContext`
- Factory-style controller creation via JavaFX controller factory
- Transaction script style atomic transfer in `TransferService`

## Status

- Version: 0.1.0-SNAPSHOT
- Java: 21
- Last updated: April 2026
