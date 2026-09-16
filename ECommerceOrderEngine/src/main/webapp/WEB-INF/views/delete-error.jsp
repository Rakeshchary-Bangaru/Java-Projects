<%@ page contentType="text/html;charset=UTF-8" %>

<!DOCTYPE html>
<html>

<head>
    <title>Delete Error</title>
</head>

<body>

<h1>${errorTitle}</h1>

<p>
    ${errorMessage}
</p>

<a href="${pageContext.request.contextPath}${backPath}">
    Go Back
</a>

</body>
</html>