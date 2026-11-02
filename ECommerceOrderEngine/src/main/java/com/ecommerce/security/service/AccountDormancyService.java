package com.ecommerce.security.service;

import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;


@Service
public class AccountDormancyService {

    private final UserAccountRepository userAccountRepository;


    public AccountDormancyService(
            UserAccountRepository userAccountRepository
    ) {

        this.userAccountRepository =
                userAccountRepository;
    }


    @Transactional
    public int markInactiveAccountsDormant(
            Instant cutoff
    ) {

        if (cutoff == null) {
            throw new IllegalArgumentException(
                    "Dormancy cutoff cannot be null"
            );
        }


        List<UserAccount> inactiveAccounts =
                userAccountRepository
                        .findActiveAccountsInactiveSince(
                                cutoff
                        );


        inactiveAccounts.forEach(
                UserAccount::markDormant
        );


        if (!inactiveAccounts.isEmpty()) {

            userAccountRepository.saveAll(
                    inactiveAccounts
            );
        }


        return inactiveAccounts.size();
    }
}