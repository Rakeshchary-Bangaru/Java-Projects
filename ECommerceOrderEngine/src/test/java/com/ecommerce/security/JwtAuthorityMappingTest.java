package com.ecommerce.security;

import com.ecommerce.security.config.JwtAuthenticationConfig;

import org.junit.jupiter.api.Test;

import org.springframework.security.core.Authentication;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class JwtAuthorityMappingTest {

    private final JwtAuthenticationConverter converter =
            new JwtAuthenticationConfig()
                    .jwtAuthenticationConverter();


    @Test
    void sellerAdminShouldReceiveSellerPermissions() {

        Authentication authentication =
                converter.convert(
                        jwtWithRole("SELLER_ADMIN")
                );

        Set<String> authorities =
                authorityNames(authentication);

        assertTrue(
                authorities.contains(
                        "ROLE_SELLER_ADMIN"
                )
        );

        assertTrue(
                authorities.contains(
                        "PRODUCT_CREATE"
                )
        );

        assertTrue(
                authorities.contains(
                        "PRODUCT_UPDATE_OWN"
                )
        );

        assertTrue(
                authorities.contains(
                        "INVENTORY_UPDATE_OWN"
                )
        );

        assertFalse(
                authorities.contains(
                        "ACCOUNT_SUSPEND"
                )
        );
    }


    @Test
    void itSupportShouldReceiveSupportPermissions() {

        Authentication authentication =
                converter.convert(
                        jwtWithRole("IT_SUPPORT")
                );

        Set<String> authorities =
                authorityNames(authentication);

        assertTrue(
                authorities.contains(
                        "ROLE_IT_SUPPORT"
                )
        );

        assertTrue(
                authorities.contains(
                        "ACCOUNT_SUSPEND"
                )
        );

        assertTrue(
                authorities.contains(
                        "ACCOUNT_DELETE_DORMANT"
                )
        );

        assertFalse(
                authorities.contains(
                        "PRODUCT_CREATE"
                )
        );
    }


    @Test
    void customerShouldReceiveOnlyCustomerPermissions() {

        Authentication authentication =
                converter.convert(
                        jwtWithRole("CUSTOMER")
                );

        Set<String> authorities =
                authorityNames(authentication);

        assertTrue(
                authorities.contains(
                        "ROLE_CUSTOMER"
                )
        );

        assertTrue(
                authorities.contains(
                        "ORDER_READ_OWN"
                )
        );

        assertFalse(
                authorities.contains(
                        "PRODUCT_CREATE"
                )
        );

        assertFalse(
                authorities.contains(
                        "ACCOUNT_SUSPEND"
                )
        );
    }


    @Test
    void rootShouldReceiveAllPermissions() {

        Authentication authentication =
                converter.convert(
                        jwtWithRole("ROOT")
                );

        Set<String> authorities =
                authorityNames(authentication);

        assertTrue(
                authorities.contains(
                        "ROLE_ROOT"
                )
        );

        assertTrue(
                authorities.contains(
                        "PRODUCT_CREATE"
                )
        );

        assertTrue(
                authorities.contains(
                        "ACCOUNT_SUSPEND"
                )
        );

        assertTrue(
                authorities.contains(
                        "ANALYTICS_READ_GLOBAL"
                )
        );

        assertTrue(
                authorities.contains(
                        "SYSTEM_ADMIN"
                )
        );
    }


    private Jwt jwtWithRole(String role) {

        Instant now = Instant.now();

        return Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("test@example.com")
                .issuedAt(now)
                .expiresAt(
                        now.plusSeconds(900)
                )
                .claim(
                        "userId",
                        1L
                )
                .claim(
                        "role",
                        role
                )
                .build();
    }


    private Set<String> authorityNames(
            Authentication authentication
    ) {

        assertNotNull(authentication);

        return authentication
                .getAuthorities()
                .stream()
                .map(
                        authority ->
                                authority.getAuthority()
                )
                .collect(
                        Collectors.toSet()
                );
    }
}