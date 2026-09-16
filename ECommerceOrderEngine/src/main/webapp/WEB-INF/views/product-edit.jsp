<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>

<head>
    <title>Edit Product</title>
</head>

<body>

<h1>Edit Product</h1>

<form
    action="${pageContext.request.contextPath}/products/edit"
    method="post">

    <input
        type="hidden"
        name="id"
        value="${product.id}">

    <label>Name:</label>
    <input
        type="text"
        name="name"
        value="${product.name}"
        required>

    <br><br>

    <label>Category:</label>
    <input
        type="text"
        name="category"
        value="${product.category}"
        required>

    <br><br>

    <label>Price:</label>
    <input
        type="number"
        step="0.01"
        name="price"
        value="${product.price}"
        required>

    <br><br>

    <button type="submit">
        Update Product
    </button>

</form>

<br>

<a href="${pageContext.request.contextPath}/products">
    Back to Products
</a>

</body>
</html>