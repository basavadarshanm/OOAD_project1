# Online Banking Desktop (JavaFX)

JavaFX desktop client for an online banking system. Layers: JavaFX UI, services, JDBC repositories (H2 embedded by default), and pooled connections via HikariCP.

## Prerequisites
- JDK 21+
- Maven 3.9+

## Configure
1) Copy `.env.example` to `.env` if you want to override defaults. By default, it uses file-based H2 at `jdbc:h2:./data/online_banking;MODE=MySQL;AUTO_SERVER=TRUE;LOCK_TIMEOUT=5000` with user `sa` and empty password.
2) Optionally tune pool values via `POOL_MAXIMUM_POOL_SIZE`, `POOL_CONNECTION_TIMEOUT_MS`, `POOL_IDLE_TIMEOUT_MS`, `POOL_MAX_LIFETIME_MS`.

## Run
```
mvn clean javafx:run
```

## Modules
- `com.onlinebanking.model` – domain entities.
- `com.onlinebanking.repository` – JDBC repositories for users, accounts, transactions, beneficiaries.
- `com.onlinebanking.service` – auth, accounts, transfers (atomic), bill pay, beneficiaries.
- `com.onlinebanking.controller` – JavaFX controllers for login, registration, and dashboard.

## Notes
- Authentication currently compares plain-text passwords; swap with hashing before production.
- Transactional transfer uses a single JDBC connection to keep updates and transaction logs atomic.
- Schema is auto-created on first run for H2.
- FXML views live under `src/main/resources/fxml`.
