package com.onlinebanking.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO Pattern: bill payment payload passed from controller/service boundary.
 */
public record BillPaymentRequestDto(
        String fromAccountNumber,
        String billerName,
        BigDecimal amount,
        String billReference,
        LocalDate dueDate
) {
}
