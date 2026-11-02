package com.ecommerce.security.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


class UserAccountTest {


    /*
     * =========================================================
     * ROLE CHANGE
     * =========================================================
     */
    @Test
    void shouldIncrementAuthVersionWhenRoleChanges() {

        UserAccount user =
                createCustomer();


        long originalVersion =
                user.getAuthVersion();


        user.changeRole(
                Role.SELLER_ADMIN
        );


        assertEquals(
                Role.SELLER_ADMIN,
                user.getRole()
        );

        assertEquals(
                originalVersion + 1,
                user.getAuthVersion()
        );
    }


    @Test
    void shouldNotIncrementAuthVersionWhenRoleDoesNotChange() {

        UserAccount user =
                createCustomer();


        long originalVersion =
                user.getAuthVersion();


        user.changeRole(
                Role.CUSTOMER
        );


        assertEquals(
                Role.CUSTOMER,
                user.getRole()
        );

        assertEquals(
                originalVersion,
                user.getAuthVersion()
        );
    }


    @Test
    void shouldRejectNullRole() {

        UserAccount user =
                createCustomer();


        long originalVersion =
                user.getAuthVersion();


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        user.changeRole(
                                null
                        )
        );


        assertEquals(
                Role.CUSTOMER,
                user.getRole()
        );

        assertEquals(
                originalVersion,
                user.getAuthVersion()
        );
    }


    /*
     * =========================================================
     * SUSPENSION
     * =========================================================
     */
    @Test
    void shouldIncrementAuthVersionWhenAccountIsSuspended() {

        UserAccount user =
                createCustomer();


        long originalVersion =
                user.getAuthVersion();


        user.suspend();


        assertEquals(
                AccountStatus.SUSPENDED,
                user.getStatus()
        );

        assertEquals(
                originalVersion + 1,
                user.getAuthVersion()
        );
    }


    @Test
    void shouldNotIncrementAuthVersionWhenAlreadySuspended() {

        UserAccount user =
                createCustomer();


        user.suspend();


        long versionAfterFirstSuspension =
                user.getAuthVersion();


        user.suspend();


        assertEquals(
                AccountStatus.SUSPENDED,
                user.getStatus()
        );

        assertEquals(
                versionAfterFirstSuspension,
                user.getAuthVersion()
        );
    }


    /*
     * =========================================================
     * REACTIVATION
     * =========================================================
     */
    @Test
    void shouldIncrementAuthVersionWhenAccountIsReactivated() {

        UserAccount user =
                createCustomer();


        user.suspend();


        long versionBeforeReactivation =
                user.getAuthVersion();


        user.reactivate();


        assertEquals(
                AccountStatus.ACTIVE,
                user.getStatus()
        );

        assertEquals(
                versionBeforeReactivation + 1,
                user.getAuthVersion()
        );
    }


    @Test
    void shouldNotIncrementAuthVersionWhenAlreadyActive() {

        UserAccount user =
                createCustomer();


        long originalVersion =
                user.getAuthVersion();


        user.reactivate();


        assertEquals(
                AccountStatus.ACTIVE,
                user.getStatus()
        );

        assertEquals(
                originalVersion,
                user.getAuthVersion()
        );
    }


    /*
     * =========================================================
     * DORMANCY
     * =========================================================
     */
    @Test
    void shouldIncrementAuthVersionWhenAccountBecomesDormant() {

        UserAccount user =
                createCustomer();


        long originalVersion =
                user.getAuthVersion();


        user.markDormant();


        assertEquals(
                AccountStatus.DORMANT,
                user.getStatus()
        );

        assertEquals(
                originalVersion + 1,
                user.getAuthVersion()
        );
    }


    @Test
    void shouldNotIncrementAuthVersionWhenAlreadyDormant() {

        UserAccount user =
                createCustomer();


        user.markDormant();


        long versionAfterDormancy =
                user.getAuthVersion();


        user.markDormant();


        assertEquals(
                AccountStatus.DORMANT,
                user.getStatus()
        );

        assertEquals(
                versionAfterDormancy,
                user.getAuthVersion()
        );
    }


    /*
     * =========================================================
     * DELETION PENDING
     * =========================================================
     */
    @Test
    void shouldIncrementAuthVersionWhenDormantAccountIsMarkedDeletionPending() {

        UserAccount user =
                createCustomer();


        user.markDormant();


        long versionBeforeDeletionPending =
                user.getAuthVersion();


        user.markDeletionPending();


        assertEquals(
                AccountStatus.DELETION_PENDING,
                user.getStatus()
        );

        assertEquals(
                versionBeforeDeletionPending + 1,
                user.getAuthVersion()
        );
    }


    @Test
    void shouldNotIncrementAuthVersionWhenAlreadyDeletionPending() {

        UserAccount user =
                createCustomer();


        user.markDormant();

        user.markDeletionPending();


        long versionAfterDeletionPending =
                user.getAuthVersion();


        user.markDeletionPending();


        assertEquals(
                AccountStatus.DELETION_PENDING,
                user.getStatus()
        );

        assertEquals(
                versionAfterDeletionPending,
                user.getAuthVersion()
        );
    }


    @Test
    void shouldRejectDeletionPendingWhenAccountIsNotDormant() {

        UserAccount user =
                createCustomer();


        long originalVersion =
                user.getAuthVersion();


        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        user::markDeletionPending
                );


        assertEquals(
                "Only dormant accounts can be marked for deletion",
                exception.getMessage()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                user.getStatus()
        );

        assertEquals(
                originalVersion,
                user.getAuthVersion()
        );
    }


    /*
     * =========================================================
     * MULTIPLE SECURITY CHANGES
     * ---------------------------------------------------------
     * Ensures each genuine transition increments exactly once.
     * =========================================================
     */
    @Test
    void shouldIncrementAuthVersionOnceForEachSecurityChange() {

        UserAccount user =
                createCustomer();


        assertEquals(
                1L,
                user.getAuthVersion()
        );


        user.changeRole(
                Role.SELLER_ADMIN
        );

        assertEquals(
                2L,
                user.getAuthVersion()
        );


        user.suspend();

        assertEquals(
                3L,
                user.getAuthVersion()
        );


        user.reactivate();

        assertEquals(
                4L,
                user.getAuthVersion()
        );


        user.markDormant();

        assertEquals(
                5L,
                user.getAuthVersion()
        );


        user.markDeletionPending();

        assertEquals(
                6L,
                user.getAuthVersion()
        );
    }


    /*
     * =========================================================
     * TEST FACTORY
     * =========================================================
     */
    private UserAccount createCustomer() {

        return new UserAccount(
                "customer@test.com",
                "encoded-password-hash",
                Role.CUSTOMER,
                AuthProvider.LOCAL
        );
    }
}