package com.onlinebanking.dto;

/**
 * DTO Pattern: user summary used for manager user-management UI.
 */
public record UserManagementDto(long id, String username, String role, boolean blocked) {
}
