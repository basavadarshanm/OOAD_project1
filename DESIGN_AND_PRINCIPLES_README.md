# Online Banking Project Design Reference

## Purpose
This document captures the design styles, GRASP principles, SOLID principles, and design patterns used in this project, with:
1. Definition
2. Where used (files)
3. Code snippet
4. Explanation
5. Possible outcome/benefit
6. Interaction with other files

---

## 1. Overall Design Style (Layered + MVC)

### 1.1 Layered Architecture (Controller -> Service -> Repository -> DB)
Definition:
A layered architecture separates UI logic, business logic, and data access logic so each layer has focused responsibility.

Files used:
1. src/main/java/com/onlinebanking/controller/LoginController.java
2. src/main/java/com/onlinebanking/service/AuthService.java
3. src/main/java/com/onlinebanking/repository/UserRepository.java
4. src/main/java/com/onlinebanking/repository/jdbc/JdbcUserRepository.java

Code snippet:
```java
// LoginController
Optional<User> userOpt = authService.login(username, password);

// AuthService
return userRepository.findByUsername(username)
        .filter(user -> !userRepository.isUserBlocked(user.getId()))
        .filter(user -> user.getPasswordHash().equals(passwordPlain));
```

Explanation:
The controller never talks directly to SQL. It delegates authentication to AuthService, which delegates persistence to UserRepository/JdbcUserRepository.

Possible outcome:
1. Better maintainability
2. Clear responsibility boundaries
3. Easier testing and feature extension

Interaction with other files:
1. ApplicationContext wires controller-service-repository dependencies
2. DataSourceFactory provides DB connectivity used by repository implementations

### 1.2 MVC in JavaFX
Definition:
Model-View-Controller splits domain data, UI screens, and event handling.

Files used:
1. src/main/java/com/onlinebanking/model/Account.java
2. src/main/java/com/onlinebanking/controller/DashboardController.java
3. src/main/resources/fxml/dashboard.fxml

Code snippet:
```java
public void setCurrentUser(User user) {
    this.currentUser = user;
    welcomeLabel.setText("Welcome, " + user.getUsername());
    loadFirstAccount();
}
```

Explanation:
FXML defines view controls, DashboardController handles interactions, model objects provide data.

Possible outcome:
1. Cleaner UI logic
2. Easier UI updates without business-layer rewrites

Interaction with other files:
1. App loads FXML and controller factory
2. ApplicationContext supplies controller instances with dependencies

---

## 2. GRASP Principles Used

## 2.1 Controller (GRASP)
Definition:
Assign system event handling to a class representing the use-case boundary (UI controller).

Files used:
1. src/main/java/com/onlinebanking/controller/TransferMoneyController.java
2. src/main/java/com/onlinebanking/controller/PayBillsController.java
3. src/main/java/com/onlinebanking/controller/DashboardController.java

Code snippet:
```java
// TransferMoneyController
Transaction tx = transferService.transfer(
        currentAccount.getAccountNumber(),
        toAccount,
        amount,
        description.isEmpty() ? "Transfer" : description
);
```

Explanation:
TransferMoneyController receives user event and delegates actual business behavior to TransferService.

Possible outcome:
1. Thin UI handlers
2. Reusable service logic

Interaction with other files:
1. Uses TransferService
2. Uses ReceiptService for output rendering

## 2.2 Information Expert (GRASP)
Definition:
Give responsibility to the class that has the necessary data.

Files used:
1. src/main/java/com/onlinebanking/strategy/TransferPaymentStrategy.java
2. src/main/java/com/onlinebanking/strategy/BillPaymentStrategy.java

Code snippet:
```java
if (from.getBalance().compareTo(amount) < 0) {
    throw new IllegalStateException("Insufficient funds. Available: " + from.getBalance());
}
```

Explanation:
TransferPaymentStrategy has account data and business rule context; therefore it validates funds and transfer constraints.

Possible outcome:
1. Rules live near the decision logic
2. Reduced duplication

Interaction with other files:
1. Fetches/updates accounts using AccountRepository
2. Persists transaction using TransactionRepository

## 2.3 Creator (GRASP)
Definition:
Assign object creation to classes that aggregate, closely use, or contain initialization data.

Files used:
1. src/main/java/com/onlinebanking/factory/TransactionFactory.java
2. src/main/java/com/onlinebanking/builder/TransactionBuilder.java
3. src/main/java/com/onlinebanking/repository/jdbc/JdbcTransactionRepository.java

Code snippet:
```java
return TransactionFactory.completed(
    id,
    fromAccountId,
    toAccountId,
    transactionType,
    amount,
    description,
    java.time.LocalDateTime.now()
);
```

Explanation:
JdbcTransactionRepository obtains DB-generated fields and uses TransactionFactory to build consistent Transaction objects.

Possible outcome:
1. Standardized object creation
2. Lower constructor noise

Interaction with other files:
1. TransactionFactory internally uses TransactionBuilder

## 2.4 Low Coupling (GRASP)
Definition:
Minimize class dependency on concrete implementations.

Files used:
1. src/main/java/com/onlinebanking/service/AuthService.java
2. src/main/java/com/onlinebanking/repository/UserRepository.java
3. src/main/java/com/onlinebanking/config/ApplicationContext.java

Code snippet:
```java
private final UserRepository userRepository;

public AuthService(UserRepository userRepository) {
    this.userRepository = userRepository;
}
```

Explanation:
AuthService depends on UserRepository interface, not JdbcUserRepository directly.

Possible outcome:
1. Easier replacement/testing of repository layer

Interaction with other files:
1. ApplicationContext binds UserRepository to JdbcUserRepository

## 2.5 High Cohesion (GRASP)
Definition:
Keep each class focused on a narrow, related set of responsibilities.

Files used:
1. src/main/java/com/onlinebanking/service/ReceiptService.java
2. src/main/java/com/onlinebanking/builder/ReceiptBuilder.java

Code snippet:
```java
public ReceiptDto generateBillPaymentReceiptDto(...) {
    String body = new ReceiptBuilder()
            .separator()
            .line("        BILL PAYMENT RECEIPT          ")
            .build();
    return new ReceiptDto("BILL_PAYMENT", body);
}
```

Explanation:
ReceiptService is cohesive around receipt generation, while formatting assembly is delegated to ReceiptBuilder.

Possible outcome:
1. Readable code
2. Focused change impact

Interaction with other files:
1. Controllers call ReceiptService after successful operations

---

## 3. SOLID Principles Used

## 3.1 S - Single Responsibility Principle
Definition:
A class should have one reason to change.

Files used:
1. src/main/java/com/onlinebanking/controller/RegisterController.java
2. src/main/java/com/onlinebanking/service/AuthService.java
3. src/main/java/com/onlinebanking/repository/jdbc/JdbcUserRepository.java

Code snippet:
```java
// RegisterController: UI validation + navigation only
if (!password.equals(confirm)) {
    messageLabel.setText("Passwords do not match");
    return;
}
```

Explanation:
RegisterController handles screen behavior; AuthService handles auth rules; JdbcUserRepository handles SQL only.

Possible outcome:
1. Easier maintenance
2. Lower regression risk

Interaction with other files:
1. Controller -> Service -> Repository chain remains clean

## 3.2 O - Open/Closed Principle
Definition:
Open for extension, closed for modification.

Files used:
1. src/main/java/com/onlinebanking/strategy/PaymentStrategy.java
2. src/main/java/com/onlinebanking/strategy/TransferPaymentStrategy.java
3. src/main/java/com/onlinebanking/strategy/BillPaymentStrategy.java
4. src/main/java/com/onlinebanking/strategy/PaymentStrategyFactory.java

Code snippet:
```java
public interface PaymentStrategy<TRequest, TResult> {
    TResult execute(TRequest request);
}
```

Explanation:
To add a new payment mode, create a new strategy implementation and register it in factory flow.

Possible outcome:
1. Extensible payment architecture
2. Less fragile service code

Interaction with other files:
1. TransferService and BillPayService request strategy from PaymentStrategyFactory

## 3.3 L - Liskov Substitution Principle
Definition:
Implementations should be substitutable for their interface contract.

Files used:
1. src/main/java/com/onlinebanking/repository/UserRepository.java
2. src/main/java/com/onlinebanking/repository/jdbc/JdbcUserRepository.java

Code snippet:
```java
public class JdbcUserRepository implements UserRepository {
    @Override
    public Optional<User> findByUsername(String username) { ... }
}
```

Explanation:
Any valid UserRepository implementation can replace JdbcUserRepository without changing service usage.

Possible outcome:
1. Future flexibility (mock/in-memory/other DB)

Interaction with other files:
1. AuthService stays unchanged when implementation changes

## 3.4 I - Interface Segregation Principle
Definition:
Clients should not be forced to depend on methods they do not use.

Files used:
1. src/main/java/com/onlinebanking/repository/AccountRepository.java
2. src/main/java/com/onlinebanking/repository/TransactionRepository.java
3. src/main/java/com/onlinebanking/repository/BillPaymentRepository.java
4. src/main/java/com/onlinebanking/repository/BeneficiaryRepository.java

Code snippet:
```java
public interface AccountRepository { ... }
public interface TransactionRepository { ... }
```

Explanation:
Data contracts are split by domain responsibility rather than one huge data-access interface.

Possible outcome:
1. Clearer contracts
2. Simpler implementation classes

Interaction with other files:
1. Services inject only needed repository interfaces

## 3.5 D - Dependency Inversion Principle
Definition:
High-level modules depend on abstractions, not concrete details.

Files used:
1. src/main/java/com/onlinebanking/service/TransferService.java
2. src/main/java/com/onlinebanking/service/ManagerService.java
3. src/main/java/com/onlinebanking/config/ApplicationContext.java

Code snippet:
```java
public TransferService(AccountRepository accountRepository, PaymentStrategyFactory paymentStrategyFactory) {
    this.accountRepository = accountRepository;
    this.paymentStrategyFactory = paymentStrategyFactory;
}
```

Explanation:
Service classes rely on repository interfaces and strategy abstraction; wiring to concrete classes is in ApplicationContext.

Possible outcome:
1. Loose coupling
2. Better testability

Interaction with other files:
1. ApplicationContext maps interfaces to JDBC implementations

---

## 4. Design Patterns Used

## 4.1 Singleton Pattern
Definition:
Ensure only one instance and provide global access point.

Files used:
1. src/main/java/com/onlinebanking/config/ApplicationContext.java

Code snippet:
```java
private static final ApplicationContext INSTANCE = new ApplicationContext();

public static ApplicationContext getInstance() {
    return INSTANCE;
}
```

Explanation:
One shared application context controls dependency graph and lifecycle.

Possible outcome:
1. Consistent wiring
2. Centralized shutdown

Interaction with other files:
1. App uses ApplicationContext for controller factory

## 4.2 Strategy Pattern
Definition:
Encapsulate interchangeable algorithms behind a common interface.

Files used:
1. src/main/java/com/onlinebanking/strategy/PaymentStrategy.java
2. src/main/java/com/onlinebanking/strategy/TransferPaymentStrategy.java
3. src/main/java/com/onlinebanking/strategy/BillPaymentStrategy.java
4. src/main/java/com/onlinebanking/strategy/PaymentStrategyFactory.java

Code snippet:
```java
return paymentStrategyFactory
        .<TransferRequestDto, Transaction>getStrategy(PaymentType.TRANSFER)
        .execute(request);
```

Explanation:
TransferService and BillPayService delegate operation behavior to strategy implementations.

Possible outcome:
1. Cleaner service methods
2. Easier new payment mode addition

Interaction with other files:
1. Strategies use repositories for persistence and state updates

## 4.3 Factory Pattern
Definition:
Centralize object creation logic.

Files used:
1. src/main/java/com/onlinebanking/factory/AccountFactory.java
2. src/main/java/com/onlinebanking/factory/TransactionFactory.java
3. src/main/java/com/onlinebanking/strategy/PaymentStrategyFactory.java

Code snippet:
```java
return AccountFactory.create(account.getId(), account.getUserId(), account.getAccountNumber(), newBalance);
```

Explanation:
Factory classes avoid repeated construction logic and keep object creation consistent.

Possible outcome:
1. Cleaner constructors usage
2. Reusable creation rules

Interaction with other files:
1. AccountService and JdbcTransactionRepository consume factory outputs

## 4.4 Builder Pattern
Definition:
Construct complex object/text step-by-step.

Files used:
1. src/main/java/com/onlinebanking/builder/TransactionBuilder.java
2. src/main/java/com/onlinebanking/builder/ReceiptBuilder.java
3. src/main/java/com/onlinebanking/service/ReceiptService.java

Code snippet:
```java
ReceiptBuilder builder = new ReceiptBuilder()
        .separator()
        .line("         TRANSACTION RECEIPT           ")
        .dashedSeparator();
```

Explanation:
ReceiptBuilder supports fluent receipt formatting without long string concatenation noise.

Possible outcome:
1. Readable formatting logic
2. Easier receipt format updates

Interaction with other files:
1. ReceiptService uses ReceiptBuilder and returns ReceiptDto to controllers

## 4.5 Repository Pattern
Definition:
Encapsulate data access behind domain-oriented interfaces.

Files used:
1. src/main/java/com/onlinebanking/repository/*.java
2. src/main/java/com/onlinebanking/repository/jdbc/*.java

Code snippet:
```java
public interface TransactionRepository {
    List<Transaction> findByAccountId(long accountId, int limit);
    Transaction create(long fromAccountId, Long toAccountId, String transactionType, BigDecimal amount, String description);
}
```

Explanation:
Services call repository contracts, while JDBC classes manage SQL and row mapping.

Possible outcome:
1. DB technology hidden from business layer
2. Better modularity

Interaction with other files:
1. Used by AccountService, TransferPaymentStrategy, BillPaymentStrategy, ManagerService

## 4.6 DTO Pattern
Definition:
Use small, immutable transfer objects for request/response payloads.

Files used:
1. src/main/java/com/onlinebanking/dto/TransferRequestDto.java
2. src/main/java/com/onlinebanking/dto/BillPaymentRequestDto.java
3. src/main/java/com/onlinebanking/dto/ReceiptDto.java
4. src/main/java/com/onlinebanking/dto/UserManagementDto.java

Code snippet:
```java
TransferRequestDto request = new TransferRequestDto(fromAccountNumber, recipientIdentifier, amount, description);
```

Explanation:
DTOs simplify method signatures and provide immutable data transfer contracts.

Possible outcome:
1. Cleaner APIs between layers
2. Reduced accidental mutation

Interaction with other files:
1. Created in services, consumed by strategies/UI output

---

## 5. End-to-End Interaction Example (Transfer)

Flow:
1. TransferMoneyController.handleTransfer validates input and MPIN
2. TransferService.transfer creates TransferRequestDto
3. PaymentStrategyFactory returns TransferPaymentStrategy
4. TransferPaymentStrategy validates and updates account balances
5. JdbcTransactionRepository stores transaction
6. ReceiptService generates display receipt text for UI

Files interacting:
1. src/main/java/com/onlinebanking/controller/TransferMoneyController.java
2. src/main/java/com/onlinebanking/service/TransferService.java
3. src/main/java/com/onlinebanking/strategy/PaymentStrategyFactory.java
4. src/main/java/com/onlinebanking/strategy/TransferPaymentStrategy.java
5. src/main/java/com/onlinebanking/repository/jdbc/JdbcAccountRepository.java
6. src/main/java/com/onlinebanking/repository/jdbc/JdbcTransactionRepository.java
7. src/main/java/com/onlinebanking/service/ReceiptService.java

Outcome:
1. Balance debited/credited
2. Transaction persisted
3. Receipt shown in transfer screen

---

## 6. Important Note
Current password and MPIN logic are plain-text comparisons, which is acceptable for learning/demo scope but should be replaced with secure hashing in production.
