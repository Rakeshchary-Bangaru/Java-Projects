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
class UserAccountStatusSecurityTest {

    @Autowired
    private UserAccountAdministrationService administrationService;

    @MockitoBean
    private UserAccountRepository userAccountRepository;


    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }


    @Test
    void rootCanSuspendUser() {

        authenticate(
                "ROLE_ROOT",
                "ACCOUNT_SUSPEND"
        );

        UserAccount user = customer();

        when(userAccountRepository.findById(10L))
                .thenReturn(Optional.of(user));

        when(userAccountRepository.save(user))
                .thenReturn(user);


        var response =
                administrationService.changeStatus(
                        10L,
                        AccountStatus.SUSPENDED
                );


        assertEquals(
                AccountStatus.SUSPENDED,
                user.getStatus()
        );

        assertEquals(
                AccountStatus.SUSPENDED,
                response.status()
        );
    }


    @Test
    void itSupportCanSuspendUser() {

        authenticate(
                "ROLE_IT_SUPPORT",
                "ACCOUNT_SUSPEND"
        );

        UserAccount user = customer();

        when(userAccountRepository.findById(20L))
                .thenReturn(Optional.of(user));

        when(userAccountRepository.save(user))
                .thenReturn(user);


        administrationService.changeStatus(
                20L,
                AccountStatus.SUSPENDED
        );


        assertEquals(
                AccountStatus.SUSPENDED,
                user.getStatus()
        );
    }


    @Test
    void itSupportCanReactivateUser() {

        authenticate(
                "ROLE_IT_SUPPORT",
                "ACCOUNT_SUSPEND"
        );

        UserAccount user = customer();
        user.suspend();

        when(userAccountRepository.findById(30L))
                .thenReturn(Optional.of(user));

        when(userAccountRepository.save(user))
                .thenReturn(user);


        administrationService.changeStatus(
                30L,
                AccountStatus.ACTIVE
        );


        assertEquals(
                AccountStatus.ACTIVE,
                user.getStatus()
        );
    }


    @Test
    void sellerAdminCannotChangeAccountStatus() {

        authenticate(
                "ROLE_SELLER_ADMIN",
                "PRODUCT_CREATE"
        );


        assertThrows(
                AccessDeniedException.class,
                () ->
                        administrationService.changeStatus(
                                10L,
                                AccountStatus.SUSPENDED
                        )
        );

        verifyNoInteractions(userAccountRepository);
    }


    @Test
    void customerCannotChangeAccountStatus() {

        authenticate(
                "ROLE_CUSTOMER",
                "ORDER_READ_OWN"
        );


        assertThrows(
                AccessDeniedException.class,
                () ->
                        administrationService.changeStatus(
                                10L,
                                AccountStatus.SUSPENDED
                        )
        );

        verifyNoInteractions(userAccountRepository);
    }


    @Test
    void dormantCannotBeAssignedDirectly() {

        authenticate(
                "ROLE_ROOT",
                "ACCOUNT_SUSPEND"
        );

        UserAccount user = customer();

        when(userAccountRepository.findById(10L))
                .thenReturn(Optional.of(user));


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        administrationService.changeStatus(
                                10L,
                                AccountStatus.DORMANT
                        )
        );

        verify(
                userAccountRepository,
                never()
        ).save(any());
    }


    @Test
    void deletionPendingCannotBeAssignedDirectly() {

        authenticate(
                "ROLE_ROOT",
                "ACCOUNT_SUSPEND"
        );

        UserAccount user = customer();

        when(userAccountRepository.findById(10L))
                .thenReturn(Optional.of(user));


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        administrationService.changeStatus(
                                10L,
                                AccountStatus.DELETION_PENDING
                        )
        );

        verify(
                userAccountRepository,
                never()
        ).save(any());
    }


    @Test
    void deletedCannotBeAssignedDirectly() {

        authenticate(
                "ROLE_ROOT",
                "ACCOUNT_SUSPEND"
        );

        UserAccount user = customer();

        when(userAccountRepository.findById(10L))
                .thenReturn(Optional.of(user));


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        administrationService.changeStatus(
                                10L,
                                AccountStatus.DELETED
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


    private void authenticate(
            String role,
            String permission
    ) {

        Authentication authentication =
                new UsernamePasswordAuthenticationToken(
                        "test@test.com",
                        null,
                        List.of(
                                new SimpleGrantedAuthority(role),
                                new SimpleGrantedAuthority(permission)
                        )
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(authentication);
    }
}