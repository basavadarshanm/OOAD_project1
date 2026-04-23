-- Online Banking System Database Schema (H2 Database)
-- This script initializes all tables needed for the system

-- Users table (for both customers and managers)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    mpin_hash VARCHAR(4),
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'MANAGER')),
    is_blocked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Accounts table (each customer has an account)
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 10000.00,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Transactions table (records all transfers and payments)
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    from_account_id BIGINT NOT NULL,
    to_account_id BIGINT,
    transaction_type VARCHAR(20) NOT NULL CHECK (transaction_type IN ('TRANSFER', 'BILL_PAYMENT', 'DEPOSIT', 'WITHDRAWAL')),
    amount DECIMAL(15, 2) NOT NULL,
    description VARCHAR(255),
    status VARCHAR(20) NOT NULL DEFAULT 'COMPLETED' CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (from_account_id) REFERENCES accounts(id),
    FOREIGN KEY (to_account_id) REFERENCES accounts(id)
);

-- Beneficiaries table (saved payee accounts for faster transfers)
CREATE TABLE IF NOT EXISTS beneficiaries (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,
    account_number VARCHAR(50) NOT NULL,
    bank VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY unique_beneficiary (user_id, account_number)
);

-- Bill Payments table (tracks bill payment records)
CREATE TABLE IF NOT EXISTS bill_payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    biller_name VARCHAR(100) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    bill_reference_number VARCHAR(100),
    due_date DATE,
    paid_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'PAID' CHECK (status IN ('PENDING', 'PAID', 'OVERDUE')),
    transaction_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id),
    FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

-- Create indexes for faster queries
CREATE INDEX idx_user_id ON accounts(user_id);
CREATE INDEX idx_from_account ON transactions(from_account_id);
CREATE INDEX idx_to_account ON transactions(to_account_id);
CREATE INDEX idx_account_created ON transactions(created_at);
CREATE INDEX idx_user_beneficiary ON beneficiaries(user_id);
CREATE INDEX idx_account_bill_payments ON bill_payments(account_id);

-- Insert sample data
INSERT INTO users (username, password_hash, role, is_blocked) VALUES
('john_doe', 'password123', 'CUSTOMER', FALSE),
('jane_smith', 'password123', 'CUSTOMER', FALSE),
('bob_johnson', 'password123', 'CUSTOMER', FALSE),
('admin_user', 'admin123', 'MANAGER', FALSE);

-- Insert sample accounts
INSERT INTO accounts (user_id, account_number, balance) VALUES
((SELECT id FROM users WHERE username = 'john_doe'), '1001001', 10000.00),
((SELECT id FROM users WHERE username = 'jane_smith'), '1001002', 15000.00),
((SELECT id FROM users WHERE username = 'bob_johnson'), '1001003', 8500.00);

-- Insert sample transactions
INSERT INTO transactions (from_account_id, to_account_id, transaction_type, amount, description, status) VALUES
((SELECT id FROM accounts WHERE account_number = '1001001'), (SELECT id FROM accounts WHERE account_number = '1001002'), 'TRANSFER', 500.00, 'Payment to Jane', 'COMPLETED'),
((SELECT id FROM accounts WHERE account_number = '1001002'), (SELECT id FROM accounts WHERE account_number = '1001003'), 'TRANSFER', 1000.00, 'Payment to Bob', 'COMPLETED');

-- Insert sample beneficiaries
INSERT INTO beneficiaries (user_id, name, account_number, bank) VALUES
((SELECT id FROM users WHERE username = 'john_doe'), 'Jane Smith', '1001002', 'Bank ABC'),
((SELECT id FROM users WHERE username = 'john_doe'), 'Bob Johnson', '1001003', 'Bank ABC');

-- Insert sample bill payments
INSERT INTO bill_payments (account_id, biller_name, amount, bill_reference_number, due_date, status, transaction_id) VALUES
((SELECT id FROM accounts WHERE account_number = '1001001'), 'Electric Company', 250.00, 'ELEC123456', '2026-04-25', 'PAID', (SELECT id FROM transactions WHERE description = 'Payment to Jane' LIMIT 1));
