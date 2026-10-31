package com.ecommerce.security.model;

import jakarta.persistence.*;

import java.time.Instant;

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
                        columnNames = {"auth_provider", "provider_user_id"}
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
     * Null is allowed because Google OAuth2 users
     * may not have a local password.
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
     * For Google OAuth2 this will eventually contain
     * Google's stable "sub" identifier.
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


    protected UserAccount() {
        // Required by JPA
    }


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

        if (authProvider == AuthProvider.LOCAL &&
                (passwordHash == null || passwordHash.isBlank())) {

            throw new IllegalArgumentException(
                    "Local accounts require a password hash"
            );
        }

        this.email = email.trim().toLowerCase();
        this.passwordHash = passwordHash;
        this.role = role;
        this.authProvider = authProvider;
        this.status = AccountStatus.ACTIVE;
    }


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


    public void recordLogin() {

        Instant now = Instant.now();

        this.lastLoginAt = now;
        this.lastActivityAt = now;
    }


    public void recordActivity() {
        this.lastActivityAt = Instant.now();
    }


    public void suspend() {
        this.status = AccountStatus.SUSPENDED;
    }


    public void markDormant() {
        this.status = AccountStatus.DORMANT;
    }


    public void markDeletionPending() {
        this.status = AccountStatus.DELETION_PENDING;
    }


    public void reactivate() {
        this.status = AccountStatus.ACTIVE;
    }


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


    public void setProviderUserId(String providerUserId) {
        this.providerUserId = providerUserId;
    }
}