package com.ecommerce.web;

import com.ecommerce.model.Cart;
import com.ecommerce.model.Product;
import com.ecommerce.repository.JdbcProductRepository;
import com.ecommerce.repository.ProductRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Handles requests to add products to the user's shopping cart.
 *
 * <p>The shopping cart is stored in {@link HttpSession} so that
 * cart contents remain available across multiple HTTP requests
 * during the same user session.</p>
 */
@WebServlet("/cart/add")
public class CartAddServlet extends HttpServlet {

    private ProductRepository productRepository;

    /**
     * Initializes the repository used to retrieve products.
     */
    @Override
    public void init() {
        productRepository = new JdbcProductRepository();
    }

    /**
     * Adds the requested product and quantity to the session cart.
     *
     * <p>If the session does not already contain a cart, a new
     * {@link Cart} is created and stored in the session.</p>
     */
    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {

            long productId =
                    Long.parseLong(
                            request.getParameter("productId")
                    );

            int quantity =
                    Integer.parseInt(
                            request.getParameter("quantity")
                    );

            Optional<Product> product =
                    productRepository.findById(productId);

            if (product.isEmpty()) {

                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Product not found"
                );

                return;
            }

            HttpSession session =
                    request.getSession();

            /*
             * Reuse the existing cart stored in the session.
             * Create a new cart only when this session does not
             * already have one.
             */
            Cart cart =
                    (Cart) session.getAttribute("cart");

            if (cart == null) {

                cart = new Cart();

                session.setAttribute(
                        "cart",
                        cart
                );
            }

            /*
             * Cart.addProduct() also handles duplicate products
             * by increasing the existing quantity.
             */
            cart.addProduct(
                    product.get(),
                    quantity
            );

            /*
             * Redirect after POST so refreshing the cart page
             * does not submit the add-to-cart request again.
             */
            response.sendRedirect(
                    request.getContextPath() + "/cart"
            );

        } catch (SQLException e) {

            throw new ServletException(
                    "Unable to add product to cart",
                    e
            );
        }
    }
}