# Online Banking System - Mini Project

## Project Summary
A simple JavaFX-based Online Banking desktop application with H2 embedded database. Supports customer registration, login, fund transfers, bill payments, and transaction history viewing.

## Architecture

### Project Structure
```
Online_Banking_System/
├── pom.xml (Maven configuration)
├── schema.sql (Database initialization)
├── src/
│   ├── main/
│   │   ├── java/com/onlinebanking/
│   │   │   ├── App.java (Main entry point)
│   │   │   ├── config/
│   │   │   │   └── ApplicationContext.java (Dependency injection)
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Account.java
│   │   │   │   ├── Transaction.java
│   │   │   │   ├── BillPayment.java
│   │   │   │   ├── Manager.java
│   │   │   │   └── Beneficiary.java
│   │   │   ├── repository/
│   │   │   │   ├── (Interfaces)
│   │   │   │   └── jdbc/
│   │   │   │       ├── JdbcUserRepository.java
│   │   │   │       ├── JdbcAccountRepository.java
│   │   │   │       ├── JdbcTransactionRepository.java
│   │   │   │       ├── JdbcBillPaymentRepository.java
│   │   │   │       ├── JdbcBeneficiaryRepository.java
│   │   │   ├── service/
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── AccountService.java
│   │   │   │   ├── TransferService.java
│   │   │   │   ├── BillPayService.java
│   │   │   │   ├── ManagerService.java
│   │   │   │   └── ReceiptService.java
│   │   │   ├── controller/
│   │   │   │   ├── LoginController.java
│   │   │   │   ├── RegisterController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   ├── TransferMoneyController.java
│   │   │   │   └── PayBillsController.java
│   │   │   └── util/
│   │   │       └── DataSourceFactory.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── fxml/
│   │           ├── login.fxml
│   │           ├── register.fxml
│   │           ├── dashboard.fxml
│   │           ├── transfer.fxml
│   │           └── billpay.fxml
```

## Key Features Implemented

### 1. Authentication
- **Register**: Create new customer account
- **Login**: Authentication for both customers and managers
- **Logout**: Session management

### 2. Customer Features
- **Check Balance**: View account balance
- **Transfer Money**: Send money to other accounts
- **Pay Bills**: Pay bills to billers
- **View Transaction History**: See recent transactions
- **Receipt Generation**: Automatic receipt after each transaction

### 3. Database
- **Users Table**: Stores customer and manager credentials
- **Accounts Table**: Stores account information and balance
- **Transactions Table**: Records all transfers and payments
- **Bill_Payments Table**: Records bill payment history
- **Beneficiaries Table**: Stores frequently used payees

## Technology Stack
- **UI Framework**: JavaFX 21
- **Database**: H2 (Embedded)
- **Connection Pooling**: HikariCP
- **Build Tool**: Maven
- **Java Version**: Java 21

## How It Works

### Transfer Money Flow
1. Customer clicks "Transfer Money" from dashboard
2. Enters recipient account number and amount
3. System validates:
   - Account exists
   - Sufficient balance
   - Valid amount
4. Transaction is recorded
5. Balance is updated atomically
6. Receipt is displayed

### Pay Bills Flow
1. Customer clicks "Pay Bills" from dashboard
2. Enters biller name, reference, amount, and due date
3. System validates:
   - Account exists
   - Sufficient balance
   - Valid amount
4. Payment is recorded
5. Bill payment record is created
6. Receipt is displayed

### Database Schema Highlights
- Users table has role column (CUSTOMER/MANAGER)
- Accounts table links to users with unique account number
- Transactions table supports different types: TRANSFER, BILL_PAYMENT
- H2 database runs in memory (file-based in ./data/online_banking)
- Automatic indexes on frequently queried columns

## Services Architecture

### AuthService
- Handles login/registration
- User validation

### AccountService
- Retrieves user accounts
- Fetches recent transactions

### TransferService
- Validates transfer eligibility
- Performs atomic transfer with JDBC transactions
- Creates transaction record

### BillPayService
- Validates bill payment
- Deducts amount from account
- Records bill payment

### ReceiptService
- Generates formatted transaction receipts
- Generates bill payment receipts

### ManagerService
- View all customers
- Block/unblock users
- View all transactions
- Create new users

## Key Design Decisions

1. **Immutable Models**: All model classes use final fields (no setters)
2. **Repository Pattern**: JDBC repositories with interfaces for loose coupling
3. **Service Layer**: Business logic separated from controllers
4. **Atomic Transactions**: Fund transfers use JDBC transactions
5. **Simple Dependency Injection**: Manual wiring in ApplicationContext
6. **No design patterns overuse**: Kept code simple and readable

## Running the Application

```bash
# Build
mvn clean package

# Run
mvn javafx:run

# Or
java -jar target/online-banking-desktop-0.1.0-SNAPSHOT.jar
```

## Sample Data
The schema.sql file includes sample data:
- Users: john_doe, jane_smith, bob_johnson (password: password123)
- Admin: admin_user (password: admin123)
- Sample transactions and beneficiaries

## Error Handling
- Input validation for all fields
- Balance verification before transfers
- Database error handling with meaningful messages
- User-friendly error messages in UI

## Security Notes
This is a learning project. Production use would require:
- Password hashing (bcrypt/argon2)
- Encrypted connections (HTTPS/TLS)
- Role-based access control (RBAC)
- Audit logging
- Rate limiting
- Two-factor authentication

## Future Enhancements
1. Admin dashboard with user management
2. Transaction filters and search
3. Statement generation
4. Fund deposit feature
5. Loan management
6. Card management
