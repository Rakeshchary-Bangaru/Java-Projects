package com.ecommerce.web;

import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.JdbcCustomerRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

@WebServlet("/customers/delete")
public class CustomerDeleteServlet extends HttpServlet{
    private CustomerRepository customerRepository;

    @Override
    public void init(){
        customerRepository = new JdbcCustomerRepository();
    }

    @Override
    protected void doPost(HttpServletRequest request,HttpServletResponse response) throws ServletException,IOException{
        try{
            long id = Long.parseLong(request.getParameter("id"));
            customerRepository.deleteById(id);
            response.sendRedirect(request.getContextPath() + "/customers");
        }
        catch (SQLIntegrityConstraintViolationException e) {

            response.setStatus(
                    HttpServletResponse.SC_CONFLICT
            );

            request.setAttribute(
                    "errorTitle",
                    "Unable to Delete Customer"
            );

            request.setAttribute(
                    "errorMessage",
                    "This customer cannot be deleted because existing orders reference this customer."
            );

            request.setAttribute(
                    "backPath",
                    "/customers"
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/delete-error.jsp"
            ).forward(request, response);
        }
        catch(SQLException e){
            throw new ServletException("Unable to delete customer ",e);
        }
    }
}