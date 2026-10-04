package com.ecommerce.web;

import com.ecommerce.model.Product;
import com.ecommerce.repository.HibernateProductRepository;
import com.ecommerce.repository.ProductRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

@WebServlet("/products/edit")
public class ProductEditServlet extends HttpServlet{
    private ProductRepository productRepository;

    @Override
    public void init(){
        productRepository = new HibernateProductRepository();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        try{
            long id = Long.parseLong(request.getParameter("id"));

            Optional<Product> product = productRepository.findById(id);

            if(product.isEmpty()){
                response.sendError(HttpServletResponse.SC_NOT_FOUND,"Product not found");
                return;
            }

            request.setAttribute("product",product.get());
            request.getRequestDispatcher("/WEB-INF/views/product-edit.jsp").forward(request,response);
        }
        catch(SQLException e){
            throw new ServletException("Unable to load product",e);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        try{
            long id = Long.parseLong(request.getParameter("id"));
            String name =
                    request.getParameter("name");

            String category =
                    request.getParameter("category");

            double price = Double.parseDouble(
                    request.getParameter("price")
            );

            Product product = new Product(id,name,category,price);
            productRepository.update(product);
            response.sendRedirect(request.getContextPath() + "/products");
        }
        catch(SQLException e){
            throw new ServletException("Unable to update product",e);
        }
    }
}