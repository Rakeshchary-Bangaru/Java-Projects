package com.ecommerce.dto;

import com.ecommerce.security.model.AccountStatus;
import com.ecommerce.security.model.AuthProvider;

import java.time.Instant;


/*
 * =========================================================
 * CURRENT USER PROFILE
 * ---------------------------------------------------------
 * Safe user-facing account information.
 *
 * Deliberately excluded:
 *
 * - role
 * - permissions
 * - passwordHash
 * - providerUserId
 * =========================================================
 */
public record ProfileResponse(

        Long accountId,

        String displayName,

        String email,

        AccountStatus accountStatus,

        AuthProvider loginMethod,

        Instant memberSince,

        Instant lastLoginAt,

        Instant lastActivityAt,

        SellerProfileResponse sellerProfile

) {
}