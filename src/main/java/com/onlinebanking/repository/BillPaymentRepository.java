package com.onlinebanking.repository;

import java.util.List;
import java.util.Optional;

import com.onlinebanking.model.BillPayment;

public interface BillPaymentRepository {
    List<BillPayment> findByAccountId(long accountId);
    Optional<BillPayment> findById(long id);
    BillPayment create(long accountId, String billerName, java.math.BigDecimal amount,
                      String billReferenceNumber, java.time.LocalDate dueDate, long transactionId);
    void updateStatus(long billPaymentId, String status);
}
