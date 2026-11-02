package com.ecommerce.security.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


class AccountDormancySchedulerTest {

    private AccountDormancyService dormancyService;

    private AccountDormancyScheduler scheduler;


    @BeforeEach
    void setUp() {

        dormancyService =
                mock(
                        AccountDormancyService.class
                );

        scheduler =
                new AccountDormancyScheduler(
                        dormancyService
                );
    }


    @Test
    void scheduledJobInvokesDormancyService() {

        scheduler.markInactiveAccountsDormant();


        verify(
                dormancyService,
                times(1)
        )
                .markInactiveAccountsDormant(
                        any(Instant.class)
                );
    }
}