<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>

<head>
    <title>Shopping Cart</title>

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/css/styles.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="container">

    <h1>Shopping Cart</h1>

    <c:choose>

        <c:when test="${not empty cart.items}">

            <table border="1">

                <tr>
                    <th>Product</th>
                    <th>Price</th>
                    <th>Quantity</th>
                    <th>Subtotal</th>
                    <th>Actions</th>
                </tr>

                <c:forEach
                        var="item"
                        items="${cart.items}">

                    <tr>

                        <td>
                            ${item.product.name}
                        </td>

                        <td>
                            ${item.product.price}
                        </td>

                        <td>

                            <form
                                    action="${pageContext.request.contextPath}/cart/update"
                                    method="post">

                                <input
                                        type="hidden"
                                        name="productId"
                                        value="${item.product.id}">

                                <input
                                        type="number"
                                        name="quantity"
                                        value="${item.quantity}"
                                        min="1"
                                        style="width:50px;">

                                <button type="submit">
                                    Update
                                </button>

                            </form>

                        </td>

                        <td>
                            ${item.subtotal}
                        </td>

                        <td>

                            <form
                                    action="${pageContext.request.contextPath}/cart/remove"
                                    method="post">

                                <input
                                        type="hidden"
                                        name="productId"
                                        value="${item.product.id}">

                                <button type="submit">
                                    Remove
                                </button>

                            </form>

                        </td>

                    </tr>

                </c:forEach>

            </table>

            <br>

            <strong>
                Cart Total:
                ${cart.calculateTotal()}
            </strong>

        </c:when>

        <c:otherwise>

            <p>Your cart is empty.</p>

        </c:otherwise>

    </c:choose>

    <br><br>

    <a href="${pageContext.request.contextPath}/checkout">
        Checkout
    </a>

    <br><br>

    <a href="${pageContext.request.contextPath}/products">
        Continue Shopping
    </a>

    <br><br>

    <form
            action="${pageContext.request.contextPath}/cart/clear"
            method="post">

        <button type="submit">
            Clear Cart
        </button>

    </form>

</div>



</body>
</html>