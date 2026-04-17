package com.onlinebanking.service;

import java.time.format.DateTimeFormatter;

import com.onlinebanking.model.Transaction;

/**
 * Generates transaction receipts
 */
public class ReceiptService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateTransactionReceipt(Transaction transaction, String fromAccountNumber, String toAccountNumber) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("\n");
        receipt.append("========================================\n");
        receipt.append("         TRANSACTION RECEIPT           \n");
        receipt.append("========================================\n");
        receipt.append("Transaction ID: ").append(transaction.getId()).append("\n");
        receipt.append("Type: ").append(transaction.getTransactionType()).append("\n");
        receipt.append("Date & Time: ").append(transaction.getCreatedAt().format(DATE_FORMATTER)).append("\n");
        receipt.append("----------------------------------------\n");
        receipt.append("From Account: ").append(fromAccountNumber).append("\n");
        if (toAccountNumber != null) {
            receipt.append("To Account: ").append(toAccountNumber).append("\n");
        }
        receipt.append("Amount: Rs. ").append(String.format("%.2f", transaction.getAmount())).append("\n");
        receipt.append("Description: ").append(transaction.getDescription()).append("\n");
        receipt.append("Status: ").append(transaction.getStatus()).append("\n");
        receipt.append("----------------------------------------\n");
        receipt.append("    Thank you for your transaction    \n");
        receipt.append("========================================\n");

        return receipt.toString();
    }

    public String generateBillPaymentReceipt(String billerName, String billerReference, 
                                             String accountNumber, java.math.BigDecimal amount,
                                             String transactionId) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("\n");
        receipt.append("========================================\n");
        receipt.append("        BILL PAYMENT RECEIPT          \n");
        receipt.append("========================================\n");
        receipt.append("Transaction ID: ").append(transactionId).append("\n");
        receipt.append("Date & Time: ").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        receipt.append("----------------------------------------\n");
        receipt.append("Biller Name: ").append(billerName).append("\n");
        receipt.append("Bill Reference: ").append(billerReference).append("\n");
        receipt.append("From Account: ").append(accountNumber).append("\n");
        receipt.append("Amount Paid: Rs. ").append(String.format("%.2f", amount)).append("\n");
        receipt.append("Status: PAID\n");
        receipt.append("----------------------------------------\n");
        receipt.append("   Bill payment successful!          \n");
        receipt.append("========================================\n");

        return receipt.toString();
    }

    public String generateTransferReceipt(String fromAccountNumber, String toAccountNumber,
                                         String recipientName, java.math.BigDecimal amount) {
        StringBuilder receipt = new StringBuilder();
        receipt.append("\n");
        receipt.append("========================================\n");
        receipt.append("          TRANSFER RECEIPT            \n");
        receipt.append("========================================\n");
        receipt.append("Date & Time: ").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        receipt.append("----------------------------------------\n");
        receipt.append("From Account: ").append(fromAccountNumber).append("\n");
        receipt.append("To Account: ").append(toAccountNumber).append("\n");
        receipt.append("Recipient: ").append(recipientName).append("\n");
        receipt.append("Amount Transferred: Rs. ").append(String.format("%.2f", amount)).append("\n");
        receipt.append("Status: SUCCESS\n");
        receipt.append("----------------------------------------\n");
        receipt.append("   Transfer completed successfully   \n");
        receipt.append("========================================\n");

        return receipt.toString();
    }
}
