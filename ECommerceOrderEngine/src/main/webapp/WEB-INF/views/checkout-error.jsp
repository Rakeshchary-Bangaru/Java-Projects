<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>

<head>
    <title>Checkout Error</title>
</head>

<body>

<h1>Unable to Complete Checkout</h1>

<p>
    ${errorMessage}
</p>

<a href="${pageContext.request.contextPath}/cart">
    Back to Cart
</a>

<br><br>

<a href="${pageContext.request.contextPath}/products">
    Continue Shopping
</a>

</body>

</html>