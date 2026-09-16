package com.ecommerce.web;

import com.ecommerce.model.Cart;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebServlet("/cart")
public class CartServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        HttpSession session = request.getSession();

        Cart cart = (Cart) session.getAttribute("cart");

        if(cart == null){
            cart= new Cart();
            session.setAttribute("cart",cart);
        }

        request.setAttribute("cart",cart);

        request.getRequestDispatcher("/WEB-INF/views/cart.jsp").forward(request,response);
    }
}