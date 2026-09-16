<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>

<head>
    <title>Orders</title>

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/css/styles.css">
</head>

<body>
        <jsp:include page="/WEB-INF/views/includes/header.jsp" />

        <div class="container">

            <h1>Orders</h1>

            <c:choose>

                <c:when test="${not empty orders}">

                    <table border="1">

                        <tr>
                            <th>Order ID</th>
                            <th>Customer</th>
                            <th>Status</th>
                            <th>Payment Type</th>
                            <th>Created At</th>
                            <th>Actions</th>
                        </tr>

                        <c:forEach
                                var="order"
                                items="${orders}">

                            <tr>

                                <td>
                                    ${order.id}
                                </td>

                                <td>
                                    ${order.customer.name}
                                </td>

                                <td>
                                    ${order.status}
                                </td>

                                <td>
                                    ${order.paymentType}
                                </td>
                                <td>
                                    ${formattedCreatedAt[order.id]}
                                </td>

                                <td>

                                    <a href="${pageContext.request.contextPath}/orders/view?id=${order.id}">
                                        View
                                    </a>

                                </td>

                            </tr>

                        </c:forEach>

                    </table>

                </c:when>

                <c:otherwise>

                    <p>No orders found.</p>

                </c:otherwise>

            </c:choose>

            <br>

            <a href="${pageContext.request.contextPath}/products">
                Back to Products
            </a>

        </div>



</body>
</html>