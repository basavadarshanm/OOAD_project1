# Online Banking Test Inputs and Validation Guide

This file gives ready-to-use inputs for positive and negative testing of the current project.

## 1. Pre-Run Checklist

1. Build project:

```bash
mvn clean package
```

2. Run app:

```bash
mvn javafx:run
```

3. Ensure database file exists after first launch:

- data/online_banking.mv.db

## 2. Test Users and Accounts

| Username | Password | Role | Account Number | Initial Balance |
|---|---|---|---|---|
| john_doe | password123 | CUSTOMER | 1001001 | 10000.00 |
| jane_smith | password123 | CUSTOMER | 1001002 | 15000.00 |
| bob_johnson | password123 | CUSTOMER | 1001003 | 8500.00 |
| admin_user | admin123 | MANAGER | N/A | N/A |

Note: Transfer and Pay Bills require a CUSTOMER account. The MANAGER user admin_user has no linked account and cannot perform these operations.

## 3. Login Test Cases

### Valid

1. Input: john_doe / password123
2. Expected: Dashboard opens, account and balance visible.

### Invalid

1. Input: john_doe / wrongpass
2. Expected: Invalid credentials.

1. Input: unknown_user / password123
2. Expected: Invalid credentials.

## 4. Registration Test Cases

### Valid

1. Username: new_user_01
2. Password: pass123
3. Confirm: pass123
4. Expected: Registration successful, redirected to Login with success info.

### Invalid

1. Empty username
2. Expected: Username is required.

1. Empty password
2. Expected: Password is required.

1. Password: pass123, Confirm: pass124
2. Expected: Passwords do not match.

1. Username already used (for example john_doe)
2. Expected: User already exists or similar duplicate user message.

## 5. Transfer Money Test Cases

Login as john_doe first.

Recipient can be either:
- 8-digit Account ID
- 10-digit Phone Number

### Valid transfer

1. Recipient account: 1001002
2. Amount: 500
3. Expected UI:
- Success message.
- Receipt shown.
- Input fields cleared.

4. Expected DB:
- New row in transactions with transaction_type = TRANSFER.
- Sender balance decreases by 500.
- Recipient balance increases by 500.

### Invalid transfer inputs

1. Recipient account empty
2. Amount: 100
3. Expected: Enter recipient account ID or phone number.

1. Recipient: 12345
2. Amount: 100
3. Expected: Recipient must be 8-digit account ID or 10-digit phone number.

1. Recipient: 1001002
2. Amount: 0
3. Expected: Amount must be positive.

1. Recipient: 1001002
2. Amount: -10
3. Expected: Amount must be positive.

1. Recipient: 9999999 (non-existing)
2. Amount: 100
3. Expected: Account not found style error.

1. Recipient: 1001002
2. Amount: 99999999
3. Expected: Insufficient funds style error.

## 5A. Create Account Test Cases

Use this when dashboard shows Account: Not available.

### Valid account creation

1. Login with a user that has no account.
2. Click Create Account (opens dedicated account creation section).
3. Verify Username is auto-filled and read-only.
4. Verify Account ID is auto-generated and read-only (8 digits).
5. Enter Phone Number: 9876543210
6. Enter Initial amount: 2500
7. Click Create Account.
4. Expected:
- Account number is generated.
- Balance shows Rs. 2500.00.
- Transfer/Pay Bills/Add Money buttons become enabled.
- A DEPOSIT transaction appears as Initial account funding.
- Back returns to Dashboard.

### Invalid account creation

1. Phone Number: 98765
2. Initial amount: 1000
3. Expected: Phone number must be exactly 10 digits.

1. Phone Number already linked to another user account
2. Expected: Phone number is already linked to another account.

1. Initial amount: -100
2. Expected: Initial amount cannot be negative.

1. Try Create Account again for same user after account already exists.
2. Expected: Account already exists message.

## 5B. Add Money (Deposit) Test Cases

### Valid deposit

1. Click Add Money.
2. Amount: 1500
3. Expected:
- New balance increases by 1500.
- DEPOSIT transaction added with description Cash deposit.

### Invalid deposit

1. Amount: 0
2. Expected: Amount must be positive.

1. Amount: -75
2. Expected: Amount must be positive.

1. Amount: abc
2. Expected: Invalid amount format message.

## 6. Pay Bills Test Cases

Login as john_doe first, then go to Pay Bills.

### Valid bill payment

1. Biller Name: Electric Company
2. Bill Reference: ELEC2026A01
3. Amount: 250
4. Due Date: any future date (for example 2026-04-25)
5. Expected UI:
- Bill paid successfully.
- Receipt shown in text area.
- Fields cleared.
- Balance label reduced by amount.

6. Expected DB:
- One row inserted in bill_payments.
- One row inserted in transactions with transaction_type = BILL_PAYMENT.
- Linked transaction_id in bill_payments is not null.
- Account balance reduced by 250.

### Invalid bill payment inputs

1. Leave Biller Name blank
2. Expected: Fill all fields.

1. Leave Bill Reference blank
2. Expected: Fill all fields.

1. Leave Due Date blank
2. Expected: Fill all fields.

1. Amount: 0
2. Expected: Amount must be positive.

1. Amount: -5
2. Expected: Amount must be positive.

1. Amount: 99999999
2. Expected: Insufficient funds for bill payment.

## 7. Back Button and Navigation Test Cases

1. Login -> Dashboard -> Pay Bills -> click Back
2. Expected: Returns to Dashboard without crash.

1. Login -> Dashboard -> Transfer Money -> click Back
2. Expected: Returns to Dashboard without crash.

1. Dashboard -> Logout
2. Expected: Returns to Login.

## 8. H2 Data Storage Verification

## Option A: H2 Shell (CLI)

Run this in project root on Windows PowerShell:

```powershell
java -cp "$env:USERPROFILE\.m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar" org.h2.tools.Shell -url "jdbc:h2:./data/online_banking" -user "sa"
```

If you accidentally connect to an empty DB, use an absolute file URL instead:

```powershell
$dbPath = "C:/Users/bhara/Desktop/OOAD/final_project/OOAD_project1/data/online_banking"
$h2Jar = Join-Path $env:USERPROFILE ".m2\repository\com\h2database\h2\2.2.224\h2-2.2.224.jar"
java -cp "$h2Jar" org.h2.tools.Shell -url "jdbc:h2:file:$dbPath" -user "sa"
```

Then run SQL checks below.

Important:
- Close the banking app before opening H2 Shell, otherwise DB file lock errors can occur.

### Cleanup invalid accounts (interactive SQL)

Run these 3 statements one-by-one inside H2 Shell:

```sql
DELETE FROM bill_payments
WHERE account_id IN (
	 SELECT id
	 FROM accounts
	 WHERE phone_number IS NULL
		 OR phone_number = ''
		 OR LENGTH(phone_number) <> 10
		 OR LENGTH(account_number) <> 8
);

DELETE FROM transactions
WHERE from_account_id IN (
	 SELECT id
	 FROM accounts
	 WHERE phone_number IS NULL
		 OR phone_number = ''
		 OR LENGTH(phone_number) <> 10
		 OR LENGTH(account_number) <> 8
)
OR to_account_id IN (
	 SELECT id
	 FROM accounts
	 WHERE phone_number IS NULL
		 OR phone_number = ''
		 OR LENGTH(phone_number) <> 10
		 OR LENGTH(account_number) <> 8
);

DELETE FROM accounts
WHERE phone_number IS NULL
	OR phone_number = ''
	OR LENGTH(phone_number) <> 10
	OR LENGTH(account_number) <> 8;
```

Exit shell:

```sql
exit
```

## Option B: Any DB tool that supports H2

Connect with:

- JDBC URL: jdbc:h2:./data/online_banking
- User: sa
- Password: (empty)

## SQL checks

### Check accounts and balances

```sql
SELECT account_number, balance
FROM accounts
ORDER BY account_number;
```

### Check account phone mappings

```sql
SELECT account_number, phone_number, balance
FROM accounts
ORDER BY account_number;
```

### Latest transfers and bill payments

```sql
SELECT id, transaction_type, amount, description, status, created_at
FROM transactions
ORDER BY id DESC
LIMIT 20;
```

### Latest bill payments

```sql
SELECT id, account_id, biller_name, amount, bill_reference_number, due_date, status, transaction_id, paid_date
FROM bill_payments
ORDER BY id DESC
LIMIT 20;
```

### Join bill payments with account number

```sql
SELECT bp.id, a.account_number, bp.biller_name, bp.amount, bp.bill_reference_number, bp.due_date, bp.status, bp.transaction_id
FROM bill_payments bp
JOIN accounts a ON a.id = bp.account_id
ORDER BY bp.id DESC;
```

### Verify transaction linkage for bill payments

```sql
SELECT bp.id AS bill_payment_id, bp.transaction_id, t.transaction_type, t.amount, t.description
FROM bill_payments bp
LEFT JOIN transactions t ON t.id = bp.transaction_id
ORDER BY bp.id DESC;
```

## 9. Suggested Full Regression Run (Short)

1. Login valid.
2. Transfer valid.
3. Transfer invalid (negative amount).
4. Pay Bill valid.
5. Pay Bill invalid (empty fields).
6. Use Back buttons from Transfer and Pay Bills.
7. Logout.
8. Run SQL checks to validate persistence and balances.

## 10. Quick Reset for Fresh Test Data

```powershell
Remove-Item .\data\online_banking.mv.db -Force
mvn javafx:run
```

Database is recreated with sample data on restart.
