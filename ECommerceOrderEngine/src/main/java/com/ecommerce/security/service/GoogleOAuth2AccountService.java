
package com.ecommerce.security.service;

import com.ecommerce.security.model.AccountStatus;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;

import com.ecommerce.model.Customer;
import com.ecommerce.service.CustomerProfileService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.oauth2.core.oidc.user.OidcUser;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
public class GoogleOAuth2AccountService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    GoogleOAuth2AccountService.class
            );

    private final UserAccountRepository userAccountRepository;
    private final CustomerProfileService customerProfileService;

    public GoogleOAuth2AccountService(
            UserAccountRepository userAccountRepository,
            CustomerProfileService customerProfileService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.customerProfileService = customerProfileService;
    }

    /*
     * =========================================================
     * RESOLVE GOOGLE OAUTH2 ACCOUNT
     * =========================================================
     *
     * Called only after Spring Security has successfully
     * authenticated the Google OpenID Connect user.
     *
     * Responsibilities:
     *
     * 1. Validate Google identity.
     * 2. Require a verified email address.
     * 3. Extract a customer display name.
     * 4. Find an existing account using GOOGLE + subject.
     * 5. Prevent automatic account linking by email.
     * 6. Register new Google users as CUSTOMER.
     * 7. Ensure the Customer shopping profile exists.
     * 8. Record the successful login.
     *
     * UserAccount and Customer creation participate in
     * the same database transaction.
     *
     * JWT generation is handled by JwtService separately.
     * =========================================================
     */
    @Transactional
    public UserAccount resolveAndRecordLogin(
            OidcUser oidcUser
    ) {

        /*
         * =====================================================
         * VALIDATE GOOGLE IDENTITY
         * =====================================================
         */
        if (oidcUser == null) {
            throw new IllegalArgumentException(
                    "Google identity is required"
            );
        }

        String googleSubject = oidcUser.getSubject();

        if (
                googleSubject == null ||
                        googleSubject.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Google subject is missing"
            );
        }

        googleSubject = googleSubject.trim();

        if (googleSubject.length() > 255) {
            throw new IllegalArgumentException(
                    "Google subject exceeds maximum length"
            );
        }

        /*
         * The email must be verified by Google.
         */
        if (!Boolean.TRUE.equals(
                oidcUser.getEmailVerified()
        )) {
            throw new IllegalStateException(
                    "Google email is not verified"
            );
        }

        String googleEmail = oidcUser.getEmail();

        if (
                googleEmail == null ||
                        googleEmail.isBlank()
        ) {
            throw new IllegalArgumentException(
                    "Google email is missing"
            );
        }

        googleEmail = googleEmail
                .trim()
                .toLowerCase(Locale.ROOT);

        if (googleEmail.length() > 255) {
            throw new IllegalArgumentException(
                    "Google email exceeds maximum length"
            );
        }

        /*
         * =====================================================
         * GOOGLE DISPLAY NAME
         * =====================================================
         *
         * Prefer the name supplied by Google's OIDC profile.
         *
         * If Google does not provide a name, use the
         * verified email as the fallback.
         *
         * Existing customer profile details are preserved
         * by CustomerProfileService.
         */
        String googleDisplayName =
                oidcUser.getFullName();

        if (
                googleDisplayName == null ||
                        googleDisplayName.isBlank()
        ) {
            googleDisplayName = googleEmail;
        }

        googleDisplayName =
                googleDisplayName.trim();

        /*
         * =====================================================
         * FIND EXISTING GOOGLE ACCOUNT
         * =====================================================
         *
         * Google's stable OIDC "sub" is used as the
         * identity key.
         *
         * Email alone must never be used to automatically
         * authenticate or merge accounts.
         */
        Optional<UserAccount> existingGoogleAccount =
                userAccountRepository
                        .findByAuthProviderAndProviderUserId(
                                AuthProvider.GOOGLE,
                                googleSubject
                        );

        if (existingGoogleAccount.isPresent()) {

            UserAccount user =
                    existingGoogleAccount.get();

            /*
             * Only ACTIVE accounts may sign in.
             */
            validateActiveAccount(user);

            /*
             * =================================================
             * ENSURE EXISTING GOOGLE CUSTOMER PROFILE
             * =================================================
             *
             * This also attempts to repair Google accounts
             * created before profile creation was integrated.
             */
            log.info(
                    "Ensuring customer profile for existing Google account"
            );

            Customer customer =
                    customerProfileService.ensureCustomerProfile(
                            user,
                            googleDisplayName
                    );

            log.info(
                    "Customer profile ensured successfully. Customer ID: {}",
                    customer.getId()
            );

            /*
             * Preserve the database role, registered email,
             * authentication provider and Google subject.
             */
            user.recordLogin();

            return userAccountRepository.save(user);
        }

        /*
         * =====================================================
         * EMAIL CONFLICT CHECK
         * =====================================================
         *
         * An existing LOCAL account or another GOOGLE identity
         * with the same email must not be automatically linked.
         */
        if (
                userAccountRepository.existsByEmail(
                        googleEmail
                )
        ) {
            throw new IllegalStateException(
                    "An account with this email already exists"
            );
        }

        /*
         * =====================================================
         * CREATE NEW GOOGLE ACCOUNT
         * =====================================================
         *
         * Newly registered public users can only receive
         * the CUSTOMER role.
         *
         * Google accounts do not require a local password.
         */
        UserAccount newUser = new UserAccount(
                googleEmail,
                null,
                Role.CUSTOMER,
                AuthProvider.GOOGLE
        );

        /*
         * Link Google's stable, verified OIDC subject.
         */
        newUser.linkGoogleIdentity(
                googleSubject
        );

        /*
         * Record the initial login time.
         */
        newUser.recordLogin();

        /*
         * Persist the account before creating its
         * linked Customer record.
         */
        UserAccount savedUser =
                userAccountRepository.saveAndFlush(
                        newUser
                );

        /*
         * =====================================================
         * CREATE CUSTOMER SHOPPING PROFILE
         * =====================================================
         *
         * Use the same CustomerProfileService already
         * used by normal email/password registration.
         */
        log.info(
                "Creating customer profile for new Google account"
        );

        Customer customer =
                customerProfileService.ensureCustomerProfile(
                        savedUser,
                        googleDisplayName
                );

        log.info(
                "Google customer profile created successfully. Customer ID: {}",
                customer.getId()
        );

        return savedUser;
    }

    /*
     * =========================================================
     * ACCOUNT STATUS VALIDATION
     * =========================================================
     *
     * Suspended, dormant and deletion-pending accounts
     * must not sign in.
     */
    private void validateActiveAccount(
            UserAccount user
    ) {

        if (
                user.getStatus() != AccountStatus.ACTIVE
        ) {
            throw new IllegalStateException(
                    "Account is not active"
            );
        }
    }
}
