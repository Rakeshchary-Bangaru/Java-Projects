package com.ecommerce.security;

import com.ecommerce.security.model.AccountStatus;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest
@ActiveProfiles("test")
class UserDeletionPendingSecurityTest {

    @Autowired
    private UserAccountAdministrationService administrationService;

    @MockitoBean
    private UserAccountRepository userAccountRepository;


    @AfterEach
    void clearSecurityContext() {

        SecurityContextHolder.clearContext();
    }


    @Test
    void rootCanMarkDormantAccountDeletionPending() {

        authenticate(
                "ROLE_ROOT",
                "ACCOUNT_DELETE_DORMANT"
        );

        UserAccount user =
                dormantCustomer();


        when(userAccountRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(userAccountRepository.save(user))
                .thenReturn(user);


        var response =
                administrationService
                        .markDeletionPending(
                                10L
                        );


        assertEquals(
                AccountStatus.DELETION_PENDING,
                user.getStatus()
        );

        assertEquals(
                AccountStatus.DELETION_PENDING,
                response.status()
        );


        verify(userAccountRepository)
                .save(user);
    }


    @Test
    void itSupportCanMarkDormantAccountDeletionPending() {

        authenticate(
                "ROLE_IT_SUPPORT",
                "ACCOUNT_DELETE_DORMANT"
        );

        UserAccount user =
                dormantCustomer();


        when(userAccountRepository.findById(20L))
                .thenReturn(Optional.of(user));

        when(userAccountRepository.save(user))
                .thenReturn(user);


        administrationService
                .markDeletionPending(
                        20L
                );


        assertEquals(
                AccountStatus.DELETION_PENDING,
                user.getStatus()
        );
    }


    @Test
    void activeAccountCannotBeMarkedDeletionPending() {

        authenticate(
                "ROLE_ROOT",
                "ACCOUNT_DELETE_DORMANT"
        );

        UserAccount user =
                customer();


        when(userAccountRepository.findById(30L))
                .thenReturn(Optional.of(user));


        assertThrows(
                IllegalStateException.class,
                () ->
                        administrationService
                                .markDeletionPending(
                                        30L
                                )
        );


        assertEquals(
                AccountStatus.ACTIVE,
                user.getStatus()
        );


        verify(
                userAccountRepository,
                never()
        ).save(any());
    }


    @Test
    void suspendedAccountCannotBeMarkedDeletionPending() {

        authenticate(
                "ROLE_ROOT",
                "ACCOUNT_DELETE_DORMANT"
        );

        UserAccount user =
                customer();

        user.suspend();


        when(userAccountRepository.findById(40L))
                .thenReturn(Optional.of(user));


        assertThrows(
                IllegalStateException.class,
                () ->
                        administrationService
                                .markDeletionPending(
                                        40L
                                )
        );


        assertEquals(
                AccountStatus.SUSPENDED,
                user.getStatus()
        );


        verify(
                userAccountRepository,
                never()
        ).save(any());
    }


    @Test
    void sellerAdminCannotMarkAccountDeletionPending() {

        authenticate(
                "ROLE_SELLER_ADMIN",
                "PRODUCT_CREATE"
        );


        assertThrows(
                AccessDeniedException.class,
                () ->
                        administrationService
                                .markDeletionPending(
                                        10L
                                )
        );


        verifyNoInteractions(
                userAccountRepository
        );
    }


    @Test
    void customerCannotMarkAccountDeletionPending() {

        authenticate(
                "ROLE_CUSTOMER",
                "ORDER_READ_OWN"
        );


        assertThrows(
                AccessDeniedException.class,
                () ->
                        administrationService
                                .markDeletionPending(
                                        10L
                                )
        );


        verifyNoInteractions(
                userAccountRepository
        );
    }


    @Test
    void unknownUserCannotBeMarkedDeletionPending() {

        authenticate(
                "ROLE_ROOT",
                "ACCOUNT_DELETE_DORMANT"
        );


        when(userAccountRepository.findById(999L))
                .thenReturn(Optional.empty());


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        administrationService
                                .markDeletionPending(
                                        999L
                                )
        );


        verify(
                userAccountRepository,
                never()
        ).save(any());
    }


    private UserAccount customer() {

        return new UserAccount(
                "customer@test.com",
                "hashed-password",
                Role.CUSTOMER,
                AuthProvider.LOCAL
        );
    }


    private UserAccount dormantCustomer() {

        UserAccount user =
                customer();

        user.markDormant();

        return user;
    }


    private void authenticate(
            String role,
            String permission
    ) {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "administrator@test.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(
                                        role
                                ),
                                new SimpleGrantedAuthority(
                                        permission
                                )
                        )
                );


        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        authentication
                );
    }
}