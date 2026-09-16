package com.ecommerce.web;

import com.ecommerce.model.Cart;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/cart/update")
public class CartUpdateServlet extends HttpServlet{
    @Override
    protected  void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        long productId = Long.parseLong(request.getParameter("productId"));

        int quantity = Integer.parseInt(request.getParameter("quantity"));

        HttpSession session = request.getSession();

        Cart cart = (Cart) session.getAttribute("cart");

        if(cart != null){
            cart.updateQuantity(productId,quantity);
        }

        response.sendRedirect(request.getContextPath() + "/cart");
    }
}