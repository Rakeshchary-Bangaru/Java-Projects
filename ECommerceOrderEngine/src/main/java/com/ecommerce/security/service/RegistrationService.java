package com.ecommerce.security.service;

import com.ecommerce.security.dto.RegisterRequest;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserAccount registerCustomer(
            RegisterRequest request
    ) {

        String email =
                request.email()
                        .trim()
                        .toLowerCase();

        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "An account with this email already exists"
            );
        }

        String passwordHash =
                passwordEncoder.encode(
                        request.password()
                );

        UserAccount user =
                new UserAccount(
                        email,
                        passwordHash,
                        Role.CUSTOMER,
                        AuthProvider.LOCAL
                );

        return userAccountRepository.save(user);
    }
}