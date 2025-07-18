<%@ page import="java.text.DecimalFormat" %>
<%@ page import="com.unisa.seedify.model.*" %>
<%@ page import="java.util.ArrayList" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%
    UserBean userBean = (UserBean) request.getSession(true).getAttribute("user");
    CartBean cartBean = (CartBean) request.getSession(true).getAttribute("cart");

    MemorizationsDao memorizationsDao = MemorizationsDao.getInstance();
    LocationsDao locationsDao = LocationsDao.getInstance();

    ArrayList<AddressBean> userAddresses = locationsDao.getAllAddresses(userBean);
    ArrayList<String> rawAddressOptions = new ArrayList<>();
    for (AddressBean addressBean : userAddresses) {
        rawAddressOptions.add("(" +
                addressBean.getStreet() + " - " +
                addressBean.getCity() + " " +
                "(" + addressBean.getProvince() + ")" +
                ", " + addressBean.getAddressId() + ")"
        );
    }
    String addressOptions = "[" + String.join(", ", rawAddressOptions) + "]";

    ArrayList<CreditCardBean> userCreditCards = memorizationsDao.getAllCreditCards(userBean);
    ArrayList<String> rawCreditCardOptions = new ArrayList<>();
    for (CreditCardBean creditCardBean : userCreditCards) {
        rawCreditCardOptions.add("(" + "************" + creditCardBean.getCardNumber().substring(12) + ", " + creditCardBean.getCardId() + ")");
    }
    String creditCardOptions = "[" + String.join(", ", rawCreditCardOptions) + "]";
%>

<html lang="en">
<head>
    <jsp:include page="/common/general/metadata.jsp"/>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/cart/styles/style.css">

    <script type="module" src="${pageContext.request.contextPath}/cart/scripts/script.js" defer></script>
</head>
<body>
    <jsp:include page="/common/components/main-navbar/main-navbar.jsp"/>

    <div class="main-page-content">
        <div id="cart-products-container">
            <% if (cartBean.getCartItems().isEmpty()) { %>
                <h5 class="rubik-400">Nessun prodotto nel carrello</h5>
            <% } else { %>
                <% for (CartItemBean cartItemBean : cartBean.getCartItems()) {
                    ProductBean productBean = cartItemBean.getProduct(); %>
                    <div class="product-container ui-block rubik-300" onclick="goToProductPage(<%= productBean.getProductId() %>)">
                        <button class="remove-from-cart-button material-button" onclick="sendRemoveFromCartRequest(this, <%= productBean.getProductId() %>, <%= cartItemBean.getQuantity() %>, <%= cartItemBean.getProduct().getPrice() %>)">
                            <span class="material-icons-round md-18">delete</span>
                        </button>
                        <div class="product-image-section">
                            <div class="product-image-container">
                                <img src="http://localhost:8080/seedify_war/resources-servlet?resource_type=product_image&amp;entity_primary_key=codice_prodotto=<%= productBean.getProductId() %>">
                            </div>
                        </div>
                        <div class="product-info-section">
                            <div class="product-info-container">
                                <div class="product-info">
                                    <p class="product-name rubik-500"><%= productBean.getName() %></p>
                                    <p class="product-price"><span class="price"><%= productBean.getPrice() %></span> €</p>
                                </div>
                            </div>
                        </div>
                        <p class="product-quantity">Quantità: <span class="quantity-value"><%= cartItemBean.getQuantity() %></span></p>
                        <div class="product-change-quantity-actions-container">
                            <button class="material-button cart-change-quantity" onclick="sendRemoveOneFromCart(this, <%= productBean.getProductId() %>)">
                                <span class="material-icons-round md-18">remove</span>
                            </button>
                            <button class="material-button cart-change-quantity" onclick="sendAddOneToCart(this, <%= productBean.getProductId() %>)">
                                <span class="material-icons-round md-18">add</span>
                            </button>
                        </div>
                    </div>
                <% }
            } %>
        </div>
        <div id="cart-menu" class="ui-block">
            <div id="cart-summary-container" class="ui-block">
                <div class="summary-info">
                    <span class="rubik-400">Prodotti:</span>
                    <span id="total-cart-products" class="rubik-300"><%= cartBean.getCartItems().stream().map((CartItemBean::getQuantity)).reduce(0, Integer::sum) %></span>
                </div>
                <div class="summary-info">
                    <span class="rubik-400">Totale:</span>
                    <span class="rubik-300"><span id="total-cart-price"><%= new DecimalFormat("0.00", new java.text.DecimalFormatSymbols(java.util.Locale.US)).format(cartBean.getTotalCartPrice()) %></span> €</span>
                </div>
            </div>
            <form action="${pageContext.request.contextPath}/order-servlet" method="POST">
                <div id="choose-address-box" class="dark rubik-300">
                    <jsp:include page="/common/components/input-box/input-box.jsp">
                        <jsp:param name="label" value="Indirizzo" />
                        <jsp:param name="tag" value="select" />
                        <jsp:param name="options" value="<%= addressOptions %>" />
                        <jsp:param name="id" value="choose-address-input-box" />
                        <jsp:param name="name" value="address_code" />
                        <jsp:param name="group" value="checkout" />
                    </jsp:include>
                </div>
                <div id="choose-credit-card-box" class="dark rubik-300">
                    <jsp:include page="/common/components/input-box/input-box.jsp">
                        <jsp:param name="label" value="Carta di credito" />
                        <jsp:param name="tag" value="select" />
                        <jsp:param name="options" value="<%= creditCardOptions %>" />
                        <jsp:param name="id" value="choose-credit-card-input-box" />
                        <jsp:param name="name" value="credit_card_code" />
                        <jsp:param name="group" value="checkout" />
                    </jsp:include>
                </div>
                <div id="cart-actions-container">
                    <button id="go-to-checkout-button" class="material-button" type="submit">
                        <span class="material-icons-round md-18">credit_score</span>
                        <span class="small-text">Procedi all'acquisto</span>
                    </button>
                </div>
            </form>
            <form action="${pageContext.request.contextPath}/cart-servlet?action=empty_cart" method="POST">
                <button id="empty-cart-button" class="material-button" type="submit">
                    <span class="material-icons-round md-18">restart_alt</span>
                    <span class="small-text">Svuota carrello</span>
                </button>
            </form>
        </div>
    </div>

    <jsp:include page="/common/components/main-footer/main-footer.jsp" />
</body>
</html>
