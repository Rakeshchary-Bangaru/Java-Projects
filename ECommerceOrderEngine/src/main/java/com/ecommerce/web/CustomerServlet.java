package com.ecommerce.web;

import com.ecommerce.model.Customer;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.HibernateCustomerRepository;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

@WebServlet("/customers")
public class CustomerServlet extends  HttpServlet{
    private CustomerRepository customerRepository;

    @Override
    public void init(){
        customerRepository = new HibernateCustomerRepository();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        try{
            List<Customer> customers = customerRepository.findAll();

            request.setAttribute("customers",customers);

            request.getRequestDispatcher("/WEB-INF/views/customers.jsp").forward(request, response);
        }
        catch(SQLException e){
            throw new ServletException("Unable to load customers",e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
        try{
            long id = Long.parseLong(request.getParameter("id"));
            String name =
                    request.getParameter("name");

            String email =
                    request.getParameter("email");
            Customer customer = new Customer(id,name,email);
            customerRepository.save(customer);
            response.sendRedirect(request.getContextPath() + "/customers");
        }
        catch (SQLIntegrityConstraintViolationException e) {

            response.setStatus(
                    HttpServletResponse.SC_CONFLICT
            );

            request.setAttribute(
                    "errorMessage",
                    "A customer with the same ID or email already exists."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/customer-error.jsp"
            ).forward(request, response);

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            request.setAttribute(
                    "errorMessage",
                    "Customer ID must be a valid number."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/customer-error.jsp"
            ).forward(request, response);

        } catch (SQLException e) {

            getServletContext().log(
                    "Customer creation database error",
                    e
            );

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            request.setAttribute(
                    "errorMessage",
                    "Something went wrong while creating the customer."
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/customer-error.jsp"
            ).forward(request, response);
        }
    }
}