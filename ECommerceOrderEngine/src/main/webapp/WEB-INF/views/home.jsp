<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">

    <title>E-Commerce Order Engine</title>

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/css/styles.css"
    >
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="container">

    <h1>E-Commerce Order Engine</h1>

    <p>
        Java Servlet, JSP, JDBC and MySQL based
        e-commerce order processing application.
    </p>

    <h2>V3 Features</h2>

    <ul>
        <li>Product Management</li>
        <li>Customer Management</li>
        <li>Session-Based Shopping Cart</li>
        <li>Transactional Checkout</li>
        <li>Order Management</li>
        <li>Inventory Management</li>
    </ul>

    <p>
        <a
            class="action-link"
            href="${pageContext.request.contextPath}/products"
        >
            Start Shopping
        </a>
    </p>

</div>

</body>
</html>