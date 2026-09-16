<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>

<head>
    <title>Add Customer</title>
</head>

<body>

<h1>Add Customer</h1>

<form
    action="${pageContext.request.contextPath}/customers"
    method="post">

    <label>Customer ID:</label>
    <input
        type="number"
        name="id"
        required>

    <br><br>

    <label>Name:</label>
    <input
        type="text"
        name="name"
        required>

    <br><br>

    <label>Email:</label>
    <input
        type="email"
        name="email"
        required>

    <br><br>

    <button type="submit">
        Add Customer
    </button>

</form>

<br>

<a href="${pageContext.request.contextPath}/customers">
    Back to Customers
</a>

</body>
</html>