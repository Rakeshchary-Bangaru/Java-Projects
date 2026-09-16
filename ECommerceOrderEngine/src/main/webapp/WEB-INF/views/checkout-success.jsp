<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.ecommerce.model.Order" %>

<!DOCTYPE html>
<html>

<head>
    <title>Order Successful</title>
</head>

<body>

<%
    Order order =
            (Order) request.getAttribute("order");
%>

<h1>Order Successful</h1>

<p>
    Your order has been placed successfully.
</p>

<p>
    Order ID:
    <strong><%= order.getId() %></strong>
</p>

<a href="${pageContext.request.contextPath}/products">
    Continue Shopping
</a>

</body>
</html>