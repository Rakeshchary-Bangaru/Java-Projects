package com.ecommerce.security.dto;

import com.ecommerce.security.model.Role;

import java.time.Instant;

public record LoginResponse(

        String accessToken,
        String tokenType,

        Instant issuedAt,
        Instant expiresAt,

        Long userId,
        String email,
        Role role

) {
}