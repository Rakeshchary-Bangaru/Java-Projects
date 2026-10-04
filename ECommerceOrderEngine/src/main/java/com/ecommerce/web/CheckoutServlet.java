package com.ecommerce.web;

import com.ecommerce.checkout.HibernateCheckoutService;
import com.ecommerce.exception.InsufficientStockException;
import com.ecommerce.exception.PaymentFailedException;
import com.ecommerce.model.Cart;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.PaymentType;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.HibernateCustomerRepository;
import com.ecommerce.repository.HibernateInventoryRepository;
import com.ecommerce.repository.HibernateOrderRepository;
import com.ecommerce.repository.HibernatePaymentRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Handles the web checkout flow.
 *
 * <p>GET displays the checkout page using the cart stored in the
 * current HTTP session. POST validates the submitted customer and
 * payment type, then delegates the actual transactional checkout
 * workflow to {@link HibernateCheckoutService}.</p>
 *
 * <p>The servlet is responsible only for web-layer concerns such as
 * request parameters, session state, redirects, view forwarding,
 * and HTTP error responses. Inventory reservation, payment,
 * order persistence, and transaction management remain inside
 * the checkout service.</p>
 */
@WebServlet("/checkout")
public class CheckoutServlet extends HttpServlet {

    private CustomerRepository customerRepository;
    private HibernateCheckoutService checkoutService;

    /**
     * Initializes repositories and the checkout service once when
     * Tomcat creates this servlet.
     */
    @Override
    public void init() {

        customerRepository =
                new HibernateCustomerRepository();

        checkoutService =
                new HibernateCheckoutService(
                        new HibernateInventoryRepository(),
                        new HibernateOrderRepository(),
                        new HibernatePaymentRepository()
                );
    }

    /**
     * Displays the checkout page.
     *
     * <p>The cart is retrieved from HttpSession because it must survive
     * across multiple HTTP requests. An empty or missing cart is sent
     * back to the cart page because checkout requires at least one item.</p>
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session =
                request.getSession();

        Cart cart =
                (Cart) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {

            response.sendRedirect(
                    request.getContextPath() + "/cart"
            );

            return;
        }

        try {

            /*
             * Customers come from persistent storage while the cart
             * comes from the current browser session.
             */
            List<Customer> customers =
                    customerRepository.findAll();

            request.setAttribute(
                    "customers",
                    customers
            );

            request.setAttribute(
                    "cart",
                    cart
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/checkout.jsp"
            ).forward(request, response);

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to load checkout page",
                    e
            );
        }
    }

    /**
     * Processes the submitted checkout request.
     *
     * <p>The servlet collects the selected customer, payment method,
     * and session cart, then delegates the transaction to
     * {@link HibernateCheckoutService}.</p>
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        HttpSession session =
                request.getSession();

        Cart cart =
                (Cart) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {

            response.sendRedirect(
                    request.getContextPath() + "/cart"
            );

            return;
        }

        try {

            long customerId =
                    Long.parseLong(
                            request.getParameter("customerId")
                    );

            String paymentTypeValue =
                    request.getParameter("paymentType");

            /*
             * HTML form values such as CARD, UPI, and WALLET
             * are converted back into the PaymentType enum.
             */
            PaymentType paymentType =
                    PaymentType.valueOf(paymentTypeValue);

            Optional<Customer> customer =
                    customerRepository.findById(customerId);

            if (customer.isEmpty()) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Customer not found"
                );

                return;
            }

            /*
             * Timestamp-based IDs are currently used for the V3
             * learning implementation. A production version would
             * normally use a database-generated or dedicated ID strategy.
             */
            long orderId =
                    System.currentTimeMillis();

            /*
             * The checkout service owns the business transaction:
             *
             * inventory reservation
             * payment processing
             * order persistence
             * payment persistence
             * commit / rollback
             */
            Order order =
                    checkoutService.checkout(
                            orderId,
                            customer.get(),
                            cart,
                            paymentType
                    );

            /*
             * Clear the cart only after checkout successfully commits.
             *
             * If checkout throws an exception, this line is never reached,
             * so the user's cart remains available for another attempt.
             */
            session.removeAttribute("cart");

            request.setAttribute(
                    "order",
                    order
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/checkout-success.jsp"
            ).forward(request, response);

        } catch (InsufficientStockException e) {

            /*
             * Insufficient stock is an expected business conflict,
             * so preserve HTTP 409 while rendering our own error page.
             */
            response.setStatus(
                    HttpServletResponse.SC_CONFLICT
            );

            request.setAttribute(
                    "errorMessage",
                    e.getMessage()
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/checkout-error.jsp"
            ).forward(request, response);

        } catch (PaymentFailedException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            request.setAttribute(
                    "errorMessage",
                    "Payment failed: " + e.getMessage()
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/checkout-error.jsp"
            ).forward(request, response);

        } catch (SQLException e) {

            /*
             * Log technical database details for developers,
             * but do not expose SQL information to the browser.
             */
            getServletContext().log(
                    "Checkout database error",
                    e
            );

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            request.setAttribute(
                    "errorMessage",
                    "Something went wrong while processing your order. Please try again."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/checkout-error.jsp"
            ).forward(request, response);

        } catch (IllegalArgumentException e) {

            /*
             * Covers invalid numeric input, invalid PaymentType values,
             * and domain validation failures.
             */
            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            request.setAttribute(
                    "errorMessage",
                    "Invalid checkout request."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/checkout-error.jsp"
            ).forward(request, response);
        }
    }
}