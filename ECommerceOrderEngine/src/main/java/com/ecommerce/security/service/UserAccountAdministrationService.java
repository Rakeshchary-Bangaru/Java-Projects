package com.ecommerce.security.service;

import com.ecommerce.security.dto.UserAccountResponse;
import com.ecommerce.security.model.AccountStatus;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class UserAccountAdministrationService {

    private final UserAccountRepository userAccountRepository;


    public UserAccountAdministrationService(
            UserAccountRepository userAccountRepository
    ) {

        this.userAccountRepository =
                userAccountRepository;
    }


    /*
     * =========================================================
     * LIST USERS
     * =========================================================
     */
    @PreAuthorize(
            "hasAnyRole('ROOT', 'IT_SUPPORT')"
    )
    @Transactional(readOnly = true)
    public List<UserAccountResponse> findAllUsers() {

        return userAccountRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }


    /*
     * =========================================================
     * CHANGE ROLE
     * ---------------------------------------------------------
     * UserAccount.changeRole() is responsible for:
     *
     * - detecting whether the role actually changed
     * - updating the role
     * - invalidating existing JWT sessions exactly once
     *
     * Therefore this service must NOT separately increment
     * authVersion.
     * =========================================================
     */
    @PreAuthorize(
            "hasAnyRole('ROOT', 'IT_SUPPORT') && " +
                    "hasAuthority('ROLE_MANAGE')"
    )
    @Transactional
    public UserAccountResponse changeRole(
            Long userId,
            Role newRole
    ) {

        if (userId == null) {

            throw new IllegalArgumentException(
                    "User ID cannot be null"
            );
        }


        if (newRole == null) {

            throw new IllegalArgumentException(
                    "Role cannot be null"
            );
        }


        UserAccount user =
                findUserOrThrow(
                        userId
                );


        /*
         * Entity handles:
         *
         * CUSTOMER -> SELLER_ADMIN = increment
         * ROOT -> CUSTOMER         = increment
         * CUSTOMER -> CUSTOMER     = no increment
         */
        user.changeRole(
                newRole
        );


        UserAccount savedUser =
                userAccountRepository
                        .save(
                                user
                        );


        return toResponse(
                savedUser
        );
    }


    /*
     * =========================================================
     * CHANGE ACCOUNT STATUS
     * ---------------------------------------------------------
     * Only ACTIVE and SUSPENDED may be changed directly here.
     *
     * Dormancy and deletion use their dedicated workflows.
     *
     * UserAccount handles authVersion invalidation internally.
     * =========================================================
     */
    @PreAuthorize(
            "hasAnyRole('ROOT', 'IT_SUPPORT') && " +
                    "hasAuthority('ACCOUNT_SUSPEND')"
    )
    @Transactional
    public UserAccountResponse changeStatus(
            Long userId,
            AccountStatus newStatus
    ) {

        if (userId == null) {

            throw new IllegalArgumentException(
                    "User ID cannot be null"
            );
        }


        if (newStatus == null) {

            throw new IllegalArgumentException(
                    "Account status cannot be null"
            );
        }


        UserAccount user =
                findUserOrThrow(
                        userId
                );


        switch (newStatus) {

            case ACTIVE ->
                    user.reactivate();

            case SUSPENDED ->
                    user.suspend();

            default ->
                    throw new IllegalArgumentException(
                            "Status cannot be changed directly to "
                                    + newStatus
                    );
        }


        UserAccount savedUser =
                userAccountRepository
                        .save(
                                user
                        );


        return toResponse(
                savedUser
        );
    }


    /*
     * =========================================================
     * MARK DORMANT ACCOUNT FOR DELETION
     * ---------------------------------------------------------
     * UserAccount enforces:
     *
     * only DORMANT -> DELETION_PENDING
     *
     * and invalidates existing sessions exactly once.
     * =========================================================
     */
    @PreAuthorize(
            "hasAnyRole('ROOT', 'IT_SUPPORT') && " +
                    "hasAuthority('ACCOUNT_DELETE_DORMANT')"
    )
    @Transactional
    public UserAccountResponse markDeletionPending(
            Long userId
    ) {

        if (userId == null) {

            throw new IllegalArgumentException(
                    "User ID cannot be null"
            );
        }


        UserAccount user =
                findUserOrThrow(
                        userId
                );


        user.markDeletionPending();


        UserAccount savedUser =
                userAccountRepository
                        .save(
                                user
                        );


        return toResponse(
                savedUser
        );
    }


    /*
     * =========================================================
     * COMMON USER LOOKUP
     * =========================================================
     */
    private UserAccount findUserOrThrow(
            Long userId
    ) {

        return userAccountRepository
                .findById(
                        userId
                )
                .orElseThrow(
                        () ->
                                new IllegalArgumentException(
                                        "User account not found: "
                                                + userId
                                )
                );
    }


    /*
     * =========================================================
     * RESPONSE MAPPING
     * =========================================================
     */
    private UserAccountResponse toResponse(
            UserAccount user
    ) {

        return new UserAccountResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getAuthProvider(),
                user.getLastLoginAt(),
                user.getLastActivityAt(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}