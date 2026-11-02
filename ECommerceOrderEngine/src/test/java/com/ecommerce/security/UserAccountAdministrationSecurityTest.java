package com.ecommerce.security;

import com.ecommerce.security.dto.UserAccountResponse;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;
import com.ecommerce.security.service.UserAccountAdministrationService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@ActiveProfiles("test")
class UserAccountAdministrationSecurityTest {

    @Autowired
    private UserAccountAdministrationService administrationService;

    @MockitoBean
    private UserAccountRepository userAccountRepository;


    @AfterEach
    void clearSecurityContext() {

        SecurityContextHolder.clearContext();
    }


    @Test
    void rootCanReadAllUserAccounts() {

        authenticate(
                authentication(
                        "root@test.com",
                        "ROLE_ROOT",
                        "SYSTEM_ADMIN"
                )
        );


        UserAccount customer =
                new UserAccount(
                        "customer@test.com",
                        "hashed-password",
                        Role.CUSTOMER,
                        AuthProvider.LOCAL
                );

        UserAccount seller =
                new UserAccount(
                        "seller@test.com",
                        "hashed-password",
                        Role.SELLER_ADMIN,
                        AuthProvider.LOCAL
                );


        when(userAccountRepository.findAll())
                .thenReturn(
                        List.of(
                                customer,
                                seller
                        )
                );


        List<UserAccountResponse> users =
                administrationService
                        .findAllUsers();


        assertEquals(
                2,
                users.size()
        );

        assertEquals(
                "customer@test.com",
                users.get(0).email()
        );

        assertEquals(
                Role.CUSTOMER,
                users.get(0).role()
        );

        assertEquals(
                Role.SELLER_ADMIN,
                users.get(1).role()
        );


        verify(userAccountRepository)
                .findAll();
    }


    @Test
    void itSupportCanReadAllUserAccounts() {

        authenticate(
                authentication(
                        "support@test.com",
                        "ROLE_IT_SUPPORT",
                        "ROLE_MANAGE"
                )
        );


        UserAccount customer =
                new UserAccount(
                        "customer@test.com",
                        "hashed-password",
                        Role.CUSTOMER,
                        AuthProvider.LOCAL
                );


        when(userAccountRepository.findAll())
                .thenReturn(
                        List.of(customer)
                );


        List<UserAccountResponse> users =
                administrationService
                        .findAllUsers();


        assertEquals(
                1,
                users.size()
        );

        assertEquals(
                "customer@test.com",
                users.get(0).email()
        );


        verify(userAccountRepository)
                .findAll();
    }


    @Test
    void sellerAdminCannotReadAllUserAccounts() {

        authenticate(
                authentication(
                        "seller@test.com",
                        "ROLE_SELLER_ADMIN",
                        "PRODUCT_CREATE"
                )
        );


        assertThrows(
                AccessDeniedException.class,
                () ->
                        administrationService
                                .findAllUsers()
        );


        verifyNoInteractions(
                userAccountRepository
        );
    }


    @Test
    void customerCannotReadAllUserAccounts() {

        authenticate(
                authentication(
                        "customer@test.com",
                        "ROLE_CUSTOMER",
                        "ORDER_READ_OWN"
                )
        );


        assertThrows(
                AccessDeniedException.class,
                () ->
                        administrationService
                                .findAllUsers()
        );


        verifyNoInteractions(
                userAccountRepository
        );
    }


    private void authenticate(
            Authentication authentication
    ) {

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );
    }


    private Authentication authentication(
            String email,
            String roleAuthority,
            String permissionAuthority
    ) {

        return new UsernamePasswordAuthenticationToken(
                email,
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                roleAuthority
                        ),
                        new SimpleGrantedAuthority(
                                permissionAuthority
                        )
                )
        );
    }
}