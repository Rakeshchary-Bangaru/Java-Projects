package  com.ecommerce.web;

import com.ecommerce.repository.HibernateInventoryRepository;

import com.ecommerce.repository.InventoryRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/inventory/set")
public class InventorySetServlet extends HttpServlet{
    private InventoryRepository inventoryRepository;

    @Override
    public void init(){
        inventoryRepository = new HibernateInventoryRepository();
    }

    @Override
    protected void doPost(  HttpServletRequest request,
                            HttpServletResponse response) throws ServletException,IOException{
        try{
            long productId = Long.parseLong(request.getParameter("productId"));

            int quantity  = Integer.parseInt(request.getParameter("quantity"));

            inventoryRepository.setStock(productId,quantity);

            response.sendRedirect(request.getContextPath() + "/inventory");
        }
        catch(SQLException  e){
            throw new ServletException("Unable to set stock",e);
        }
        catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
    }
}