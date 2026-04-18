package com.onlinebanking.dto;

import java.math.BigDecimal;

/**
 * DTO Pattern: transfer payload passed from controller/service boundary.
 */
public record TransferRequestDto(
        String fromAccountNumber,
        String recipientIdentifier,
        BigDecimal amount,
        String description
) {
}
