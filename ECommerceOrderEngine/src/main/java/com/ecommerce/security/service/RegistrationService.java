package com.ecommerce.security.service;

import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;

import com.ecommerce.service.CustomerProfileService;

import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class RegistrationService {

    private final UserAccountRepository
            userAccountRepository;

    private final CustomerProfileService
            customerProfileService;

    private final PasswordEncoder
            passwordEncoder;


    public RegistrationService(
            UserAccountRepository userAccountRepository,
            CustomerProfileService customerProfileService,
            PasswordEncoder passwordEncoder
    ) {

        this.userAccountRepository =
                userAccountRepository;

        this.customerProfileService =
                customerProfileService;

        this.passwordEncoder =
                passwordEncoder;
    }


    /*
     * =========================================================
     * REGISTER CUSTOMER
     * ---------------------------------------------------------
     * Creates:
     *
     * 1. UserAccount
     * 2. Customer shopping profile
     *
     * Both are created inside the same transaction.
     *
     * Customer profile creation is delegated to
     * CustomerProfileService so the same logic can later be
     * reused for seller, support and ROOT accounts.
     * =========================================================
     */
    @Transactional
    public UserAccount registerCustomer(
            RegisterRequest request
    ) {

        String name =
                request.name()
                        .trim();


        String email =
                request.email()
                        .trim()
                        .toLowerCase();


        /*
         * =====================================================
         * EMAIL UNIQUENESS
         * =====================================================
         */
        if (userAccountRepository
                .existsByEmail(email)) {

            throw new IllegalArgumentException(
                    "An account with this email already exists"
            );
        }


        /*
         * =====================================================
         * PASSWORD HASH
         * =====================================================
         */
        String passwordHash =
                passwordEncoder.encode(
                        request.password()
                );


        /*
         * =====================================================
         * CREATE AUTHENTICATION IDENTITY
         * =====================================================
         */
        UserAccount user =
                new UserAccount(
                        email,
                        passwordHash,
                        Role.CUSTOMER,
                        AuthProvider.LOCAL
                );


        UserAccount savedUser =
                userAccountRepository.save(
                        user
                );


        /*
         * =====================================================
         * CREATE SHOPPING PROFILE
         * -----------------------------------------------------
         * Every authenticated account should have one
         * Customer commerce profile.
         *
         * For new registration this creates it.
         *
         * For future seller/support/root provisioning the same
         * service can safely reuse or create the profile.
         * =====================================================
         */
        customerProfileService
                .ensureCustomerProfile(
                        savedUser,
                        name
                );


        return savedUser;
    }
}