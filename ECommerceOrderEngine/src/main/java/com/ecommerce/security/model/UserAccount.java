
package com.ecommerce.security.model;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Locale;

@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_email",
                        columnNames = "email"
                ),
                @UniqueConstraint(
                        name = "uk_users_provider_identity",
                        columnNames = {
                                "auth_provider",
                                "provider_user_id"
                        }
                )
        }
)
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "email",
            nullable = false,
            length = 255
    )
    private String email;

    /*
     * Local accounts require a password hash.
     * Google OAuth2 accounts may have a null password hash.
     */
    @Column(
            name = "password_hash",
            length = 255
    )
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "role",
            nullable = false,
            length = 30
    )
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private AccountStatus status;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "auth_provider",
            nullable = false,
            length = 30
    )
    private AuthProvider authProvider;

    /*
     * Google's stable OIDC "sub" identifier.
     *
     * Must never be populated from unverified frontend input.
     */
    @Column(
            name = "provider_user_id",
            length = 255
    )
    private String providerUserId;

    @Column(name = "last_login_at")
    private Instant lastLoginAt;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private Instant updatedAt;

    /*
     * JWT authorization version.
     *
     * Increments when a security-sensitive account change
     * invalidates previously issued access tokens.
     */
    @Column(
            name = "auth_version",
            nullable = false
    )
    private long authVersion = 1L;

    protected UserAccount() {
        // Required by JPA
    }

    /*
     * =========================================================
     * ACCOUNT CONSTRUCTOR
     * =========================================================
     */
    public UserAccount(
            String email,
            String passwordHash,
            Role role,
            AuthProvider authProvider
    ) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Email cannot be empty"
            );
        }

        if (role == null) {
            throw new IllegalArgumentException(
                    "Role cannot be null"
            );
        }

        if (authProvider == null) {
            throw new IllegalArgumentException(
                    "Authentication provider cannot be null"
            );
        }

        if (
                authProvider == AuthProvider.LOCAL
                        && (
                        passwordHash == null
                                || passwordHash.isBlank()
                )
        ) {
            throw new IllegalArgumentException(
                    "Local accounts require a password hash"
            );
        }

        this.email =
                email.trim().toLowerCase(Locale.ROOT);

        this.passwordHash = passwordHash;
        this.role = role;
        this.authProvider = authProvider;
        this.status = AccountStatus.ACTIVE;
    }

    /*
     * =========================================================
     * JPA LIFECYCLE
     * =========================================================
     */
    @PrePersist
    protected void onCreate() {

        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }

    /*
     * =========================================================
     * LOGIN AND ACTIVITY TRACKING
     * =========================================================
     */
    public void recordLogin() {

        Instant now = Instant.now();

        this.lastLoginAt = now;
        this.lastActivityAt = now;
    }

    public void recordActivity() {
        this.lastActivityAt = Instant.now();
    }

    /*
     * =========================================================
     * GOOGLE OAUTH2 IDENTITY
     * =========================================================
     *
     * This method must be called only with a Google subject
     * obtained from a validated OAuth2/OIDC authentication.
     *
     * Once associated with a subject, this account cannot
     * be silently linked to a different Google identity.
     */
    public void linkGoogleIdentity(String googleSubject) {

        if (this.authProvider != AuthProvider.GOOGLE) {
            throw new IllegalStateException(
                    "Account is not a Google account"
            );
        }

        if (
                googleSubject == null
                        || googleSubject.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Google subject cannot be empty"
            );
        }

        String normalizedSubject = googleSubject.trim();

        if (normalizedSubject.length() > 255) {
            throw new IllegalArgumentException(
                    "Google subject exceeds maximum length"
            );
        }

        if (this.providerUserId != null) {

            if (!this.providerUserId.equals(normalizedSubject)) {
                throw new IllegalStateException(
                        "Google identity is already linked"
                );
            }

            // The same identity is already linked.
            return;
        }

        this.providerUserId = normalizedSubject;
    }

    /*
     * Backward-compatible setter.
     *
     * Retained for existing project callers.
     * Routes all Google identity changes through the
     * controlled linking method.
     *
     * An explicit unlink/relink workflow should be
     * implemented separately if ever required.
     */
    public void setProviderUserId(String providerUserId) {

        if (this.authProvider != AuthProvider.GOOGLE) {

            if (providerUserId != null) {
                throw new IllegalStateException(
                        "Provider identity cannot be assigned " +
                                "to a non-Google account"
                );
            }

            return;
        }

        linkGoogleIdentity(providerUserId);
    }

    /*
     * =========================================================
     * ROLE MANAGEMENT
     * =========================================================
     */
    public void changeRole(Role newRole) {

        if (newRole == null) {
            throw new IllegalArgumentException(
                    "Role cannot be null"
            );
        }

        if (this.role == newRole) {
            return;
        }

        this.role = newRole;

        invalidateExistingSessions();
    }

    /*
     * =========================================================
     * ACCOUNT STATUS MANAGEMENT
     * =========================================================
     */
    public void suspend() {

        if (this.status == AccountStatus.SUSPENDED) {
            return;
        }

        this.status = AccountStatus.SUSPENDED;

        invalidateExistingSessions();
    }

    public void markDormant() {

        if (this.status == AccountStatus.DORMANT) {
            return;
        }

        this.status = AccountStatus.DORMANT;

        invalidateExistingSessions();
    }

    public void markDeletionPending() {

        if (this.status == AccountStatus.DELETION_PENDING) {
            return;
        }

        if (this.status != AccountStatus.DORMANT) {
            throw new IllegalStateException(
                    "Only dormant accounts can be marked for deletion"
            );
        }

        this.status = AccountStatus.DELETION_PENDING;

        invalidateExistingSessions();
    }

    public void reactivate() {

        if (this.status == AccountStatus.ACTIVE) {
            return;
        }

        this.status = AccountStatus.ACTIVE;

        invalidateExistingSessions();
    }

    /*
     * =========================================================
     * JWT SESSION INVALIDATION
     * =========================================================
     */
    public void invalidateExistingSessions() {

        if (authVersion == Long.MAX_VALUE) {
            throw new IllegalStateException(
                    "Authorization version cannot be incremented"
            );
        }

        authVersion++;
    }

    /*
     * =========================================================
     * GETTERS
     * =========================================================
     */
    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public String getProviderUserId() {
        return providerUserId;
    }

    public Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public Instant getLastActivityAt() {
        return lastActivityAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getAuthVersion() {
        return authVersion;
    }
}
