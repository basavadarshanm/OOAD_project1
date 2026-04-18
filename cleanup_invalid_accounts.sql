-- Remove accounts that do not meet new validation rules.
-- Rules: phone_number must be exactly 10 digits, account_number must be exactly 8 digits.

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
