package com.ecommerce.security.jwt;

import java.time.Instant;

public record IssuedToken(
        String tokenValue,
        Instant issuedAt,
        Instant expiresAt
) {
}