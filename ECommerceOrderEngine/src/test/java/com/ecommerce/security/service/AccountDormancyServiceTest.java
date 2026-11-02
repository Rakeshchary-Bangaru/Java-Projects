package com.ecommerce.security.service;

import com.ecommerce.security.model.AccountStatus;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class AccountDormancyServiceTest {

    private UserAccountRepository repository;

    private AccountDormancyService service;


    @BeforeEach
    void setUp() {

        repository =
                mock(
                        UserAccountRepository.class
                );

        service =
                new AccountDormancyService(
                        repository
                );
    }


    @Test
    void marksInactiveAccountsAsDormant() {

        Instant cutoff =
                Instant.parse(
                        "2025-10-31T00:00:00Z"
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


        when(
                repository
                        .findActiveAccountsInactiveSince(
                                cutoff
                        )
        )
                .thenReturn(
                        List.of(
                                customer,
                                seller
                        )
                );


        int count =
                service
                        .markInactiveAccountsDormant(
                                cutoff
                        );


        assertEquals(
                2,
                count
        );

        assertEquals(
                AccountStatus.DORMANT,
                customer.getStatus()
        );

        assertEquals(
                AccountStatus.DORMANT,
                seller.getStatus()
        );


        verify(repository)
                .saveAll(
                        List.of(
                                customer,
                                seller
                        )
                );
    }


    @Test
    void returnsZeroWhenNoInactiveAccountsExist() {

        Instant cutoff =
                Instant.parse(
                        "2025-10-31T00:00:00Z"
                );


        when(
                repository
                        .findActiveAccountsInactiveSince(
                                cutoff
                        )
        )
                .thenReturn(
                        List.of()
                );


        int count =
                service
                        .markInactiveAccountsDormant(
                                cutoff
                        );


        assertEquals(
                0,
                count
        );


        verify(
                repository,
                never()
        ).saveAll(any());
    }


    @Test
    void rejectsNullCutoff() {

        assertThrows(
                IllegalArgumentException.class,
                () ->
                        service
                                .markInactiveAccountsDormant(
                                        null
                                )
        );


        verifyNoInteractions(
                repository
        );
    }


    @Test
    void doesNotChangeAccountsWhenRepositoryReturnsNone() {

        Instant cutoff =
                Instant.parse(
                        "2025-10-31T00:00:00Z"
                );


        when(
                repository
                        .findActiveAccountsInactiveSince(
                                cutoff
                        )
        )
                .thenReturn(
                        List.of()
                );


        service.markInactiveAccountsDormant(
                cutoff
        );


        verify(repository)
                .findActiveAccountsInactiveSince(
                        cutoff
                );

        verify(
                repository,
                never()
        ).saveAll(any());
    }
}