package com.ecommerce.web;

import com.ecommerce.model.Product;
import com.ecommerce.repository.JdbcInventoryRepository;
import com.ecommerce.repository.JdbcProductRepository;
import com.ecommerce.repository.ProductRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Displays the inventory page.
 *
 * <p>The servlet loads all products and retrieves the current stock
 * quantity for each product before forwarding the data to the JSP.</p>
 */
@WebServlet("/inventory")
public class InventoryServlet extends HttpServlet {

    private ProductRepository productRepository;
    private JdbcInventoryRepository inventoryRepository;

    /**
     * Initializes the repositories when Tomcat creates this servlet.
     */
    @Override
    public void init() {

        productRepository =
                new JdbcProductRepository();

        inventoryRepository =
                new JdbcInventoryRepository();
    }

    /**
     * Loads products and their stock quantities and forwards them
     * to the inventory JSP.
     */
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {

            List<Product> products =
                    productRepository.findAll();

            /*
             * Store stock quantities using the product ID as the key.
             *
             * Example:
             *
             * 7001 -> 5
             * 7101 -> 10
             * 7201 -> 0
             *
             * This lets the JSP retrieve stock using:
             *
             * ${stockByProduct[product.id]}
             */
            Map<Long, Integer> stockByProduct =
                    new HashMap<>();

            /*
             * Retrieve the stock quantity for every product and
             * associate it with that product's ID.
             */
            for (Product product : products) {

                int stock =
                        inventoryRepository.getStock(
                                product.getId()
                        );

                stockByProduct.put(
                        product.getId(),
                        stock
                );
            }

            /*
             * Both collections are placed in request scope because
             * they are needed only while rendering this request.
             */
            request.setAttribute(
                    "products",
                    products
            );

            request.setAttribute(
                    "stockByProduct",
                    stockByProduct
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/inventory.jsp"
            ).forward(request, response);

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to load inventory",
                    e
            );
        }
    }
}