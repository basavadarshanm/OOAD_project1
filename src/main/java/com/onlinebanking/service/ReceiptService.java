package com.onlinebanking.service;

import java.time.format.DateTimeFormatter;

import com.onlinebanking.builder.ReceiptBuilder;
import com.onlinebanking.dto.ReceiptDto;
import com.onlinebanking.model.Transaction;

/**
 * Builder Pattern + DTO Pattern: constructs display receipts for UI safely.
 */
public class ReceiptService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String generateTransactionReceipt(Transaction transaction, String fromAccountNumber, String toAccountNumber) {
        return generateTransactionReceiptDto(transaction, fromAccountNumber, toAccountNumber).body();
    }

    public ReceiptDto generateTransactionReceiptDto(Transaction transaction, String fromAccountNumber, String toAccountNumber) {
        ReceiptBuilder builder = new ReceiptBuilder()
                .separator()
                .line("         TRANSACTION RECEIPT           ")
                .separator()
                .line("Transaction ID: " + transaction.getId())
                .line("Type: " + transaction.getTransactionType())
                .line("Date & Time: " + transaction.getCreatedAt().format(DATE_FORMATTER))
                .dashedSeparator()
                .line("From Account: " + fromAccountNumber);

        if (toAccountNumber != null) {
            builder.line("To Account: " + toAccountNumber);
        }

        builder.line("Amount: Rs. " + String.format("%.2f", transaction.getAmount()))
                .line("Description: " + transaction.getDescription())
                .line("Status: " + transaction.getStatus())
                .dashedSeparator()
                .line("    Thank you for your transaction    ")
                .separator();

        return new ReceiptDto("TRANSACTION", builder.build());
    }

    public String generateBillPaymentReceipt(String billerName, String billerReference, 
                                             String accountNumber, java.math.BigDecimal amount,
                                             String transactionId) {
        return generateBillPaymentReceiptDto(billerName, billerReference, accountNumber, amount, transactionId).body();
    }

    public ReceiptDto generateBillPaymentReceiptDto(String billerName, String billerReference,
                                                    String accountNumber, java.math.BigDecimal amount,
                                                    String transactionId) {
        String body = new ReceiptBuilder()
                .separator()
                .line("        BILL PAYMENT RECEIPT          ")
                .separator()
                .line("Transaction ID: " + transactionId)
                .line("Date & Time: " + java.time.LocalDateTime.now().format(DATE_FORMATTER))
                .dashedSeparator()
                .line("Biller Name: " + billerName)
                .line("Bill Reference: " + billerReference)
                .line("From Account: " + accountNumber)
                .line("Amount Paid: Rs. " + String.format("%.2f", amount))
                .line("Status: PAID")
                .dashedSeparator()
                .line("   Bill payment successful!          ")
                .separator()
                .build();

        return new ReceiptDto("BILL_PAYMENT", body);
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
