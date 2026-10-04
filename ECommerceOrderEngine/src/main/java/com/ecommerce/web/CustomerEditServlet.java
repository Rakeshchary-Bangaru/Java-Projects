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
import java.util.Optional;

@WebServlet("/customers/edit")
public class CustomerEditServlet extends HttpServlet{
    private CustomerRepository customerRepository;

    @Override
    public void init(){
        customerRepository = new HibernateCustomerRepository();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        try{
            long id = Long.parseLong(request.getParameter("id"));
            Optional<Customer> customer = customerRepository.findById(id);

            if(customer.isEmpty()){
                response.sendError(HttpServletResponse.SC_NOT_FOUND,"Customer not found");

                return;
            }

            request.setAttribute("customer",customer.get());

            request.getRequestDispatcher("/WEB-INF/views/customer-edit.jsp").forward(request,response);
        }
        catch(SQLException e){
            throw new ServletException("Unable to load customer",e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException{
        try{
            long id = Long.parseLong(request.getParameter("id"));
            String name =
                    request.getParameter("name");

            String email =
                    request.getParameter("email");
            Customer customer = new Customer(id,name,email);
            customerRepository.update(customer);
            response.sendRedirect(request.getContextPath() + "/customers");
        }
        catch(SQLException e){
            throw new ServletException("Unable to update customer", e);
        }
    }
}