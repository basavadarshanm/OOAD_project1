# Online Banking System - Quick Start Guide

## 1. Prerequisites

- Java 21+
- Maven 3.8+

## 2. Build and Run

```bash
# from OOAD_project1
mvn clean package
mvn javafx:run
```

## 3. Core Flows

### 3.1 Register and Login (Customer)

1. Open app.
2. Click Create account on Login screen.
3. Register username/password.
4. Login with newly created credentials.

### 3.2 Create Bank Account (New Customer)

1. On Dashboard, click Create Account.
2. Username is prefilled.
3. Account ID is auto-generated (exactly 8 digits, read-only).
4. Enter phone number (exactly 10 digits, unique).
5. Enter initial deposit.
6. Submit.

Expected:
- Account gets created.
- Create Account button disappears.
- User Details button is enabled.

### 3.3 Add Money

1. Click Add Money.
2. Enter positive amount.
3. Confirm.

Expected:
- Balance updates.
- DEPOSIT transaction recorded.

### 3.4 Transfer Money

Recipient input supports:
- 8-digit account ID, or
- 10-digit phone number

Steps:
1. Click Transfer Money.
2. Enter recipient account ID or phone number.
3. Enter amount and optional description.
4. Submit.

Expected:
- Transfer succeeds if recipient exists and funds are sufficient.
- Receipt shown.

### 3.5 Pay Bills

1. Click Pay Bills.
2. Enter biller name, reference, amount, due date.
3. Submit.

Expected:
- BILL_PAYMENT transaction and bill record created.
- Receipt shown.

### 3.6 Admin Login Mode

Default (if created in DB):
- Username: admin_user
- Password: admin123

Admin mode behavior:
- Shows all transactions globally.
- Create/Deposit/Transfer/Bill Pay actions disabled.
- No account creation prompt for admin.

## 4. H2 Database Access (Windows)

```powershell
$h2Jar = Join-Path $env:USERPROFILE ".m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar"
$dbPath = "C:/Users/<your-user>/Desktop/OOAD/final_project/OOAD_project1/data/online_banking"
java -cp "$h2Jar" org.h2.tools.Shell -url "jdbc:h2:file:$dbPath" -user "sa"
```

Useful queries:

```sql
SELECT id, username, role FROM users ORDER BY id;
SELECT account_number, phone_number, balance FROM accounts ORDER BY account_number;
SELECT id, from_account_id, to_account_id, transaction_type, amount, description, status, created_at
FROM transactions ORDER BY id DESC;
```

## 5. Reset Project Data

Close the app first, then run:

```powershell
Remove-Item .\data\online_banking.mv.db -Force -ErrorAction SilentlyContinue
Remove-Item .\data\online_banking.trace.db -Force -ErrorAction SilentlyContinue
mvn clean package
mvn javafx:run
```

## 6. Troubleshooting

### Error: Unknown lifecycle phase "packagemvn"

Cause: two Maven commands typed together.

Correct:

```bash
mvn clean package
mvn javafx:run
```

### Error: Database may be already in use

Cause: app or H2 shell still open.

Fix:
1. Close app window and H2 shell (exit).
2. Run `mvn javafx:run` again.

---

Version: 0.1.0-SNAPSHOT
Updated: April 2026
