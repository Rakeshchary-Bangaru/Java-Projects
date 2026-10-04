package com.ecommerce.web;

import com.ecommerce.model.Order;
import com.ecommerce.repository.HibernateOrderRepository;
import com.ecommerce.repository.OrderRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * Displays the details of a single order.
 *
 * <p>The servlet reads the order ID from the request, loads the order
 * from persistent storage, prepares display-friendly date/time data,
 * and forwards the result to the order-details JSP.</p>
 */
@WebServlet("/orders/view")
public class OrderViewServlet extends HttpServlet {

    private OrderRepository orderRepository;

    /**
     * Initializes the order repository when Tomcat creates
     * this servlet.
     */
    @Override
    public void init() {
        orderRepository =
                new HibernateOrderRepository();
    }

    /**
     * Loads and displays a single order identified by the request
     * parameter named "id".
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {

            long orderId =
                    Long.parseLong(
                            request.getParameter("id")
                    );

            Optional<Order> order =
                    orderRepository.findById(orderId);

            if (order.isEmpty()) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Order not found"
                );

                return;
            }

            Order foundOrder =
                    order.get();

            /*
             * Convert the LocalDateTime into a user-friendly format
             * before sending it to the JSP.
             *
             * Example:
             *
             * 2026-09-15T08:51:13
             *
             * becomes:
             *
             * 15 Sep 2026, 08:51 AM
             */
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "dd MMM yyyy, hh:mm a"
                    );

            String formattedCreatedAt =
                    foundOrder.getCreatedAt()
                            .format(formatter);

            /*
             * Request scope is enough because these values are needed
             * only while rendering this single response.
             */
            request.setAttribute(
                    "order",
                    foundOrder
            );

            request.setAttribute(
                    "formattedCreatedAt",
                    formattedCreatedAt
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/order-details.jsp"
            ).forward(request, response);

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to load order",
                    e
            );

        } catch (NumberFormatException e) {

            /*
             * Handles values such as:
             *
             * /orders/view?id=abc
             */
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid order ID"
            );
        }
    }
}