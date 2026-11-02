package com.ecommerce.seller.service;

import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.repository.SpringDataOrderItemRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SellerOrderServiceTest {

    @Mock
    private SpringDataOrderItemRepository
            orderItemRepository;


    @InjectMocks
    private SellerOrderService
            sellerOrderService;


    /*
     * =========================================================
     * MULTIPLE SELLER ITEMS IN ONE ORDER
     * ---------------------------------------------------------
     * Items belonging to the authenticated seller should be
     * grouped into one SellerOrderView.
     *
     * sellerSubtotal must include only those visible items.
     * =========================================================
     */
    @Test
    void shouldGroupSellerItemsByOrderAndCalculateSubtotal() {

        long userAccountId =
                101L;


        Order order =
                mock(Order.class);


        OrderItem laptop =
                mock(OrderItem.class);

        OrderItem mouse =
                mock(OrderItem.class);


        when(order.getId())
                .thenReturn(
                        9901001L
                );


        when(laptop.getOrder())
                .thenReturn(
                        order
                );

        when(mouse.getOrder())
                .thenReturn(
                        order
                );


        when(
                laptop.getSubtotalAmount()
        )
                .thenReturn(
                        new BigDecimal(
                                "80000.00"
                        )
                );


        when(
                mouse.getSubtotalAmount()
        )
                .thenReturn(
                        new BigDecimal(
                                "2000.00"
                        )
                );


        /*
         * IMPORTANT:
         *
         * Repository already filtered these rows by:
         *
         * product.seller.user.id = userAccountId
         *
         * Therefore no other seller's item enters this
         * service.
         */
        when(
                orderItemRepository
                        .findSellerOrderItemsByUserAccountId(
                                userAccountId
                        )
        )
                .thenReturn(
                        List.of(
                                laptop,
                                mouse
                        )
                );


        List<SellerOrderView> result =
                sellerOrderService
                        .findOrdersForSellerUser(
                                userAccountId
                        );


        assertEquals(
                1,
                result.size()
        );


        SellerOrderView sellerOrder =
                result.get(0);


        assertSame(
                order,
                sellerOrder.order()
        );


        assertEquals(
                2,
                sellerOrder.items().size()
        );


        assertSame(
                laptop,
                sellerOrder.items().get(0)
        );


        assertSame(
                mouse,
                sellerOrder.items().get(1)
        );


        assertEquals(
                new BigDecimal(
                        "82000.00"
                ),
                sellerOrder.sellerSubtotal()
        );


        verify(orderItemRepository)
                .findSellerOrderItemsByUserAccountId(
                        userAccountId
                );
    }


    /*
     * =========================================================
     * DIFFERENT ORDERS REMAIN SEPARATE
     * ---------------------------------------------------------
     * Seller-owned items belonging to different customer
     * orders must produce different SellerOrderView entries.
     * =========================================================
     */
    @Test
    void shouldKeepDifferentOrdersSeparate() {

        long userAccountId =
                101L;


        Order order1 =
                mock(Order.class);

        Order order2 =
                mock(Order.class);


        OrderItem item1 =
                mock(OrderItem.class);

        OrderItem item2 =
                mock(OrderItem.class);


        when(order1.getId())
                .thenReturn(
                        9901002L
                );

        when(order2.getId())
                .thenReturn(
                        9901001L
                );


        when(item1.getOrder())
                .thenReturn(
                        order1
                );

        when(item2.getOrder())
                .thenReturn(
                        order2
                );


        when(
                item1.getSubtotalAmount()
        )
                .thenReturn(
                        new BigDecimal(
                                "5000.00"
                        )
                );


        when(
                item2.getSubtotalAmount()
        )
                .thenReturn(
                        new BigDecimal(
                                "3000.00"
                        )
                );


        when(
                orderItemRepository
                        .findSellerOrderItemsByUserAccountId(
                                userAccountId
                        )
        )
                .thenReturn(
                        List.of(
                                item1,
                                item2
                        )
                );


        List<SellerOrderView> result =
                sellerOrderService
                        .findOrdersForSellerUser(
                                userAccountId
                        );


        assertEquals(
                2,
                result.size()
        );


        assertSame(
                order1,
                result.get(0).order()
        );


        assertEquals(
                new BigDecimal(
                        "5000.00"
                ),
                result.get(0)
                        .sellerSubtotal()
        );


        assertSame(
                order2,
                result.get(1).order()
        );


        assertEquals(
                new BigDecimal(
                        "3000.00"
                ),
                result.get(1)
                        .sellerSubtotal()
        );


        verify(orderItemRepository)
                .findSellerOrderItemsByUserAccountId(
                        userAccountId
                );
    }


    /*
     * =========================================================
     * INVALID USER ACCOUNT
     * ---------------------------------------------------------
     * Invalid authenticated identity must be rejected before
     * repository access.
     * =========================================================
     */
    @Test
    void shouldRejectInvalidUserAccountId() {

        long userAccountId =
                0L;


        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                sellerOrderService
                                        .findOrdersForSellerUser(
                                                userAccountId
                                        )
                );


        assertEquals(
                "User account id must be greater than zero",
                exception.getMessage()
        );


        verifyNoInteractions(
                orderItemRepository
        );
    }
}