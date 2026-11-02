package com.ecommerce.seller.model;

import com.ecommerce.security.model.UserAccount;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(
        name = "sellers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_sellers_user",
                        columnNames = "user_id"
                )
        }
)
public class Seller {

    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private Long id;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true
    )
    private UserAccount user;

    @Column(
            name = "business_name",
            nullable = false,
            length = 200
    )
    private String businessName;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "approval_status",
            nullable = false,
            length = 30
    )
    private SellerApprovalStatus approvalStatus;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "approved_by_user_id")
    private Long approvedByUserId;

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


    protected Seller() {
        // Required by JPA
    }


    public Seller(
            UserAccount user,
            String businessName
    ) {

        this.user =
                Objects.requireNonNull(
                        user,
                        "User account is required"
                );

        if (businessName == null ||
                businessName.isBlank()) {

            throw new IllegalArgumentException(
                    "Business name is required"
            );
        }

        this.businessName =
                businessName.trim();

        this.approvalStatus =
                SellerApprovalStatus.PENDING;
    }


    public void approve(Long rootUserId) {

        if (rootUserId == null) {
            throw new IllegalArgumentException(
                    "Approving ROOT user id is required"
            );
        }

        this.approvalStatus =
                SellerApprovalStatus.APPROVED;

        this.approvedByUserId =
                rootUserId;

        this.approvedAt =
                Instant.now();
    }


    public void reject() {

        this.approvalStatus =
                SellerApprovalStatus.REJECTED;

        this.approvedAt = null;
        this.approvedByUserId = null;
    }


    @PrePersist
    void prePersist() {

        Instant now = Instant.now();

        this.createdAt = now;
        this.updatedAt = now;

        if (approvalStatus == null) {
            approvalStatus =
                    SellerApprovalStatus.PENDING;
        }
    }


    @PreUpdate
    void preUpdate() {
        this.updatedAt =
                Instant.now();
    }


    public Long getId() {
        return id;
    }

    public UserAccount getUser() {
        return user;
    }

    public String getBusinessName() {
        return businessName;
    }

    public SellerApprovalStatus getApprovalStatus() {
        return approvalStatus;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public Long getApprovedByUserId() {
        return approvedByUserId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}