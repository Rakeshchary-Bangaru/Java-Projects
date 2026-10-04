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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays the order history page.
 *
 * <p>The servlet loads persisted orders, formats their creation
 * timestamps for display, and forwards the prepared data to the JSP.</p>
 */
@WebServlet("/orders")
public class OrderServlet extends HttpServlet {

    private OrderRepository orderRepository;

    /**
     * Initializes the repository when Tomcat creates this servlet.
     */
    @Override
    public void init() {
        orderRepository =
                new HibernateOrderRepository();
    }

    /**
     * Loads all orders and prepares display-friendly creation
     * timestamps before forwarding to the orders JSP.
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {

            List<Order> orders =
                    orderRepository.findAll();

            /*
             * Order.createdAt is stored as LocalDateTime.
             *
             * Formatting is done in the servlet so the JSP only
             * displays prepared presentation data instead of
             * containing Java date-formatting logic.
             */
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "dd MMM yyyy, hh:mm a"
                    );

            /*
             * Map each order ID to its formatted creation timestamp.
             *
             * Example:
             *
             * 6001 -> "15 Sep 2026, 08:51 AM"
             *
             * The JSP can then retrieve the value using:
             *
             * ${formattedCreatedAt[order.id]}
             */
            Map<Long, String> formattedCreatedAt =
                    new HashMap<>();

            for (Order order : orders) {

                formattedCreatedAt.put(
                        order.getId(),
                        order.getCreatedAt()
                                .format(formatter)
                );
            }

            /*
             * Request scope is sufficient because these values are
             * needed only while rendering the current orders page.
             */
            request.setAttribute(
                    "orders",
                    orders
            );

            request.setAttribute(
                    "formattedCreatedAt",
                    formattedCreatedAt
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/orders.jsp"
            ).forward(request, response);

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to load orders",
                    e
            );
        }
    }
}