package com.ecommerce.checkout;

import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.PaymentFailedException;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.Payment;
import com.ecommerce.model.PaymentType;
import com.ecommerce.model.Product;

import com.ecommerce.payment.PaymentProcessor;
import com.ecommerce.payment.PaymentProcessorProvider;
import com.ecommerce.payment.PaymentResult;

import com.ecommerce.repository.SpringDataInventoryRepository;
import com.ecommerce.repository.SpringDataOrderRepository;
import com.ecommerce.repository.SpringDataPaymentRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class SpringCheckoutServiceUnitTest {

    @Mock
    private SpringDataInventoryRepository inventoryRepository;

    @Mock
    private SpringDataOrderRepository orderRepository;

    @Mock
    private SpringDataPaymentRepository paymentRepository;

    @Mock
    private PaymentProcessorProvider paymentProcessorProvider;

    @InjectMocks
    private SpringCheckoutService checkoutService;


    /*
     * Constructor validation
     */
    @Test
    void shouldThrowExceptionWhenInventoryRepositoryIsNull() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new SpringCheckoutService(
                                        null,
                                        orderRepository,
                                        paymentRepository,
                                        paymentProcessorProvider
                                )
                );

        assertEquals(
                "Inventory repository cannot be null",
                exception.getMessage()
        );
    }


    @Test
    void shouldThrowExceptionWhenOrderRepositoryIsNull() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new SpringCheckoutService(
                                        inventoryRepository,
                                        null,
                                        paymentRepository,
                                        paymentProcessorProvider
                                )
                );

        assertEquals(
                "Order repository cannot be null",
                exception.getMessage()
        );
    }


    @Test
    void shouldThrowExceptionWhenPaymentRepositoryIsNull() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new SpringCheckoutService(
                                        inventoryRepository,
                                        orderRepository,
                                        null,
                                        paymentProcessorProvider
                                )
                );

        assertEquals(
                "Payment repository cannot be null",
                exception.getMessage()
        );
    }


    @Test
    void shouldThrowExceptionWhenPaymentProcessorProviderIsNull() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                new SpringCheckoutService(
                                        inventoryRepository,
                                        orderRepository,
                                        paymentRepository,
                                        null
                                )
                );

        assertEquals(
                "Payment processor provider cannot be null",
                exception.getMessage()
        );
    }


    /*
     * checkout() invalid order id
     */
    @Test
    void shouldThrowExceptionWhenOrderIdIsInvalid() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                checkoutService.checkout(
                                        0L,
                                        null,
                                        null,
                                        null
                                )
                );

        assertEquals(
                "Order id must be greater than 0",
                exception.getMessage()
        );

        verifyNoInteractions(
                inventoryRepository,
                orderRepository,
                paymentRepository,
                paymentProcessorProvider
        );
    }


    /*
     * checkout() null customer
     */
    @Test
    void shouldThrowExceptionWhenCustomerIsNull() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                checkoutService.checkout(
                                        9901001L,
                                        null,
                                        null,
                                        PaymentType.CARD
                                )
                );

        assertEquals(
                "Customer cannot be null",
                exception.getMessage()
        );

        verifyNoInteractions(
                inventoryRepository,
                orderRepository,
                paymentRepository,
                paymentProcessorProvider
        );
    }


    /*
     * checkout() null cart
     */
    @Test
    void shouldThrowExceptionWhenCartIsNull() {

        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                checkoutService.checkout(
                                        9901001L,
                                        customer,
                                        null,
                                        PaymentType.CARD
                                )
                );

        assertEquals(
                "Cart cannot be null or empty",
                exception.getMessage()
        );

        verifyNoInteractions(
                inventoryRepository,
                orderRepository,
                paymentRepository,
                paymentProcessorProvider
        );
    }


    /*
     * checkout() empty cart
     */
    @Test
    void shouldThrowExceptionWhenCartIsEmpty() {

        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );

        Cart cart =
                new Cart();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                checkoutService.checkout(
                                        9901001L,
                                        customer,
                                        cart,
                                        PaymentType.CARD
                                )
                );

        assertEquals(
                "Cart cannot be null or empty",
                exception.getMessage()
        );

        verifyNoInteractions(
                inventoryRepository,
                orderRepository,
                paymentRepository,
                paymentProcessorProvider
        );
    }


    /*
     * checkout() null payment type
     */
    @Test
    void shouldThrowExceptionWhenPaymentTypeIsNull() {

        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );

        Product product =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        75000
                );

        Cart cart =
                new Cart();

        cart.addProduct(
                product,
                1
        );

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () ->
                                checkoutService.checkout(
                                        9901001L,
                                        customer,
                                        cart,
                                        null
                                )
                );

        assertEquals(
                "Payment type cannot be null",
                exception.getMessage()
        );

        verifyNoInteractions(
                inventoryRepository,
                orderRepository,
                paymentRepository,
                paymentProcessorProvider
        );
    }


    /*
     * Insufficient stock
     */
    @Test
    void shouldThrowInsufficientStockException() {

        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );

        Product product =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        75000
                );

        Cart cart =
                new Cart();

        cart.addProduct(
                product,
                2
        );


        when(
                inventoryRepository.reserveStock(
                        7001L,
                        2
                )
        )
                .thenReturn(0);


        InsufficientStockException exception =
                assertThrows(
                        InsufficientStockException.class,
                        () ->
                                checkoutService.checkout(
                                        9901001L,
                                        customer,
                                        cart,
                                        PaymentType.CARD
                                )
                );


        assertEquals(
                "Insufficient stock for product 7001",
                exception.getMessage()
        );


        verify(inventoryRepository)
                .reserveStock(
                        7001L,
                        2
                );


        verifyNoInteractions(
                orderRepository,
                paymentRepository,
                paymentProcessorProvider
        );
    }


    /*
     * First stock reservation succeeds,
     * second reservation fails.
     */
    @Test
    void shouldStopCheckoutWhenSecondProductHasInsufficientStock() {

        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Product laptop =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        75000
                );


        Product mouse =
                new Product(
                        7401L,
                        "Wireless Mouse",
                        "Electronics",
                        2500
                );


        Cart cart =
                new Cart();

        cart.addProduct(
                laptop,
                1
        );

        cart.addProduct(
                mouse,
                2
        );


        when(
                inventoryRepository.reserveStock(
                        7001L,
                        1
                )
        )
                .thenReturn(1);


        when(
                inventoryRepository.reserveStock(
                        7401L,
                        2
                )
        )
                .thenReturn(0);


        InsufficientStockException exception =
                assertThrows(
                        InsufficientStockException.class,
                        () ->
                                checkoutService.checkout(
                                        9901001L,
                                        customer,
                                        cart,
                                        PaymentType.CARD
                                )
                );


        assertEquals(
                "Insufficient stock for product 7401",
                exception.getMessage()
        );


        InOrder inOrder =
                inOrder(inventoryRepository);


        inOrder.verify(inventoryRepository)
                .reserveStock(
                        7001L,
                        1
                );


        inOrder.verify(inventoryRepository)
                .reserveStock(
                        7401L,
                        2
                );


        verifyNoInteractions(
                orderRepository,
                paymentRepository,
                paymentProcessorProvider
        );
    }


    /*
     * Provider unexpectedly returns null.
     */
    @Test
    void shouldThrowExceptionWhenPaymentProcessorIsNull() {

        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Product product =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        75000
                );


        Cart cart =
                new Cart();

        cart.addProduct(
                product,
                1
        );


        when(
                inventoryRepository.reserveStock(
                        7001L,
                        1
                )
        )
                .thenReturn(1);


        when(
                paymentProcessorProvider.get(
                        PaymentType.CARD
                )
        )
                .thenReturn(null);


        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                checkoutService.checkout(
                                        9901001L,
                                        customer,
                                        cart,
                                        PaymentType.CARD
                                )
                );


        assertEquals(
                "Payment processor factory returned null",
                exception.getMessage()
        );


        verify(paymentProcessorProvider)
                .get(
                        PaymentType.CARD
                );


        verify(
                orderRepository,
                never()
        )
                .save(any(Order.class));


        verify(
                paymentRepository,
                never()
        )
                .save(any(Payment.class));
    }


    /*
     * Payment result contains a different
     * payment type than requested.
     */
    @Test
    void shouldThrowExceptionWhenPaymentResultTypeDoesNotMatch() {

        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Product product =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        75000
                );


        Cart cart =
                new Cart();

        cart.addProduct(
                product,
                1
        );


        PaymentProcessor paymentProcessor =
                mock(PaymentProcessor.class);


        PaymentResult paymentResult =
                mock(PaymentResult.class);


        when(
                inventoryRepository.reserveStock(
                        7001L,
                        1
                )
        )
                .thenReturn(1);


        when(
                paymentProcessorProvider.get(
                        PaymentType.CARD
                )
        )
                .thenReturn(
                        paymentProcessor
                );


        when(
                paymentProcessor.processPayment(
                        75000.0
                )
        )
                .thenReturn(
                        paymentResult
                );


        when(
                paymentResult.getPaymentType()
        )
                .thenReturn(
                        PaymentType.UPI
                );


        IllegalStateException exception =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                checkoutService.checkout(
                                        9901001L,
                                        customer,
                                        cart,
                                        PaymentType.CARD
                                )
                );


        assertEquals(
                "Payment result type does not match requested payment type",
                exception.getMessage()
        );


        verify(
                orderRepository,
                never()
        )
                .save(any(Order.class));


        verify(
                paymentRepository,
                never()
        )
                .save(any(Payment.class));
    }


    /*
     * Payment processor reports failure.
     */
    @Test
    void shouldThrowPaymentFailedExceptionWhenPaymentFails() {

        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Product product =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        75000
                );


        Cart cart =
                new Cart();

        cart.addProduct(
                product,
                1
        );


        PaymentProcessor paymentProcessor =
                mock(PaymentProcessor.class);


        PaymentResult paymentResult =
                mock(PaymentResult.class);


        when(
                inventoryRepository.reserveStock(
                        7001L,
                        1
                )
        )
                .thenReturn(1);


        when(
                paymentProcessorProvider.get(
                        PaymentType.CARD
                )
        )
                .thenReturn(
                        paymentProcessor
                );


        when(
                paymentProcessor.processPayment(
                        75000.0
                )
        )
                .thenReturn(
                        paymentResult
                );


        when(
                paymentResult.getPaymentType()
        )
                .thenReturn(
                        PaymentType.CARD
                );


        when(
                paymentResult.isSuccessful()
        )
                .thenReturn(false);


        when(
                paymentResult.getMessage()
        )
                .thenReturn(
                        "Payment declined"
                );


        PaymentFailedException exception =
                assertThrows(
                        PaymentFailedException.class,
                        () ->
                                checkoutService.checkout(
                                        9901001L,
                                        customer,
                                        cart,
                                        PaymentType.CARD
                                )
                );


        assertEquals(
                "Payment declined",
                exception.getMessage()
        );


        verify(
                orderRepository,
                never()
        )
                .save(any(Order.class));


        verify(
                paymentRepository,
                never()
        )
                .save(any(Payment.class));
    }


    /*
     * Successful checkout
     */
    @Test
    void shouldCheckoutSuccessfully() {

        long orderId =
                9901001L;


        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Product product =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        75000
                );


        Cart cart =
                new Cart();

        cart.addProduct(
                product,
                2
        );


        PaymentProcessor paymentProcessor =
                mock(PaymentProcessor.class);


        PaymentResult paymentResult =
                mock(PaymentResult.class);


        when(
                inventoryRepository.reserveStock(
                        7001L,
                        2
                )
        )
                .thenReturn(1);


        when(
                paymentProcessorProvider.get(
                        PaymentType.CARD
                )
        )
                .thenReturn(
                        paymentProcessor
                );


        when(
                paymentProcessor.processPayment(
                        150000.0
                )
        )
                .thenReturn(
                        paymentResult
                );


        when(
                paymentResult.getPaymentType()
        )
                .thenReturn(
                        PaymentType.CARD
                );


        when(
                paymentResult.isSuccessful()
        )
                .thenReturn(true);


        when(
                paymentResult.getAmount()
        )
                .thenReturn(
                        150000.0
                );


        when(
                paymentResult.getMessage()
        )
                .thenReturn(
                        "Payment successful"
                );


        /*
         * Return the same Order instance that the
         * service passes to save().
         */
        when(
                orderRepository.save(
                        any(Order.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );


        Order result =
                checkoutService.checkout(
                        orderId,
                        customer,
                        cart,
                        PaymentType.CARD
                );


        assertEquals(
                orderId,
                result.getId()
        );


        assertSame(
                customer,
                result.getCustomer()
        );


        assertEquals(
                PaymentType.CARD,
                result.getPaymentType()
        );


        assertEquals(
                OrderStatus.PAID,
                result.getStatus()
        );


        assertEquals(
                1,
                result.getItems().size()
        );


        assertEquals(
                150000.0,
                result.calculateTotal(),
                0.001
        );


        verify(inventoryRepository)
                .reserveStock(
                        7001L,
                        2
                );


        verify(paymentProcessorProvider)
                .get(
                        PaymentType.CARD
                );


        verify(paymentProcessor)
                .processPayment(
                        150000.0
                );


        verify(orderRepository)
                .save(
                        any(Order.class)
                );


        verify(paymentRepository)
                .save(
                        any(Payment.class)
                );
    }


    /*
     * Successful checkout with multiple products
     */
    @Test
    void shouldCheckoutMultipleProductsSuccessfully() {

        long orderId =
                9901002L;


        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Product laptop =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        75000
                );


        Product mouse =
                new Product(
                        7401L,
                        "Wireless Mouse",
                        "Electronics",
                        2500
                );


        Cart cart =
                new Cart();


        cart.addProduct(
                laptop,
                1
        );


        cart.addProduct(
                mouse,
                2
        );


        /*
         * Total:
         *
         * Laptop = 75000 × 1 = 75000
         * Mouse  = 2500  × 2 = 5000
         *
         * Total = 80000
         */


        when(
                inventoryRepository.reserveStock(
                        7001L,
                        1
                )
        )
                .thenReturn(1);


        when(
                inventoryRepository.reserveStock(
                        7401L,
                        2
                )
        )
                .thenReturn(1);


        PaymentProcessor paymentProcessor =
                mock(PaymentProcessor.class);


        PaymentResult paymentResult =
                mock(PaymentResult.class);


        when(
                paymentProcessorProvider.get(
                        PaymentType.CARD
                )
        )
                .thenReturn(
                        paymentProcessor
                );


        when(
                paymentProcessor.processPayment(
                        80000.0
                )
        )
                .thenReturn(
                        paymentResult
                );


        when(
                paymentResult.getPaymentType()
        )
                .thenReturn(
                        PaymentType.CARD
                );


        when(
                paymentResult.isSuccessful()
        )
                .thenReturn(true);


        when(
                paymentResult.getAmount()
        )
                .thenReturn(
                        80000.0
                );


        when(
                paymentResult.getMessage()
        )
                .thenReturn(
                        "Payment successful"
                );


        when(
                orderRepository.save(
                        any(Order.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );


        Order result =
                checkoutService.checkout(
                        orderId,
                        customer,
                        cart,
                        PaymentType.CARD
                );


        assertEquals(
                orderId,
                result.getId()
        );


        assertEquals(
                2,
                result.getItems().size()
        );


        assertEquals(
                80000.0,
                result.calculateTotal(),
                0.001
        );


        assertEquals(
                OrderStatus.PAID,
                result.getStatus()
        );


        verify(inventoryRepository)
                .reserveStock(
                        7001L,
                        1
                );


        verify(inventoryRepository)
                .reserveStock(
                        7401L,
                        2
                );


        verify(paymentProcessor)
                .processPayment(
                        80000.0
                );


        verify(orderRepository)
                .save(
                        any(Order.class)
                );


        verify(paymentRepository)
                .save(
                        any(Payment.class)
                );
    }


    /*
     * Verify that the Order passed to the repository
     * contains the correct checkout information.
     */
    @Test
    void shouldSaveOrderWithCorrectDetails() {

        long orderId =
                9901001L;


        Customer customer =
                new Customer(
                        8101L,
                        "Rakesh",
                        "rakesh@email.com"
                );


        Product product =
                new Product(
                        7001L,
                        "Laptop",
                        "Electronics",
                        75000
                );


        Cart cart =
                new Cart();

        cart.addProduct(
                product,
                1
        );


        PaymentProcessor paymentProcessor =
                mock(PaymentProcessor.class);


        PaymentResult paymentResult =
                mock(PaymentResult.class);


        when(
                inventoryRepository.reserveStock(
                        7001L,
                        1
                )
        )
                .thenReturn(1);


        when(
                paymentProcessorProvider.get(
                        PaymentType.CARD
                )
        )
                .thenReturn(
                        paymentProcessor
                );


        when(
                paymentProcessor.processPayment(
                        anyDouble()
                )
        )
                .thenReturn(
                        paymentResult
                );


        when(
                paymentResult.getPaymentType()
        )
                .thenReturn(
                        PaymentType.CARD
                );


        when(
                paymentResult.isSuccessful()
        )
                .thenReturn(true);


        when(
                paymentResult.getAmount()
        )
                .thenReturn(
                        75000.0
                );


        when(
                paymentResult.getMessage()
        )
                .thenReturn(
                        "Payment successful"
                );


        when(
                orderRepository.save(
                        any(Order.class)
                )
        )
                .thenAnswer(
                        invocation ->
                                invocation.getArgument(0)
                );


        checkoutService.checkout(
                orderId,
                customer,
                cart,
                PaymentType.CARD
        );


        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(
                        Order.class
                );


        verify(orderRepository)
                .save(
                        orderCaptor.capture()
                );


        Order savedOrder =
                orderCaptor.getValue();


        assertEquals(
                9901001L,
                savedOrder.getId()
        );


        assertSame(
                customer,
                savedOrder.getCustomer()
        );


        assertEquals(
                PaymentType.CARD,
                savedOrder.getPaymentType()
        );


        assertEquals(
                OrderStatus.PAID,
                savedOrder.getStatus()
        );


        assertEquals(
                1,
                savedOrder.getItems().size()
        );


        assertEquals(
                75000.0,
                savedOrder.calculateTotal(),
                0.001
        );
    }
}