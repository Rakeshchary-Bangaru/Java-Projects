<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>

<head>
    <title>Edit Customer</title>
</head>

<body>

<h1>Edit Customer</h1>

<form
    action="${pageContext.request.contextPath}/customers/edit"
    method="post">

    <input
        type="hidden"
        name="id"
        value="${customer.id}">

    <label>Name:</label>

    <input
        type="text"
        name="name"
        value="${customer.name}"
        readonly>

    <br><br>

    <label>Email:</label>

    <input
        type="email"
        name="email"
        value="${customer.email}"
        required>

    <br><br>

    <button type="submit">
        Update Customer
    </button>

</form>

<br>

<a href="${pageContext.request.contextPath}/customers">
    Back to Customers
</a>

</body>

</html>