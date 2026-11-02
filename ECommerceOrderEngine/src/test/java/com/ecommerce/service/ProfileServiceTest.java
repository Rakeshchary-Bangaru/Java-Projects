package com.ecommerce.service;

import com.ecommerce.dto.ProfileResponse;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.model.Customer;
import com.ecommerce.repository.SpringDataCustomerRepository;
import com.ecommerce.security.model.AccountStatus;
import com.ecommerce.security.model.AuthProvider;
import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;
import com.ecommerce.security.repository.UserAccountRepository;
import com.ecommerce.seller.model.Seller;
import com.ecommerce.seller.model.SellerApprovalStatus;
import com.ecommerce.seller.repository.SellerRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private UserAccountRepository
            userAccountRepository;

    @Mock
    private SpringDataCustomerRepository
            customerRepository;

    @Mock
    private SellerRepository
            sellerRepository;

    @InjectMocks
    private ProfileService
            profileService;


    /*
     * CUSTOMER PROFILE
     */
    @Test
    void shouldReturnCustomerProfile() {

        long userAccountId = 101L;

        UserAccount user =
                mock(UserAccount.class);

        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh Bangaru",
                        "rakesh@email.com"
                );


        when(user.getId())
                .thenReturn(userAccountId);

        when(user.getEmail())
                .thenReturn(
                        "rakesh@email.com"
                );

        when(user.getRole())
                .thenReturn(
                        Role.CUSTOMER
                );

        when(user.getStatus())
                .thenReturn(
                        AccountStatus.ACTIVE
                );

        when(user.getAuthProvider())
                .thenReturn(
                        AuthProvider.LOCAL
                );


        when(
                userAccountRepository
                        .findById(
                                userAccountId
                        )
        )
                .thenReturn(
                        Optional.of(
                                user
                        )
                );


        when(
                customerRepository
                        .findByUserAccount_Id(
                                userAccountId
                        )
        )
                .thenReturn(
                        Optional.of(
                                customer
                        )
                );


        ProfileResponse profile =
                profileService
                        .getProfile(
                                userAccountId
                        );


        assertEquals(
                userAccountId,
                profile.accountId()
        );

        assertEquals(
                "Rakesh Bangaru",
                profile.displayName()
        );

        assertEquals(
                "rakesh@email.com",
                profile.email()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                profile.accountStatus()
        );

        assertEquals(
                AuthProvider.LOCAL,
                profile.loginMethod()
        );

        assertNull(
                profile.sellerProfile()
        );


        verify(userAccountRepository)
                .findById(
                        userAccountId
                );

        verify(customerRepository)
                .findByUserAccount_Id(
                        userAccountId
                );

        verifyNoInteractions(
                sellerRepository
        );
    }


    /*
     * SELLER PROFILE
     */
    @Test
    void shouldReturnSellerProfile() {

        long userAccountId = 201L;

        UserAccount user =
                mock(UserAccount.class);

        Seller seller =
                mock(Seller.class);


        when(user.getId())
                .thenReturn(
                        userAccountId
                );

        when(user.getEmail())
                .thenReturn(
                        "seller1@test.com"
                );

        when(user.getRole())
                .thenReturn(
                        Role.SELLER_ADMIN
                );

        when(user.getStatus())
                .thenReturn(
                        AccountStatus.ACTIVE
                );

        when(user.getAuthProvider())
                .thenReturn(
                        AuthProvider.LOCAL
                );


        when(seller.getId())
                .thenReturn(
                        1L
                );

        when(seller.getBusinessName())
                .thenReturn(
                        "TechStore"
                );

        when(seller.getApprovalStatus())
                .thenReturn(
                        SellerApprovalStatus.APPROVED
                );


        when(
                userAccountRepository
                        .findById(
                                userAccountId
                        )
        )
                .thenReturn(
                        Optional.of(
                                user
                        )
                );


        when(
                sellerRepository
                        .findByUserId(
                                userAccountId
                        )
        )
                .thenReturn(
                        Optional.of(
                                seller
                        )
                );


        ProfileResponse profile =
                profileService
                        .getProfile(
                                userAccountId
                        );


        assertEquals(
                "TechStore",
                profile.displayName()
        );

        assertEquals(
                "seller1@test.com",
                profile.email()
        );

        assertEquals(
                1L,
                profile
                        .sellerProfile()
                        .sellerId()
        );

        assertEquals(
                "TechStore",
                profile
                        .sellerProfile()
                        .businessName()
        );

        assertEquals(
                SellerApprovalStatus.APPROVED,
                profile
                        .sellerProfile()
                        .approvalStatus()
        );


        verify(userAccountRepository)
                .findById(
                        userAccountId
                );

        verify(sellerRepository)
                .findByUserId(
                        userAccountId
                );

        verifyNoInteractions(
                customerRepository
        );
    }


    /*
     * ROOT / IT SUPPORT
     * ---------------------------------------------------------
     * No Customer or Seller profile is required.
     */
    @Test
    void shouldUseEmailAsDisplayNameForAdministrativeAccount() {

        long userAccountId = 301L;

        UserAccount user =
                mock(UserAccount.class);


        when(user.getId())
                .thenReturn(
                        userAccountId
                );

        when(user.getEmail())
                .thenReturn(
                        "support@test.com"
                );

        when(user.getRole())
                .thenReturn(
                        Role.IT_SUPPORT
                );

        when(user.getStatus())
                .thenReturn(
                        AccountStatus.ACTIVE
                );

        when(user.getAuthProvider())
                .thenReturn(
                        AuthProvider.LOCAL
                );


        when(
                userAccountRepository
                        .findById(
                                userAccountId
                        )
        )
                .thenReturn(
                        Optional.of(
                                user
                        )
                );


        ProfileResponse profile =
                profileService
                        .getProfile(
                                userAccountId
                        );


        assertEquals(
                "support@test.com",
                profile.displayName()
        );

        assertNull(
                profile.sellerProfile()
        );


        verify(userAccountRepository)
                .findById(
                        userAccountId
                );

        verifyNoInteractions(
                customerRepository,
                sellerRepository
        );
    }


    /*
     * INVALID ACCOUNT ID
     */
    @Test
    void shouldRejectInvalidUserAccountId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                profileService
                                        .getProfile(
                                                0L
                                        )
                );


        assertEquals(
                "User account id must be greater than zero",
                exception.getMessage()
        );


        verifyNoInteractions(
                userAccountRepository,
                customerRepository,
                sellerRepository
        );
    }


    /*
     * MISSING USER ACCOUNT
     */
    @Test
    void shouldThrowWhenUserAccountDoesNotExist() {

        long userAccountId =
                999L;


        when(
                userAccountRepository
                        .findById(
                                userAccountId
                        )
        )
                .thenReturn(
                        Optional.empty()
                );


        ResourceNotFoundException exception =
                assertThrows(
                        ResourceNotFoundException.class,
                        () ->
                                profileService
                                        .getProfile(
                                                userAccountId
                                        )
                );


        assertEquals(
                "User account not found",
                exception.getMessage()
        );


        verify(userAccountRepository)
                .findById(
                        userAccountId
                );

        verifyNoInteractions(
                customerRepository,
                sellerRepository
        );
    }
}