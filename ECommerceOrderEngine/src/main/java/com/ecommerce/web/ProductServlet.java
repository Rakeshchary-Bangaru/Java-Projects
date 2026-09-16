package com.ecommerce.web;

import com.ecommerce.model.Product;
import com.ecommerce.repository.JdbcProductRepository;
import com.ecommerce.repository.ProductRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/products")
public class  ProductServlet extends HttpServlet{
    private  ProductRepository productRepository;

    @Override
    public void init(){
        productRepository = new JdbcProductRepository();
    }

    @Override
    protected  void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        try{
            List<Product> products = productRepository.findAll();
            request.setAttribute(
                    "products",
                    products
            );

            request.getRequestDispatcher("/WEB-INF/views/products.jsp").forward(request,response);

        }catch(SQLException e){
            throw new ServletException("Unable to load products" , e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException{
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
            productRepository.save(product);

            response.sendRedirect(request.getContextPath() + "/products");
        }
        catch (SQLException e){
            throw new ServletException("Unable to create product",e);
        }
    }
}