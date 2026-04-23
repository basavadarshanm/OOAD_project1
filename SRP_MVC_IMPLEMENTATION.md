**MVC Architecture Used?**

- **Yes.**
- **Type used:** Layered JavaFX MVC (Desktop MVC).

**Explanation:**

i. Model: plain domain entities encapsulating state and minimal behaviour (immutability where appropriate) — e.g., `Account`, `Transaction`, `User`.

ii. View: JavaFX FXML screens provide UI layout and styling; FXML files are purely declarative and contain no business logic.

iii. Controller: JavaFX controllers mediate between user actions (view) and business logic (services). Controllers handle UI events, update view state, and delegate operations to services/repositories.

iv. Services & Repositories: application logic (validation, business rules) lives in service classes; persistence concerns are isolated in repository classes. This layered split keeps controllers thin and focused on UI coordination.

Model layer contains domain entities like `User`, `Account`, `Transaction`, `BillPayment`.

View layer is built with JavaFX FXML screens.

Controller layer handles UI events and delegates business logic to services.

Services and repositories extend MVC into a layered architecture, keeping controllers clean.

**Evidence (representative files):**

- Controllers: [src/main/java/com/onlinebanking/controller/DashboardController.java](src/main/java/com/onlinebanking/controller/DashboardController.java#L1), [src/main/java/com/onlinebanking/controller/LoginController.java](src/main/java/com/onlinebanking/controller/LoginController.java#L1)
- Views: [src/main/resources/fxml/dashboard.fxml](src/main/resources/fxml/dashboard.fxml), [src/main/resources/fxml/login.fxml](src/main/resources/fxml/login.fxml)
- Models: [src/main/java/com/onlinebanking/model/Account.java](src/main/java/com/onlinebanking/model/Account.java#L1), [src/main/java/com/onlinebanking/model/Transaction.java](src/main/java/com/onlinebanking/model/Transaction.java#L1)


**Design Principles — Single Responsibility Principle (SRP)**

- Each class has one clear responsibility:
  - `AuthService` — authentication and user registration checks.
  - `AccountService` — account-related business rules (create, add money, transaction listing).
  - `ReceiptBuilder` / `ReceiptService` — formatting/producing textual receipts.

- This separation ensures: easier testing, safer refactors, and controllers that simply orchestrate UI <-> service calls.

**Concrete code snippets & explanations**

1) Controller responsibility (UI coordination, not business logic)

From `DashboardController` (controller coordinates view and delegates to `AccountService`, `ReceiptService`, etc.):

```
public void setCurrentUser(User user) {
    // MVC: controller receives authenticated user context and coordinates view state.
    this.currentUser = user;
    welcomeLabel.setText("Welcome, " + user.getUsername());
    actionMessageLabel.setText("");

    if ("MANAGER".equalsIgnoreCase(user.getRole())) {
        enterAdminMode();
        return;
    }

    loadFirstAccount();
}
```

Why this demonstrates SRP/MVC: `setCurrentUser` updates UI and decides view mode; it does NOT perform persistence or complex business rules — those live in service classes.

2) AuthService (single responsibility: authentication/registration)

Excerpt from `AuthService`:

```
public Optional<User> login(String username, String passwordPlain) {
    return userRepository.findByUsername(username)
            .filter(user -> !userRepository.isUserBlocked(user.getId()))
            .filter(user -> user.getPasswordHash().equals(passwordPlain));
}

public User register(String username, String passwordPlain) {
    // validation + creation; note repository is used for persistence
    return userRepository.create(normalizedUsername, passwordPlain, "CUSTOMER");
}
```

Why SRP: `AuthService` focuses solely on authentication/registration logic and delegates storage to `UserRepository`.

3) AccountService (account business rules, validations)

Excerpt from `AccountService` (create and add money show validation and repository interaction):

```
public Account addMoney(Account account, BigDecimal amount) {
    if (account == null) {
        throw new IllegalArgumentException("Account is required");
    }
    if (amount == null || amount.signum() <= 0) {
        throw new IllegalArgumentException("Amount must be positive");
    }

    BigDecimal newBalance = account.getBalance().add(amount);
    accountRepository.updateBalance(account.getId(), newBalance);
    transactionRepository.create(
            account.getId(),
            null,
            "DEPOSIT",
            amount,
            "Cash deposit"
    );

    return AccountFactory.create(account.getId(), account.getUserId(), account.getAccountNumber(), newBalance);
}
```

Why SRP: `AccountService` enforces business constraints and updates repositories. It does not manipulate UI or format results — that is left to controllers and builder/service classes.

4) ReceiptBuilder (single, focused formatting responsibility)

Excerpt from `ReceiptBuilder`:

```
public ReceiptBuilder line(String text) {
    lines.add(text == null ? "" : text);
    return this;
}

public String build() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n");
    for (String line : lines) {
        sb.append(line).append("\n");
    }
    return sb.toString();
}
```

Why SRP: builder constructs receipt text incrementally; it has no idea about transactions persistence or UI.


**File/Directory overview (key locations)**

- Controllers: `src/main/java/com/onlinebanking/controller/` — UI controllers (DashboardController, LoginController, TransferMoneyController, etc.)
- Models: `src/main/java/com/onlinebanking/model/` — domain entities (`Account.java`, `Transaction.java`, `User.java`, `BillPayment.java`)
- Services: `src/main/java/com/onlinebanking/service/` — business logic (`AuthService.java`, `AccountService.java`, `TransferService.java`, `ReceiptService.java`)
- Repositories: `src/main/java/com/onlinebanking/repository/` (and `repository/jdbc`) — persistence abstractions/implementations
- Views: `src/main/resources/fxml/` — FXML screens (`dashboard.fxml`, `login.fxml`, `transfer.fxml`, ...)
- Builders/Factories: `src/main/java/com/onlinebanking/builder/` and `.../factory/` — helper patterns (`ReceiptBuilder`, `AccountFactory`)
- Configuration: `src/main/java/com/onlinebanking/config/ApplicationContext.java` — dependency wiring and controller factory used by FXMLLoader


**Guidance & notes**

- Controllers stay thin: if you find validation or persistence code inside a controller, move it into a service.
- Services should be unit tested independently; repositories can be mocked to verify business rules.
- Keep formatting/presentation (receipts, CSV, logs) in dedicated classes (builders/services) to respect SRP.

---

If you want, I can:
- Add more annotated snippets (transfer flow, bill payment) from specific files.
- Insert line-numbered links to particular methods for easier review.
- Merge this into `IMPLEMENTATION_SUMMARY.md` or place under a `docs/` folder.

File created at: [SRP_MVC_IMPLEMENTATION.md](SRP_MVC_IMPLEMENTATION.md)
