package com.ecommerce.service;

import com.ecommerce.dto.ProfileResponse;
import com.ecommerce.dto.SellerProfileResponse;

import com.ecommerce.exception.ResourceNotFoundException;

import com.ecommerce.model.Customer;

import com.ecommerce.repository.SpringDataCustomerRepository;

import com.ecommerce.security.model.Role;
import com.ecommerce.security.model.UserAccount;

import com.ecommerce.security.repository.UserAccountRepository;

import com.ecommerce.seller.model.Seller;
import com.ecommerce.seller.repository.SellerRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/*
 * =========================================================
 * PROFILE SERVICE
 * ---------------------------------------------------------
 * Builds the profile for the currently authenticated
 * UserAccount.
 *
 * IMPORTANT:
 *
 * Role is used internally only to locate the correct
 * domain profile.
 *
 * It is NOT returned to the browser as profile data.
 * =========================================================
 */
@Service
public class ProfileService {

    private final UserAccountRepository
            userAccountRepository;

    private final SpringDataCustomerRepository
            customerRepository;

    private final SellerRepository
            sellerRepository;


    public ProfileService(
            UserAccountRepository userAccountRepository,
            SpringDataCustomerRepository customerRepository,
            SellerRepository sellerRepository
    ) {

        this.userAccountRepository =
                userAccountRepository;

        this.customerRepository =
                customerRepository;

        this.sellerRepository =
                sellerRepository;
    }


    /*
     * =========================================================
     * CURRENT USER PROFILE
     * ---------------------------------------------------------
     *
     * userAccountId comes from the signed JWT.
     *
     * It is never supplied as a request parameter.
     * =========================================================
     */
    @Transactional(readOnly = true)
    public ProfileResponse getProfile(
            long userAccountId
    ) {

        if (userAccountId <= 0) {

            throw new IllegalArgumentException(
                    "User account id must be greater than zero"
            );
        }


        UserAccount user =
                userAccountRepository
                        .findById(
                                userAccountId
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "User account not found"
                                        )
                        );


        String displayName;

        SellerProfileResponse sellerProfile =
                null;


        /*
         * CUSTOMER PROFILE
         */
        if (user.getRole() == Role.CUSTOMER) {

            Customer customer =
                    customerRepository
                            .findByUserAccount_Id(
                                    user.getId()
                            )
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Customer profile not found"
                                            )
                            );


            displayName =
                    customer.getName();
        }


        /*
         * SELLER PROFILE
         *
         * Seller is loaded only ONCE.
         */
        else if (
                user.getRole()
                        == Role.SELLER_ADMIN
        ) {

            Seller seller =
                    sellerRepository
                            .findByUserId(
                                    user.getId()
                            )
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Seller profile not found"
                                            )
                            );


            displayName =
                    seller.getBusinessName();


            sellerProfile =
                    new SellerProfileResponse(

                            seller.getId(),

                            seller.getBusinessName(),

                            seller.getApprovalStatus()
                    );
        }


        /*
         * ROOT / IT_SUPPORT
         */
        else {

            displayName =
                    user.getEmail();
        }


        return new ProfileResponse(

                user.getId(),

                displayName,

                user.getEmail(),

                user.getStatus(),

                user.getAuthProvider(),

                user.getCreatedAt(),

                user.getLastLoginAt(),

                user.getLastActivityAt(),

                sellerProfile
        );
    }

    /*
     * =========================================================
     * DISPLAY NAME
     * ---------------------------------------------------------
     * CUSTOMER:
     *      Customer.name
     *
     * SELLER_ADMIN:
     *      Seller.businessName
     *
     * ROOT / IT_SUPPORT:
     *      email
     *
     * We do not pretend that ROOT/IT_SUPPORT have a personal
     * name because UserAccount currently has no name column.
     * =========================================================
     */
    private String resolveDisplayName(
            UserAccount user
    ) {

        if (user.getRole() == Role.CUSTOMER) {

            Customer customer =
                    customerRepository
                            .findByUserAccount_Id(
                                    user.getId()
                            )
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Customer profile not found"
                                            )
                            );


            return customer.getName();
        }


        if (user.getRole() == Role.SELLER_ADMIN) {

            Seller seller =
                    sellerRepository
                            .findByUserId(
                                    user.getId()
                            )
                            .orElseThrow(
                                    () ->
                                            new ResourceNotFoundException(
                                                    "Seller profile not found"
                                            )
                            );


            return seller.getBusinessName();
        }


        /*
         * UserAccount currently stores no personal name for
         * ROOT or IT_SUPPORT.
         */
        return user.getEmail();
    }


    /*
     * =========================================================
     * SELLER-SPECIFIC PROFILE
     * ---------------------------------------------------------
     * Returned only for a SELLER_ADMIN account.
     *
     * Normal customers receive null.
     * =========================================================
     */
    private SellerProfileResponse resolveSellerProfile(
            UserAccount user
    ) {

        if (user.getRole() != Role.SELLER_ADMIN) {
            return null;
        }


        Seller seller =
                sellerRepository
                        .findByUserId(
                                user.getId()
                        )
                        .orElseThrow(
                                () ->
                                        new ResourceNotFoundException(
                                                "Seller profile not found"
                                        )
                        );


        return new SellerProfileResponse(

                seller.getId(),

                seller.getBusinessName(),

                seller.getApprovalStatus()
        );
    }
}