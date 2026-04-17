# Online Banking System 🏦

A simple yet functional JavaFX-based online banking desktop application for managing customer accounts, transfers, and bill payments.

## Overview

This is a **mini-project** demonstrating:
- ✅ JavaFX UI with FXML
- ✅ JDBC with HikariCP connection pooling
- ✅ H2 embedded database
- ✅ Clean architecture (Model-View-Controller-Service-Repository)
- ✅ Atomic database transactions
- ✅ Simple dependency injection
- ✅ Meaningful error handling

## Quick Features

### For Customers
- 📝 Register new account
- 🔐 Login/Logout securely
- 💰 Check account balance
- 💸 Transfer money between accounts
- 📄 Pay bills online
- 📋 View transaction history
- 🧾 Generate transaction receipts

## Tech Stack

| Component | Technology |
|-----------|------------|
| UI Framework | JavaFX 21 |
| Database | H2 (embedded) |
| Connection Pool | HikariCP 5.1.0 |
| Build Tool | Maven 3.8+ |
| Java Version | 21+ |

## Quick Start

### Prerequisites
- Java 21+ installed
- Maven 3.8+ installed

### Build & Run
```bash
# Build
mvn clean package

# Run
mvn javafx:run
```

### Default Test Accounts
- Username: `john_doe` | Password: `password123` | Account: 1001001
- Username: `jane_smith` | Password: `password123` | Account: 1001002
- Username: `bob_johnson` | Password: `password123` | Account: 1001003

## Features

### 1. Authentication
- ✅ User registration
- ✅ Secure login
- ✅ Session management

### 2. Accounts & Balance
- ✅ View account details
- ✅ Check real-time balance
- ✅ Account statements

### 3. Money Transfer
- ✅ Intra-bank transfers
- ✅ Balance validation
- ✅ Transaction receipt
- ✅ Atomic transactions

### 4. Bill Payments
- ✅ Pay bills to billers
- ✅ Track bill history
- ✅ Generate payment receipts

### 5. Transaction History
- ✅ View recent transactions
- ✅ Transaction details
- ✅ Chronological ordering

## Project Structure

```
src/
├── main/java/com/onlinebanking/
│   ├── App.java                      (Entry point)
│   ├── model/                        (Domain objects)
│   │   ├── User.java
│   │   ├── Account.java
│   │   ├── Transaction.java
│   │   ├── BillPayment.java
│   │   └── Beneficiary.java
│   ├── repository/                   (Data access)
│   │   ├── UserRepository.java
│   │   ├── AccountRepository.java
│   │   ├── TransactionRepository.java
│   │   └── jdbc/ (Implementations)
│   ├── service/                      (Business logic)
│   │   ├── AuthService.java
│   │   ├── AccountService.java
│   │   ├── TransferService.java
│   │   ├── BillPayService.java
│   │   └── ReceiptService.java
│   ├── controller/                   (UI handlers)
│   │   ├── LoginController.java
│   │   ├── RegisterController.java
│   │   ├── DashboardController.java
│   │   ├── TransferMoneyController.java
│   │   └── PayBillsController.java
│   ├── config/
│   │   └── ApplicationContext.java   (DI container)
│   └── util/
│       └── DataSourceFactory.java
└── resources/
    ├── fxml/                         (UI definitions)
    │   ├── login.fxml
    │   ├── register.fxml
    │   ├── dashboard.fxml
    │   ├── transfer.fxml
    │   └── billpay.fxml
    └── application.properties
```

## Database

### Tables
- **users**: Customer and manager credentials
- **accounts**: Account details and balances  
- **transactions**: Transfer and payment records
- **bill_payments**: Bill payment history
- **beneficiaries**: Saved payee information

### Configuration
- Type: H2 embedded
- File: `./data/online_banking.mv.db`
- Connection Pool: HikariCP (10 connections)
- Auto-create schema on first run

### Connection Details
```properties
db.url=jdbc:h2:./data/online_banking;MODE=MySQL;AUTO_SERVER=TRUE
db.user=sa
db.password=
pool.maximumPoolSize=10
```

## Usage Examples

### Transfer Money
```
1. Login as john_doe
2. Dashboard → Transfer Money
3. Enter recipient: 1001002
4. Enter amount: 500
5. Click Transfer
6. View receipt with transaction ID
```

### Pay Bill
```
1. Login as john_doe
2. Dashboard → Pay Bills
3. Biller: Electricity Company
4. Reference: ELEC123456
5. Amount: 250
6. Due Date: 2026-04-25
7. Click Pay Bill
8. Receipt generated automatically
```

### View Transactions
```
1. After login, dashboard shows recent 5 transactions
2. Each shows: Date, Type, Amount, Status
3. Automatically refreshes after actions
```

## Architecture

### Clean Separation
```
UI Layer (Controllers)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database Layer (H2 + SQL)
```

### Key Patterns
- **Repository Pattern**: Abstract data access
- **Service Layer**: Centralized business logic
- **Dependency Injection**: Simple manual wiring
- **Immutable Models**: Thread-safe objects
- **Atomic Transactions**: JDBC transaction management

## Error Handling

Comprehensive error handling for:
- Invalid credentials
- Duplicate usernames
- Insufficient funds
- Account not found
- Invalid input (negative amounts, empty fields)
- Database errors
- UI constraints

## Security Notes ⚠️

**Learning Project** - For production, add:
- Password hashing (bcrypt/Argon2)
- HTTPS/TLS encryption
- Role-based access control
- Audit logging
- Input sanitization
- Two-factor authentication

## Troubleshooting

### Database Connection Failed
```
- Check data/ directory exists and is writable
- Delete data/online_banking.mv.db and restart
- Verify application.properties has correct db.url
```

### FXML Not Found
```
- Run: mvn clean compile
- Check FXML files in src/main/resources/fxml/
- Rebuild project
```

### Java Version Error
```
- Ensure Java 21+: java -version
- Update JAVA_HOME environment variable
- Rebuild: mvn clean package
```

## Building

```bash
# Compile
mvn clean compile

# Build JAR
mvn clean package

# Run tests
mvn test

# Run standalone
java -jar target/online-banking-desktop-0.1.0-SNAPSHOT.jar
```

## Configuration

### Change Initial Account Balance
Edit `schema.sql`:
```sql
INSERT INTO accounts VALUES (..., 5000.00)  -- Change amount
```

### Modify Database Connection
Edit `application.properties` or set env variables:
```
DB_URL=jdbc:h2:./data/online_banking
DB_USER=sa
POOL_MAXIMUM_POOL_SIZE=10
```

## Documentation

- **PROJECT_GUIDE.md** - Detailed architecture and features
- **QUICK_START.md** - Step-by-step usage guide
- **schema.sql** - Complete database schema

## Performance

- Max 10 concurrent DB connections (HikariCP)
- Last 20 transactions loaded per page
- Indexes on frequently queried columns
- Prepared statements for SQL injection prevention
- Lazy loading of data on-demand

## Future Enhancements

- [ ] Admin dashboard
- [ ] Advanced search/filters
- [ ] PDF statement export
- [ ] Recurring payments
- [ ] Transaction analytics
- [ ] Mobile app version
- [ ] REST API backend

## License

Learning project - Use freely for educational purposes.

## Author Notes

**Demonstrates:**
- ✅ Clean code & architecture
- ✅ JDBC best practices
- ✅ Transaction management
- ✅ JavaFX UI development
- ✅ Error handling
- ✅ Validation

**Intentionally Simple:**
- ❌ No Spring Framework
- ❌ No ORM (Hibernate)
- ❌ No microservices
- ❌ Manual DI (not CDI)

Goal: Readable, maintainable, student-level code.

---

**Status**: ✅ Fully Functional  
**Version**: 0.1.0-SNAPSHOT  
**Java**: 21+  
**Last Updated**: April 2026

For detailed guides, see `PROJECT_GUIDE.md` and `QUICK_START.md`

