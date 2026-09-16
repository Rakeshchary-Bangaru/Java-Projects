<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>

<head>
    <title>Customer Error</title>
</head>

<body>

<h1>Unable to Create Customer</h1>

<p>
    ${errorMessage}
</p>

<a href="${pageContext.request.contextPath}/customers/new">
    Try Again
</a>

<br><br>

<a href="${pageContext.request.contextPath}/customers">
    Back to Customers
</a>

</body>

</html>