package com.ecommerce.security.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;


@Component
public class AccountDormancyScheduler {

    private static final int DORMANCY_MONTHS = 12;

    private final AccountDormancyService accountDormancyService;


    public AccountDormancyScheduler(
            AccountDormancyService accountDormancyService
    ) {

        this.accountDormancyService =
                accountDormancyService;
    }


    @Scheduled(
            cron = "0 0 2 * * *",
            zone = "UTC"
    )
    public void markInactiveAccountsDormant() {

        Instant cutoff =
                ZonedDateTime
                        .now(ZoneOffset.UTC)
                        .minusMonths(
                                DORMANCY_MONTHS
                        )
                        .toInstant();


        accountDormancyService
                .markInactiveAccountsDormant(
                        cutoff
                );
    }
}