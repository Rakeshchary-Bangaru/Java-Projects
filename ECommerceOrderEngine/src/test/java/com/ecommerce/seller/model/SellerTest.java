package com.ecommerce.seller.model;

import com.ecommerce.security.model.UserAccount;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SellerTest {

    @Test
    void newSellerShouldStartPending() {

        UserAccount user =
                mock(UserAccount.class);

        Seller seller =
                new Seller(
                        user,
                        "ABC Electronics"
                );

        assertEquals(
                "ABC Electronics",
                seller.getBusinessName()
        );

        assertEquals(
                SellerApprovalStatus.PENDING,
                seller.getApprovalStatus()
        );

        assertNull(
                seller.getApprovedAt()
        );

        assertNull(
                seller.getApprovedByUserId()
        );
    }


    @Test
    void rootShouldBeAbleToApproveSeller() {

        UserAccount user =
                mock(UserAccount.class);

        Seller seller =
                new Seller(
                        user,
                        "ABC Electronics"
                );

        seller.approve(1L);

        assertEquals(
                SellerApprovalStatus.APPROVED,
                seller.getApprovalStatus()
        );

        assertEquals(
                1L,
                seller.getApprovedByUserId()
        );

        assertNotNull(
                seller.getApprovedAt()
        );
    }
}