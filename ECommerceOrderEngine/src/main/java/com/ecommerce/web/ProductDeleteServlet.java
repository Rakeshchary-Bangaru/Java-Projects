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
import java.sql.SQLIntegrityConstraintViolationException;

@WebServlet("/products/delete")
public class ProductDeleteServlet extends HttpServlet{
    private ProductRepository productRepository;

    @Override
    public void init(){
        productRepository = new JdbcProductRepository();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        try{
            long id = Long.parseLong(request.getParameter("id"));

            productRepository.deleteById(id);

            response.sendRedirect(request.getContextPath() + "/products");
        }
        catch (SQLIntegrityConstraintViolationException e) {

            response.setStatus(
                    HttpServletResponse.SC_CONFLICT
            );

            request.setAttribute(
                    "errorTitle",
                    "Unable to Delete Product"
            );

            request.setAttribute(
                    "errorMessage",
                    "This product cannot be deleted because it is referenced by inventory or an existing order."
            );

            request.setAttribute(
                    "backPath",
                    "/products"
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/delete-error.jsp"
            ).forward(request, response);
        }
        catch(SQLException e){
            throw new ServletException("Unable to delete product",e);
        }
    }
}