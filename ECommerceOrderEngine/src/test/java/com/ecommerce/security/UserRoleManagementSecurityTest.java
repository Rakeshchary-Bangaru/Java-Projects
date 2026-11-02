package com.ecommerce.security;

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
class UserRoleManagementSecurityTest {

    @Autowired
    private UserAccountAdministrationService administrationService;

    @MockitoBean
    private UserAccountRepository userAccountRepository;


    @AfterEach
    void clearSecurityContext() {

        SecurityContextHolder.clearContext();
    }


    @Test
    void rootCanAssignSellerAdminRole() {

        authenticate(
                authentication(
                        "root@test.com",
                        "ROLE_ROOT",
                        "ROLE_MANAGE"
                )
        );


        UserAccount user =
                customerAccount();


        when(
                userAccountRepository.findById(10L)
        )
                .thenReturn(
                        Optional.of(user)
                );

        when(
                userAccountRepository.save(user)
        )
                .thenReturn(user);


        var response =
                administrationService.changeRole(
                        10L,
                        Role.SELLER_ADMIN
                );


        assertEquals(
                Role.SELLER_ADMIN,
                user.getRole()
        );

        assertEquals(
                Role.SELLER_ADMIN,
                response.role()
        );


        verify(userAccountRepository)
                .save(user);
    }


    @Test
    void rootCanAssignRootRole() {

        authenticate(
                authentication(
                        "root@test.com",
                        "ROLE_ROOT",
                        "ROLE_MANAGE"
                )
        );


        UserAccount user =
                customerAccount();


        when(
                userAccountRepository.findById(20L)
        )
                .thenReturn(
                        Optional.of(user)
                );

        when(
                userAccountRepository.save(user)
        )
                .thenReturn(user);


        administrationService.changeRole(
                20L,
                Role.ROOT
        );


        assertEquals(
                Role.ROOT,
                user.getRole()
        );
    }


    @Test
    void itSupportCanAssignRootRole() {

        authenticate(
                authentication(
                        "support@test.com",
                        "ROLE_IT_SUPPORT",
                        "ROLE_MANAGE"
                )
        );


        UserAccount user =
                customerAccount();


        when(
                userAccountRepository.findById(30L)
        )
                .thenReturn(
                        Optional.of(user)
                );

        when(
                userAccountRepository.save(user)
        )
                .thenReturn(user);


        administrationService.changeRole(
                30L,
                Role.ROOT
        );


        assertEquals(
                Role.ROOT,
                user.getRole()
        );
    }


    @Test
    void itSupportCanChangeSellerAdminToCustomer() {

        authenticate(
                authentication(
                        "support@test.com",
                        "ROLE_IT_SUPPORT",
                        "ROLE_MANAGE"
                )
        );


        UserAccount seller =
                new UserAccount(
                        "seller@test.com",
                        "hashed-password",
                        Role.SELLER_ADMIN,
                        AuthProvider.LOCAL
                );


        when(
                userAccountRepository.findById(40L)
        )
                .thenReturn(
                        Optional.of(seller)
                );

        when(
                userAccountRepository.save(seller)
        )
                .thenReturn(seller);


        administrationService.changeRole(
                40L,
                Role.CUSTOMER
        );


        assertEquals(
                Role.CUSTOMER,
                seller.getRole()
        );
    }


    @Test
    void sellerAdminCannotChangeRoles() {

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
                        administrationService.changeRole(
                                10L,
                                Role.ROOT
                        )
        );


        verifyNoInteractions(
                userAccountRepository
        );
    }


    @Test
    void customerCannotChangeRoles() {

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
                        administrationService.changeRole(
                                10L,
                                Role.ROOT
                        )
        );


        verifyNoInteractions(
                userAccountRepository
        );
    }


    @Test
    void changeRoleRejectsNullRole() {

        authenticate(
                authentication(
                        "root@test.com",
                        "ROLE_ROOT",
                        "ROLE_MANAGE"
                )
        );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        administrationService.changeRole(
                                10L,
                                null
                        )
        );


        verifyNoInteractions(
                userAccountRepository
        );
    }


    @Test
    void changeRoleRejectsUnknownUser() {

        authenticate(
                authentication(
                        "root@test.com",
                        "ROLE_ROOT",
                        "ROLE_MANAGE"
                )
        );


        when(
                userAccountRepository.findById(999L)
        )
                .thenReturn(
                        Optional.empty()
                );


        assertThrows(
                IllegalArgumentException.class,
                () ->
                        administrationService.changeRole(
                                999L,
                                Role.IT_SUPPORT
                        )
        );


        verify(
                userAccountRepository,
                never()
        ).save(any());
    }


    private UserAccount customerAccount() {

        return new UserAccount(
                "customer@test.com",
                "hashed-password",
                Role.CUSTOMER,
                AuthProvider.LOCAL
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