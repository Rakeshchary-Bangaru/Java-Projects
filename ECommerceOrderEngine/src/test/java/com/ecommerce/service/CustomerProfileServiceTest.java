package com.ecommerce.service;

import com.ecommerce.model.Customer;

import com.ecommerce.repository.SpringDataCustomerRepository;

import com.ecommerce.security.model.UserAccount;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

import java.util.Optional;


class CustomerProfileServiceTest {

    private SpringDataCustomerRepository
            customerRepository;

    private CustomerIdGeneratorService
            customerIdGeneratorService;

    private CustomerProfileService
            customerProfileService;


    @BeforeEach
    void setUp() {

        customerRepository =
                mock(
                        SpringDataCustomerRepository.class
                );


        customerIdGeneratorService =
                mock(
                        CustomerIdGeneratorService.class
                );


        customerProfileService =
                new CustomerProfileService(
                        customerRepository,
                        customerIdGeneratorService
                );
    }


    /*
     * =========================================================
     * EXISTING LINKED CUSTOMER
     * ---------------------------------------------------------
     * If UserAccount already has a Customer profile,
     * return it without creating another one.
     * =========================================================
     */
    @Test
    void shouldReturnExistingLinkedCustomerProfile() {

        UserAccount user =
                mock(
                        UserAccount.class
                );


        Customer existingCustomer =
                new Customer(
                        1000000L,
                        "Rakesh Bangaru",
                        "rakesh@test.com"
                );


        when(user.getId())
                .thenReturn(
                        10L
                );


        when(
                customerRepository
                        .findByUserAccount_Id(
                                10L
                        )
        )
                .thenReturn(
                        Optional.of(
                                existingCustomer
                        )
                );


        Customer result =
                customerProfileService
                        .ensureCustomerProfile(
                                user,
                                "Rakesh Bangaru"
                        );


        assertSame(
                existingCustomer,
                result
        );


        verify(customerRepository)
                .findByUserAccount_Id(
                        10L
                );


        verify(
                customerRepository,
                never()
        )
                .findByEmail(
                        any()
                );


        verifyNoInteractions(
                customerIdGeneratorService
        );
    }


    /*
     * =========================================================
     * LINK EXISTING LEGACY CUSTOMER
     * ---------------------------------------------------------
     * Legacy customer exists with matching email but does not
     * yet have a UserAccount relationship.
     *
     * Reuse the same Customer rather than creating a duplicate.
     * =========================================================
     */
    @Test
    void shouldLinkExistingLegacyCustomerByEmail() {

        UserAccount user =
                mock(
                        UserAccount.class
                );


        Customer legacyCustomer =
                new Customer(
                        25L,
                        "Legacy Customer",
                        "legacy@test.com"
                );


        when(user.getId())
                .thenReturn(
                        20L
                );


        when(user.getEmail())
                .thenReturn(
                        "legacy@test.com"
                );


        when(
                customerRepository
                        .findByUserAccount_Id(
                                20L
                        )
        )
                .thenReturn(
                        Optional.empty()
                );


        when(
                customerRepository
                        .findByEmail(
                                "legacy@test.com"
                        )
        )
                .thenReturn(
                        Optional.of(
                                legacyCustomer
                        )
                );


        when(
                customerRepository.save(
                        legacyCustomer
                )
        )
                .thenReturn(
                        legacyCustomer
                );


        Customer result =
                customerProfileService
                        .ensureCustomerProfile(
                                user,
                                "Legacy Customer"
                        );


        assertSame(
                legacyCustomer,
                result
        );


        assertSame(
                user,
                legacyCustomer.getUserAccount()
        );


        verify(customerRepository)
                .save(
                        legacyCustomer
                );


        verifyNoInteractions(
                customerIdGeneratorService
        );
    }


    /*
     * =========================================================
     * CREATE NEW CUSTOMER PROFILE
     * ---------------------------------------------------------
     * No linked customer and no existing email customer.
     *
     * Generate a safe Customer ID and create the shopping
     * profile.
     * =========================================================
     */
    @Test
    void shouldCreateNewCustomerProfile() {

        UserAccount user =
                mock(
                        UserAccount.class
                );


        when(user.getId())
                .thenReturn(
                        30L
                );


        when(user.getEmail())
                .thenReturn(
                        "seller@test.com"
                );


        when(
                customerRepository
                        .findByUserAccount_Id(
                                30L
                        )
        )
                .thenReturn(
                        Optional.empty()
                );


        when(
                customerRepository
                        .findByEmail(
                                "seller@test.com"
                        )
        )
                .thenReturn(
                        Optional.empty()
                );


        when(
                customerIdGeneratorService
                        .generateCustomerId()
        )
                .thenReturn(
                        1000005L
                );


        when(
                customerRepository.save(
                        any(Customer.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );


        Customer result =
                customerProfileService
                        .ensureCustomerProfile(
                                user,
                                "Tech Store Seller"
                        );


        assertEquals(
                1000005L,
                result.getId()
        );


        assertEquals(
                "Tech Store Seller",
                result.getName()
        );


        assertEquals(
                "seller@test.com",
                result.getEmail()
        );


        assertSame(
                user,
                result.getUserAccount()
        );


        verify(customerIdGeneratorService)
                .generateCustomerId();


        verify(customerRepository)
                .save(
                        any(Customer.class)
                );
    }


    /*
     * =========================================================
     * CONFLICTING CUSTOMER EMAIL
     * ---------------------------------------------------------
     * Same customer email must not already belong to another
     * authenticated UserAccount.
     * =========================================================
     */
    @Test
    void shouldRejectCustomerLinkedToAnotherUserAccount() {

        UserAccount currentUser =
                mock(
                        UserAccount.class
                );


        UserAccount differentUser =
                mock(
                        UserAccount.class
                );


        Customer existingCustomer =
                new Customer(
                        50L,
                        "Existing Customer",
                        "shared@test.com"
                );


        existingCustomer.assignUserAccount(
                differentUser
        );


        when(currentUser.getId())
                .thenReturn(
                        40L
                );


        when(currentUser.getEmail())
                .thenReturn(
                        "shared@test.com"
                );


        when(differentUser.getId())
                .thenReturn(
                        99L
                );


        when(
                customerRepository
                        .findByUserAccount_Id(
                                40L
                        )
        )
                .thenReturn(
                        Optional.empty()
                );


        when(
                customerRepository
                        .findByEmail(
                                "shared@test.com"
                        )
        )
                .thenReturn(
                        Optional.of(
                                existingCustomer
                        )
                );


        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                customerProfileService
                                        .ensureCustomerProfile(
                                                currentUser,
                                                "Current User"
                                        )
                );


        assertEquals(
                "Customer email is already linked to another user account",
                exception.getMessage()
        );


        verifyNoInteractions(
                customerIdGeneratorService
        );


        verify(
                customerRepository,
                never()
        )
                .save(
                        any(Customer.class)
                );
    }


    /*
     * =========================================================
     * INVALID USER ACCOUNT
     * =========================================================
     */
    @Test
    void shouldRejectNullUserAccount() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                customerProfileService
                                        .ensureCustomerProfile(
                                                null,
                                                "Test User"
                                        )
                );


        assertEquals(
                "User account cannot be null",
                exception.getMessage()
        );


        verifyNoInteractions(
                customerRepository,
                customerIdGeneratorService
        );
    }
}