package com.ecommerce.security.dto;

import com.ecommerce.security.model.Role;

import java.time.Instant;

public record CurrentUserResponse(
        Long userId,
        String email,
        Role role,
        Instant issuedAt,
        Instant expiresAt
) {
}