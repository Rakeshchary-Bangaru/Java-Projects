package com.ecommerce.web;

import com.ecommerce.model.Cart;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/cart/remove")
public class CartRemoveServlet extends HttpServlet{
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        long productId = Long.parseLong(request.getParameter("productId"));

        HttpSession session = request.getSession();
        Cart cart = (Cart)session.getAttribute("cart");

        if(cart != null){
            cart.removeProduct(productId);
        }

        response.sendRedirect(request.getContextPath() + "/cart");
    }
}