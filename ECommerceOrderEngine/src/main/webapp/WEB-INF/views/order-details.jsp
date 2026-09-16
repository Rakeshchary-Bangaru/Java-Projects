<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>

<head>
    <title>Order Details</title>
    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/css/styles.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="container">

    <h1>Order Details</h1>

    <p>
        <strong>Order ID:</strong>
        ${order.id}
    </p>

    <p>
        <strong>Customer:</strong>
        ${order.customer.name}
    </p>

    <p>
        <strong>Status:</strong>
        ${order.status}
    </p>

    <p>
        <strong>Payment Type:</strong>
        ${order.paymentType}
    </p>

   <p>
       <strong>Created At:</strong>
       ${formattedCreatedAt}
   </p>

    <h2>Items</h2>

    <table border="1">

        <tr>
            <th>Product</th>
            <th>Quantity</th>
            <th>Unit Price</th>
            <th>Subtotal</th>
        </tr>

        <c:forEach
                var="item"
                items="${order.items}">

            <tr>

                <td>
                    ${item.product.name}
                </td>

                <td>
                    ${item.quantity}
                </td>

                <td>
                    ${item.unitPrice}
                </td>

                <td>
                    ${item.subtotal}
                </td>

            </tr>

        </c:forEach>

    </table>

    <br>

    <a href="${pageContext.request.contextPath}/orders">
        Back to Orders
    </a>


</div>


</body>
</html>