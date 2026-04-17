package com.onlinebanking.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BillPayment {
    private final long id;
    private final long accountId;
    private final String billerName;
    private final BigDecimal amount;
    private final String billReferenceNumber;
    private final LocalDate dueDate;
    private final LocalDateTime paidDate;
    private final String status; // PENDING, PAID, OVERDUE
    private final Long transactionId; // nullable, links to transaction record

    public BillPayment(long id, long accountId, String billerName, BigDecimal amount,
                      String billReferenceNumber, LocalDate dueDate, LocalDateTime paidDate,
                      String status, Long transactionId) {
        this.id = id;
        this.accountId = accountId;
        this.billerName = billerName;
        this.amount = amount;
        this.billReferenceNumber = billReferenceNumber;
        this.dueDate = dueDate;
        this.paidDate = paidDate;
        this.status = status;
        this.transactionId = transactionId;
    }

    public long getId() {
        return id;
    }

    public long getAccountId() {
        return accountId;
    }

    public String getBillerName() {
        return billerName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getBillReferenceNumber() {
        return billReferenceNumber;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getPaidDate() {
        return paidDate;
    }

    public String getStatus() {
        return status;
    }

    public Long getTransactionId() {
        return transactionId;
    }

    @Override
    public String toString() {
        return String.format("%s | %s | Amount: %s | Status: %s | Due: %s",
                billerName,
                billReferenceNumber,
                amount,
                status,
                dueDate);
    }
}
