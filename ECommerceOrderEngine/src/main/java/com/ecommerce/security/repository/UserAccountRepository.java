
package com.ecommerce.security.repository;

import com.ecommerce.security.model.AccountStatus;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.UserAccount;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface UserAccountRepository
        extends JpaRepository<UserAccount, Long> {

    /*
     * =========================================================
     * EMAIL LOOKUP
     * =========================================================
     */
    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    /*
     * =========================================================
     * GOOGLE OAUTH2 / PROVIDER IDENTITY LOOKUP
     * =========================================================
     *
     * Google authentication uses the stable OIDC "sub"
     * identifier rather than email as the account identity.
     *
     * This method can also support other identity providers
     * if they are added in the future.
     */
    Optional<UserAccount> findByAuthProviderAndProviderUserId(
            AuthProvider authProvider,
            String providerUserId
    );

    /*
     * =========================================================
     * ACCOUNT INACTIVITY DETECTION
     * =========================================================
     */
    @Query("""
        SELECT u
        FROM UserAccount u
        WHERE u.status = com.ecommerce.security.model.AccountStatus.ACTIVE
          AND (
                (u.lastActivityAt IS NOT NULL
                    AND u.lastActivityAt < :cutoff)
                OR
                (u.lastActivityAt IS NULL
                    AND u.createdAt < :cutoff)
              )
        """)
    List<UserAccount> findActiveAccountsInactiveSince(
            @Param("cutoff") Instant cutoff
    );
}
