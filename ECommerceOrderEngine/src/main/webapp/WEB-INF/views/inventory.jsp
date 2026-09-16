<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>

<head>
    <title>Inventory</title>

    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/css/styles.css">
</head>

<body>

<jsp:include page="/WEB-INF/views/includes/header.jsp" />

  <div class="container">

      <h1>Inventory</h1>

      <table border="1">

          <tr>
              <th>Product ID</th>
              <th>Product</th>
              <th>Price</th>
              <th>Stock</th>
              <th>Add Stock</th>
          </tr>

          <c:forEach
                  var="product"
                  items="${products}">

              <tr>

                  <td>
                      ${product.id}
                  </td>

                  <td>
                      ${product.name}
                  </td>

                  <td>
                      ${product.price}
                  </td>

                  <td>
                      ${stockByProduct[product.id]}
                  </td>

                  <td>

                      <form
                              action="${pageContext.request.contextPath}/inventory/add"
                              method="post">

                          <input
                                  type="hidden"
                                  name="productId"
                                  value="${product.id}">

                          <input
                                  type="number"
                                  name="quantity"
                                  min="1"
                                  value="1"
                                  required>

                          <button type="submit">
                              Add Stock
                          </button>

                      </form>

                      <form
                              action="${pageContext.request.contextPath}/inventory/set"
                              method="post">

                          <input
                                  type="hidden"
                                  name="productId"
                                  value="${product.id}">

                          <input
                                  type="number"
                                  name="quantity"
                                  min="0"
                                  value="0"
                                  required>

                          <button type="submit">
                              Set Stock
                          </button>

                      </form>

                  </td>

              </tr>

          </c:forEach>

      </table>

      <br>

      <a href="${pageContext.request.contextPath}/products">
          Back to Products
      </a>


  </div>


</body>
</html>