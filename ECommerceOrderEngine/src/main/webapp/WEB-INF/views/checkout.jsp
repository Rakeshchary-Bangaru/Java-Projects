<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html>

<head>
    <title>Checkout</title>
    <link
        rel="stylesheet"
        href="${pageContext.request.contextPath}/css/styles.css">
</head>

<body>
  <jsp:include page="/WEB-INF/views/includes/header.jsp" />

  <div class="container">

      <h1>Checkout</h1>

      <h2>Order Summary</h2>

      <table border="1">

          <tr>
              <th>Product</th>
              <th>Quantity</th>
              <th>Price</th>
              <th>Subtotal</th>
          </tr>

          <c:forEach
                  var="item"
                  items="${cart.items}">

              <tr>

                  <td>
                      ${item.product.name}
                  </td>

                  <td>
                      ${item.quantity}
                  </td>

                  <td>
                      ${item.product.price}
                  </td>

                  <td>
                      ${item.subtotal}
                  </td>

              </tr>

          </c:forEach>

      </table>

      <br>

      <strong>
          Total:
          ${cart.calculateTotal()}
      </strong>

      <h2>Customer</h2>

      <form
              action="${pageContext.request.contextPath}/checkout"
              method="post">

          <select name="customerId" required>

              <option value="">
                  Select Customer
              </option>

              <c:forEach
                      var="customer"
                      items="${customers}">

                  <option value="${customer.id}">
                      ${customer.name}
                      -
                      ${customer.email}
                  </option>

              </c:forEach>

          </select>

          <br><br>

          <h2>Payment Type</h2>

          <select name="paymentType" required>

              <option value="">
                  Select Payment Type
              </option>

              <option value="CARD">
                  Card
              </option>

              <option value="UPI">
                  UPI
              </option>

              <option value="WALLET">
                  Wallet
              </option>

          </select>

          <br><br>

          <button type="submit">
              Place Order
          </button>

      </form>

      <br>

      <a href="${pageContext.request.contextPath}/cart">
          Back to Cart
      </a>


  </div>



</body>
</html>