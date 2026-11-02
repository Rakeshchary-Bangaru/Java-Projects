package com.ecommerce.security.dto;

import com.ecommerce.security.model.AccountStatus;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;

import java.time.Instant;

public record UserAccountResponse(
        Long id,
        String email,
        Role role,
        AccountStatus status,
        AuthProvider authProvider,
        Instant lastLoginAt,
        Instant lastActivityAt,
        Instant createdAt,
        Instant updatedAt
) {
}