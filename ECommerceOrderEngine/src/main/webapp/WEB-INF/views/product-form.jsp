<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>

<head>
    <title>Add Product</title>
</head>

<body>

<h1>Add Product</h1>

<form
    action = "${pageContext.request.contextPath}/products"
    method = "post">

    <label>Product ID:</label>
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

        <label>Category:</label>
        <input
            type="text"
            name="category"
            required>

        <br><br>

        <label>Price:</label>
        <input
            type="number"
            step="0.01"
            name="price"
            required>

        <br><br>

        <button type="submit">
            Add Product
        </button>

    </form>

    <br>

    <a href="${pageContext.request.contextPath}/products">
        Back to Products
    </a>

    </body>
    </html>