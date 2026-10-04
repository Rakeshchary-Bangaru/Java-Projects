<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>

<head>
    <title>Products</title>
    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/css/styles.css">
</head>

<body>
<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="container">

    <h1>Products</h1>

    <c:choose>

        <c:when test="${not empty products}">

            <table border="1">

                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Category</th>
                    <th>Price</th>
                    <th>Actions</th>
                </tr>

                <c:forEach
                        var="product"
                        items="${products}">

                    <tr>

                        <td>${product.id}</td>

                        <td>${product.name}</td>

                        <td>${product.category}</td>

                        <td>${product.price}</td>



                            <td>

                                <div class="actions">

                                    <a
                                        class="edit-link"
                                        href="${pageContext.request.contextPath}/products/edit?id=${product.id}">
                                        Edit
                                    </a>

                                    <form
                                        action="${pageContext.request.contextPath}/products/delete"
                                        method="post">

                                        <input
                                            type="hidden"
                                            name="id"
                                            value="${product.id}">

                                        <button type="submit">
                                            Delete
                                        </button>

                                    </form>

                                    <form
                                        class="cart-action"
                                        action="${pageContext.request.contextPath}/cart/add"
                                        method="post">

                                        <input
                                            type="hidden"
                                            name="productId"
                                            value="${product.id}">

                                        <input
                                            type="number"
                                            name="quantity"
                                            value="1"
                                            min="1">

                                        <button type="submit">
                                            Add to Cart
                                        </button>

                                    </form>

                                </div>

                            </td>



                    </tr>

                </c:forEach>

            </table>

        </c:when>

        <c:otherwise>

            <p>No products found.</p>

        </c:otherwise>

    </c:choose>
    <br>
<a href="${pageContext.request.contextPath}/products/new">
    <button type="button">
        Add Product
    </button>
</a>

</div>



</body>

</html>