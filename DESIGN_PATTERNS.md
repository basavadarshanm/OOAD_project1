# Design Patterns Used in the Project

This document gives detailed pattern-level evidence for viva and report defense.

## 1. Pattern Coverage at a Glance

| Pattern | Status | Key Classes |
|---|---|---|
| MVC | Implemented | controllers + fxml + model packages |
| DAO (Repository) | Implemented (JDBC) | repository interfaces + jdbc implementations |
| Service Layer | Implemented | service package |
| Factory | Implemented | `AccountFactory`, `TransactionFactory`, `PaymentStrategyFactory` |
| Singleton | Implemented | `ApplicationContext` |
| DTO | Implemented | dto package records |
| Strategy | Implemented | payment strategy package |
| Builder | Implemented | transaction and receipt builders |
| DAO via Spring Data JPA | Not yet | currently JDBC-based DAO |

## 2. MVC Pattern

### Intent

Separate UI, data, and interaction logic to reduce coupling and improve maintainability.

### Where Used

Model:

- `src/main/java/com/onlinebanking/model/User.java`
- `src/main/java/com/onlinebanking/model/Account.java`
- `src/main/java/com/onlinebanking/model/Transaction.java`
- `src/main/java/com/onlinebanking/model/BillPayment.java`

View:

- `src/main/resources/fxml/login.fxml`
- `src/main/resources/fxml/register.fxml`
- `src/main/resources/fxml/dashboard.fxml`
- `src/main/resources/fxml/transfer.fxml`
- `src/main/resources/fxml/billpay.fxml`
- `src/main/resources/fxml/account_creation.fxml`

Controller:

- `src/main/java/com/onlinebanking/controller/LoginController.java`
- `src/main/java/com/onlinebanking/controller/RegisterController.java`
- `src/main/java/com/onlinebanking/controller/DashboardController.java`
- `src/main/java/com/onlinebanking/controller/TransferMoneyController.java`
- `src/main/java/com/onlinebanking/controller/PayBillsController.java`
- `src/main/java/com/onlinebanking/controller/AccountCreationController.java`

### Why It Fits

- UI layout is fully inside FXML, not hardcoded in service/repository.
- Controllers orchestrate actions but delegate business logic.
- Models are plain domain objects and are reused across layers.

### Viva Talking Point

"This is layered MVC: controllers handle events, services handle business rules, repositories handle persistence, and FXML handles presentation."

## 3. DAO Pattern (Repository Abstraction)

### Intent

Abstract DB access and SQL details behind interfaces so the rest of the system remains DB-technology-independent.

### Where Used

Repository contracts:

- `src/main/java/com/onlinebanking/repository/UserRepository.java`
- `src/main/java/com/onlinebanking/repository/AccountRepository.java`
- `src/main/java/com/onlinebanking/repository/TransactionRepository.java`
- `src/main/java/com/onlinebanking/repository/BillPaymentRepository.java`
- `src/main/java/com/onlinebanking/repository/BeneficiaryRepository.java`

DAO implementations:

- `src/main/java/com/onlinebanking/repository/jdbc/JdbcUserRepository.java`
- `src/main/java/com/onlinebanking/repository/jdbc/JdbcAccountRepository.java`
- `src/main/java/com/onlinebanking/repository/jdbc/JdbcTransactionRepository.java`
- `src/main/java/com/onlinebanking/repository/jdbc/JdbcBillPaymentRepository.java`
- `src/main/java/com/onlinebanking/repository/jdbc/JdbcBeneficiaryRepository.java`

### Why It Fits

- Services call interfaces, not SQL statements.
- JDBC details are isolated in one layer.
- Swapping to JPA later affects mostly repository layer.

### Caveat

The pattern is implemented, but with JDBC DAOs, not Spring Data JPA.

## 4. Service Layer Pattern

### Intent

Put business rules in dedicated classes to keep controllers simple and enforce domain consistency.

### Where Used

- `src/main/java/com/onlinebanking/service/AuthService.java`
- `src/main/java/com/onlinebanking/service/AccountService.java`
- `src/main/java/com/onlinebanking/service/TransferService.java`
- `src/main/java/com/onlinebanking/service/BillPayService.java`
- `src/main/java/com/onlinebanking/service/ManagerService.java`
- `src/main/java/com/onlinebanking/service/ReceiptService.java`

### Rule Examples

- duplicate username prevention (`AuthService`)
- blocked user login prevention (`AuthService`)
- one account per user, phone uniqueness (`AccountService`)
- transfer/bill payment validations (`TransferService`, `BillPayService`, strategy classes)
- manager operations and role update rules (`ManagerService`)

### Viva Talking Point

"Controllers do not make DB calls directly; they always call a service method that enforces business constraints."

## 5. Factory Pattern

### Intent

Centralize object creation so construction rules stay consistent and reusable.

### Where Used

- `src/main/java/com/onlinebanking/factory/AccountFactory.java`
- `src/main/java/com/onlinebanking/factory/TransactionFactory.java`
- `src/main/java/com/onlinebanking/strategy/PaymentStrategyFactory.java`

### Why It Fits

- account and transaction objects are built through dedicated factories.
- payment strategy selection logic is centralized.

### Benefit

If future transaction/account variants are introduced, creation logic remains localized.

## 6. Singleton Pattern

### Intent

Ensure a single, shared application dependency container during runtime.

### Where Used

- `src/main/java/com/onlinebanking/config/ApplicationContext.java`

### Implementation Evidence

- private static final single instance
- private constructor
- public `getInstance()` access method

### Why It Fits

- prevents duplicate datasource/repository/service objects
- consistent dependency graph across scenes/controllers

## 7. DTO Pattern

### Intent

Carry structured data between layers without exposing full domain objects unnecessarily.

### Where Used

- `src/main/java/com/onlinebanking/dto/TransferRequestDto.java`
- `src/main/java/com/onlinebanking/dto/BillPaymentRequestDto.java`
- `src/main/java/com/onlinebanking/dto/ReceiptDto.java`
- `src/main/java/com/onlinebanking/dto/UserManagementDto.java`

### Why It Fits

- request payloads are bundled for strategy execution.
- manager screen consumes summary DTOs instead of mutable domain entities.
- receipt generation can return a transport object fit for UI.

## 8. Strategy Pattern

### Intent

Define a family of algorithms and select one at runtime.

### Where Used

- strategy interface: `src/main/java/com/onlinebanking/strategy/PaymentStrategy.java`
- transfer algorithm: `src/main/java/com/onlinebanking/strategy/TransferPaymentStrategy.java`
- bill payment algorithm: `src/main/java/com/onlinebanking/strategy/BillPaymentStrategy.java`
- runtime selector: `src/main/java/com/onlinebanking/strategy/PaymentStrategyFactory.java`

### Why It Fits

- transfer and bill payment have distinct rules and write sequences.
- both are treated uniformly through the strategy contract.
- adding a new payment type does not require rewriting existing strategy classes.

### Viva Talking Point

"We separated transfer and bill payment algorithms using Strategy, then selected concrete behavior using a factory to satisfy open-closed design."

## 9. Builder Pattern

### Intent

Build complex objects and formatted outputs with readable step-by-step construction.

### Where Used

- `src/main/java/com/onlinebanking/builder/TransactionBuilder.java`
- `src/main/java/com/onlinebanking/builder/ReceiptBuilder.java`

### Why It Fits

- transaction creation can set fields fluently and clearly.
- receipt composition is cleaner than manual concatenation in multiple locations.

### Benefit

- improved readability
- reduced construction mistakes
- easier extension of output format

## 10. Pattern-to-Requirement Traceability

| Requirement Item | Pattern Contribution | Implementation Evidence |
|---|---|---|
| Register/Login | MVC + Service + DAO | login/register controllers -> `AuthService` -> `UserRepository` |
| Create Account | MVC + Service + Factory + DAO | `AccountCreationController`, `AccountService`, `AccountFactory` |
| Transfer Money | MVC + Service + Strategy + DTO + DAO + Builder | `TransferMoneyController`, `TransferService`, transfer strategy, transaction builder/factory |
| Pay Bills | MVC + Service + Strategy + DTO + DAO + Builder | `PayBillsController`, `BillPayService`, bill strategy |
| View Transactions | MVC + Service + DAO | dashboard + account service + transaction repository |
| Manager Manage Users | MVC + Service + DTO + DAO | dashboard manage users + `ManagerService` |
| View All Transactions | MVC + Service + DAO | admin mode + transaction repo |

## 11. What to Say if Asked About Spring Boot/JPA

Suggested answer:

"All required patterns are implemented. DAO is currently realized using JDBC repositories. If strict rubric requires Spring Data JPA specifically, we can migrate repository implementations to JPA while keeping MVC, service, and strategy layers unchanged."
