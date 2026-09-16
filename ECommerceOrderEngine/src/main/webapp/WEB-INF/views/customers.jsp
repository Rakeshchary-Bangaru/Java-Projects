<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>

<head>
    <title>Customers</title>

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/css/styles.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/header.jsp" />

<div class="container">

    <h1>Customers</h1>

    <a href="${pageContext.request.contextPath}/customers/new">
        Add Customer
    </a>

    <br><br>

    <c:choose>

        <c:when test="${not empty customers}">

            <table border="1">

                <tr>
                    <th>ID</th>
                    <th>Name</th>
                    <th>Email</th>
                    <th>Actions</th>
                </tr>

                <c:forEach
                        var="customer"
                        items="${customers}">

                    <tr>

                        <td>${customer.id}</td>

                        <td>${customer.name}</td>

                        <td>${customer.email}</td>

                        <td>

                            <div class="actions">

                                <a
                                    class="edit-link"
                                    href="${pageContext.request.contextPath}/customers/edit?id=${customer.id}">
                                    Edit
                                </a>

                                <form
                                    action="${pageContext.request.contextPath}/customers/delete"
                                    method="post">

                                    <input
                                        type="hidden"
                                        name="id"
                                        value="${customer.id}">

                                    <button type="submit">
                                        Delete
                                    </button>

                                </form>

                            </div>

                        </td>

                    </tr>

                </c:forEach>

            </table>

        </c:when>

        <c:otherwise>

            <p>No customers found.</p>

        </c:otherwise>

    </c:choose>

</div>



</body>
</html>