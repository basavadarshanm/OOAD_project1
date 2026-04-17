# Online Banking System - Quick Start Guide

## Setup & Build

### Prerequisites
- Java 21+ installed
- Maven 3.8+
- H2 database (embedded, no separate installation needed)

### Build Instructions

```bash
# Navigate to project directory
cd "d:\SEM 6\OOAD\mini\Online_Banking_System"

# Clean and build
mvn clean package

# Or compile only (without packaging)
mvn clean compile
```

### Run Instructions

```bash
# Using Maven
mvn javafx:run

# Or using Java directly (after packaging)
java -jar target/online-banking-desktop-0.1.0-SNAPSHOT.jar
```

## Application Usage

### 1. Login Screen
- **Username**: john_doe, jane_smith, bob_johnson, or admin_user
- **Password**: password123 (for customers) or admin123 (for admin)
- Click "Create account" to register new user

### 2. Dashboard
After successful login, you'll see:
- Welcome message with your username
- Current account number and balance
- Recent transactions list
- Action buttons

### 3. Transfer Money
1. Click "Transfer Money" button
2. Enter recipient account number
3. Enter amount to transfer
4. Click "Transfer"
5. Receipt is displayed with transaction details
6. Balance updates automatically

**Recipient Accounts:**
- 1001001: john_doe
- 1001002: jane_smith
- 1001003: bob_johnson

### 4. Pay Bills
1. Click "Pay Bills" button
2. Enter biller name (e.g., "Electric Company")
3. Enter bill reference number
4. Enter amount
5. Select due date
6. Click "Pay Bill"
7. Receipt is displayed
8. Balance updates

### 5. View Transaction History
- All recent transactions are shown in the dashboard
- Transactions include: date, type, amount, status, and description

### 6. Logout
- Click "Logout" to return to login screen

## Sample Data

### Test Users
| Username | Password | Role | Account | Balance |
|----------|----------|------|---------|---------|
| john_doe | password123 | CUSTOMER | 1001001 | 10000.00 |
| jane_smith | password123 | CUSTOMER | 1001002 | 15000.00 |
| bob_johnson | password123 | CUSTOMER | 1001003 | 8500.00 |
| admin_user | admin123 | MANAGER | N/A | N/A |

## File Locations

### Database
- Location: `./data/online_banking` (relative to project root)
- Type: H2 embedded database file
- Auto-created on first run

### Configuration
- Database config: `src/main/resources/application.properties`
- FXML UI files: `src/main/resources/fxml/`

### Database Schema
- `schema.sql` - contains all CREATE TABLE statements and sample data

## Key Features Demo

### 1. Successful Transfer
```
From: 1001001 (john_doe) - Balance: 10000
To: 1001002 (jane_smith) - Balance: 15000
Amount: 500
Result: john_doe balance becomes 9500, jane_smith becomes 15500
```

### 2. Bill Payment
```
Account: 1001001
Biller: Electric Company
Reference: ELEC123456
Amount: 250
Due Date: 2026-04-25
Result: Balance deducted, receipt generated
```

### 3. Transaction History
```
Recent 5 transactions shown in dashboard
Each shows: Date, Type, Amount, Status, Description
```

## Error Messages & Handling

| Error | Cause | Solution |
|-------|-------|----------|
| "Invalid credentials" | Wrong username/password | Check credentials and try again |
| "Insufficient funds" | Balance < transfer amount | Request lower amount or check balance |
| "Account not found" | Recipient account doesn't exist | Verify recipient account number |
| "Fill all fields" | Missing required field | Complete all form fields |
| "Amount must be positive" | Amount <= 0 | Enter positive amount |

## Troubleshooting

### Database Issues
- If "Failed to connect to database" error:
  - Check `application.properties` for correct db.url
  - Ensure `./data` directory is writable
  - Try deleting `./data/online_banking.mv.db` and restart

### UI Issues
- If FXML files not found:
  - Ensure FXML files exist in `src/main/resources/fxml/`
  - Rebuild project: `mvn clean compile`

### Memory Issues
- Increase heap size: `java -Xmx512m -jar target/online-banking-desktop-0.1.0-SNAPSHOT.jar`

## Performance Tips

1. **First Run**: Database initialization takes a few seconds
2. **Transactions**: Complex queries cached at repository level
3. **UI**: Lists limited to last 20 transactions for performance

## Project Structure Quick Reference

```
controller/     - JavaFX controllers (handle UI logic)
model/          - Data models (User, Account, Transaction, etc.)
service/        - Business logic (Transfer, BillPay, etc.)
repository/     - Database access (JDBC queries)
config/         - Application configuration and DI
util/           - Utility classes
resources/
  ├── fxml/     - JavaFX UI definitions
  └── application.properties - Database config
```

## Common Test Cases

### Test 1: Basic Transfer
```
1. Login as john_doe (password123)
2. Click Transfer Money
3. Enter account: 1001002
4. Enter amount: 500
5. Click Transfer
Expected: Success message, receipt shown
```

### Test 2: Insufficient Balance
```
1. Login as bob_johnson
2. Click Transfer Money
3. Enter account: 1001001
4. Enter amount: 10000
5. Click Transfer
Expected: "Insufficient funds" error
```

### Test 3: Bill Payment
```
1. Login as john_doe
2. Click Pay Bills
3. Biller: Telephone Company
4. Reference: TELE999
5. Amount: 300
6. Due: pick any date
7. Click Pay Bill
Expected: Success message, receipt shown, balance updated
```

## Resetting Application

To reset and start fresh:
```bash
# Delete database file
rm -r data/online_banking.mv.db (on Linux/Mac)
del data\online_banking.mv.db (on Windows)

# Restart application
mvn javafx:run
# Database will be recreated with sample data
```

## Next Steps for Enhancement

1. **Admin Dashboard** - Implement manager panel
2. **Advanced Search** - Filter transactions by date/type
3. **Recurring Payments** - Schedule automatic payments
4. **Export Reports** - Generate PDF statements
5. **Two-Factor Auth** - Add security features

## Support & Documentation

- Project Guide: See `PROJECT_GUIDE.md`
- Database Schema: See `schema.sql`
- API Documentation: Code comments in source files

---

**Created**: April 2026  
**Version**: 0.1.0-SNAPSHOT  
**Java**: 21  
**Framework**: JavaFX 21
