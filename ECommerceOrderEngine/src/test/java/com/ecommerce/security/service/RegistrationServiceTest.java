package com.ecommerce.security.service;

import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;

import com.ecommerce.service.CustomerProfileService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;


class RegistrationServiceTest {

    private UserAccountRepository
            repository;

    private CustomerProfileService
            customerProfileService;

    private PasswordEncoder
            passwordEncoder;

    private RegistrationService
            registrationService;


    @BeforeEach
    void setUp() {

        repository =
                mock(
                        UserAccountRepository.class
                );


        customerProfileService =
                mock(
                        CustomerProfileService.class
                );


        passwordEncoder =
                new BCryptPasswordEncoder();


        registrationService =
                new RegistrationService(
                        repository,
                        customerProfileService,
                        passwordEncoder
                );
    }


    /*
     * =========================================================
     * SUCCESSFUL CUSTOMER REGISTRATION
     * ---------------------------------------------------------
     * Verifies:
     *
     * - email normalization
     * - BCrypt password hashing
     * - CUSTOMER role
     * - LOCAL authentication provider
     * - UserAccount persistence
     * - Customer profile creation is delegated to
     *   CustomerProfileService
     * =========================================================
     */
    @Test
    void shouldRegisterCustomerWithHashedPassword() {

        RegisterRequest request =
                new RegisterRequest(
                        "Rakesh Bangaru",
                        "test@email.com",
                        "Password123!",
                        "test-recaptcha-token"
                );


        when(
                repository.existsByEmail(
                        "test@email.com"
                )
        )
                .thenReturn(
                        false
                );


        /*
         * Return the same UserAccount that RegistrationService
         * sends to the repository.
         */
        when(
                repository.save(
                        any(UserAccount.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );


        UserAccount result =
                registrationService
                        .registerCustomer(
                                request
                        );


        /*
         * =====================================================
         * AUTHENTICATION ACCOUNT ASSERTIONS
         * =====================================================
         */

        assertNotNull(
                result
        );


        assertEquals(
                "test@email.com",
                result.getEmail()
        );


        assertEquals(
                Role.CUSTOMER,
                result.getRole()
        );


        assertEquals(
                AuthProvider.LOCAL,
                result.getAuthProvider()
        );


        /*
         * Raw password must never be stored.
         */
        assertNotEquals(
                "Password123!",
                result.getPasswordHash()
        );


        /*
         * Stored hash must still represent the supplied password.
         */
        assertTrue(
                passwordEncoder.matches(
                        "Password123!",
                        result.getPasswordHash()
                )
        );


        /*
         * =====================================================
         * USER ACCOUNT PERSISTENCE
         * =====================================================
         */

        verify(repository)
                .existsByEmail(
                        "test@email.com"
                );


        verify(repository)
                .save(
                        any(UserAccount.class)
                );


        /*
         * =====================================================
         * CUSTOMER PROFILE CREATION
         * -----------------------------------------------------
         * RegistrationService no longer creates Customer
         * directly.
         *
         * It delegates that responsibility to
         * CustomerProfileService.
         * =====================================================
         */

        verify(customerProfileService)
                .ensureCustomerProfile(
                        result,
                        "Rakesh Bangaru"
                );
    }


    /*
     * =========================================================
     * DUPLICATE ACCOUNT EMAIL
     * ---------------------------------------------------------
     * If authentication email already exists:
     *
     * - no UserAccount is saved
     * - no Customer profile is created
     * =========================================================
     */
    @Test
    void shouldRejectDuplicateEmail() {

        RegisterRequest request =
                new RegisterRequest(
                        "Rakesh Bangaru",
                        "user@email.com",
                        "Password123!",
                        "test-recaptcha-token"
                );


        when(
                repository.existsByEmail(
                        "user@email.com"
                )
        )
                .thenReturn(
                        true
                );


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                registrationService
                                        .registerCustomer(
                                                request
                                        )
                );


        assertEquals(
                "An account with this email already exists",
                exception.getMessage()
        );


        /*
         * UserAccount must not be created.
         */
        verify(
                repository,
                never()
        )
                .save(
                        any(UserAccount.class)
                );


        /*
         * Customer shopping profile must not be created.
         */
        verifyNoInteractions(
                customerProfileService
        );
    }
}