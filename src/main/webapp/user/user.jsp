<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="com.unisa.seedify.model.UserBean" %>
<%@ page import="com.unisa.seedify.model.UserDao" %>
<%@ page import="com.unisa.seedify.model.OrderDao" %>
<%@ page import="com.unisa.seedify.model.ProductDao" %>

<%
  UserBean userBean = (UserBean) request.getSession(true).getAttribute("user");

  UserDao userDao = UserDao.getInstance();
  OrderDao orderDao = OrderDao.getInstance();
  ProductDao productDao = ProductDao.getInstance();
%>

<html>
  <head>
    <jsp:include page="/common/general/metadata.jsp"/>

    <link rel="stylesheet" href="${pageContext.request.contextPath}/user/styles/style.css">

    <script type="module" src="${pageContext.request.contextPath}/user/scripts/script.js" defer></script>
  </head>
  <body>
    <jsp:include page="/common/components/main-navbar/main-navbar.jsp" />

    <% if (userBean.getRole().equals(UserBean.Roles.ADMIN)) { %>
      <%@ include file="/common/components/edit/edit-product/edit-product.jsp" %>
    <% } else if (userBean.getRole().equals(UserBean.Roles.CUSTOMER)) { %>
      <%@ include file="/common/components/edit/edit-address/edit-address.jsp" %>
      <%@ include file="/common/components/edit/edit-credit-card/edit-credit-card.jsp" %>
    <% } %>

    <div class="main-page-content">
      <div id="info-container">
        <div class="ui-block" id="welcome-message-container">
          <div id="profile-picture-container">
            <div class="profile-picture">
              <img src="${pageContext.request.contextPath}/resources-servlet?resource_type=profile_picture" alt="Foto profilo">
            </div>
          </div>
          <div id="user-message-container">
            <span class="rubik-400" id="user-message">Ciao <%= userBean.getName() %> benvenuto nella tua dashboard! <br></span>
            <% if (userBean.getRole().equals(UserBean.Roles.ADMIN)) { %>
                <button class="material-button dashboard-action-button" onclick="showAddProductForm()">
                  <span class="material-icons-round md-18">library_add</span>
                  <span class="rubik-300">Aggiungi nuovo prodotto</span>
                </button>
            <% } else if (userBean.getRole().equals(UserBean.Roles.CUSTOMER)) { %>
              <button class="material-button dashboard-action-button" onclick="showAddressOverlay()">
                <span class="material-icons-round md-18">add_home_work</span>
                <span class="rubik-300">Aggiungi indirizzo</span>
              </button>
              <button class="material-button dashboard-action-button" onclick="showAddCreditCardOverlay()">
                <span class="material-icons-round md-18">payment</span>
                <span class="rubik-300">Aggiungi carta di credito</span>
              </button>
            <% } %>
          </div>
        </div>
        <div class="ui-block" id="stats-container">
          <div id="stats-title">
            <h5 class="rubik-400">Statistiche</h5>
          </div>
          <div id="stats">
            <% if (userBean.getRole().equals(UserBean.Roles.ADMIN)) { %>
              <div class="stat">
                <span class="material-icons-round md-18">people</span>
                <span class="rubik-300">Utenti: <span class="stat-value rubik-400"><%= userDao.getTotalCustomers() %></span></span>
              </div>
              <div class="stat">
                <span class="material-icons-round md-18">local_shipping</span>
                <span class="rubik-300">Ordini: <span class="stat-value rubik-400"><%= orderDao.getTotalOrders() %></span></span>
              </div>
              <div class="stat">
                <span class="material-icons-round md-18">inventory_2</span>
                <span class="rubik-300">Prodotti: <span class="stat-value rubik-400"><%= productDao.getTotalProducts() %></span></span>
              </div>
          <% } else if (userBean.getRole().equals(UserBean.Roles.CUSTOMER)) { %>
            <div class="stat">
              <span class="material-icons-round md-18">local_shipping</span>
              <span class="rubik-300">Ordini: <span class="stat-value rubik-400"><%= orderDao.getTotalOrders(userBean) %></span></span>
            </div>
          <% } %>
          </div>
        </div>
      </div>
      <div class="ui-block" id="table-container">
        <nav id="main-table-navbar">
          <div id="table-selector-container">
            <label id="table-selector-label" class="rubik-300" for="table-selector">Seleziona tabella:</label>
            <select name="table-selector" id="table-selector" onchange="getTableData()">
              <% if (userBean.getRole().equals(UserBean.Roles.ADMIN)) { %>
              <option value="get_products-immagine,id_prodotto,nome,prezzo,quantità,stagione,acqua_richiesta,tipologia,descrizione">Prodotti</option>
              <option value="get_customers-nome,cognome,email,ordini_effettuati">Utenti</option>
              <option value="get_orders-id_ordine,utente.email,prezzo_totale,data_ordine,data_consegna,carta_di_credito.numero_di_carta,indirizzo.città,indirizzo.provincia,indirizzo.cap,indirizzo.via,indirizzo.telefono,indirizzo.note">Ordini</option>
              <% } else if (userBean.getRole().equals(UserBean.Roles.CUSTOMER)) { %>
              <option value="get_favorites-immagine,nome,prezzo,stagione,tipologia,descrizione">Preferiti</option>
              <option value="get_orders-id_ordine,prezzo_totale,data_ordine,data_consegna,carta_di_credito.numero_di_carta,indirizzo.città,indirizzo.provincia,indirizzo.cap,indirizzo.via,indirizzo.telefono,indirizzo.note">Ordini</option>
              <option value="get_credit_cards-nome,cognome,numero_di_carta,cvv,data_di_scadenza">Carte di Credito</option>
              <option value="get_addresses-via,città,provincia,cap,nome,cognome,telefono,note">Indirizzi</option>
              <% } %>
            </select>
          </div>
          <div id="filter-orders-action-container">
            <div id="orders-start-date-box" class="rubik-300">
              <jsp:include page="/common/components/input-box/input-box.jsp">
                <jsp:param name="label" value="Da" />
                <jsp:param name="id" value="orders-start-date-input-box" />
                <jsp:param name="type" value="date" />
                <jsp:param name="name" value="start_date" />
                <jsp:param name="group" value="table-filter" />
              </jsp:include>
            </div>
            <div id="orders-end-date-box" class="rubik-300">
              <jsp:include page="/common/components/input-box/input-box.jsp">
                <jsp:param name="label" value="A" />
                <jsp:param name="id" value="orders-end-date-input-box" />
                <jsp:param name="type" value="date" />
                <jsp:param name="name" value="end_date" />
                <jsp:param name="group" value="table-filter" />
              </jsp:include>
            </div>
            <button id="filter-orders-button" class="material-button" onclick="getFilteredOrders()">
              <span class="material-icons-round md-18">filter_alt</span>
              <span class="small-text">Filtra</span>
            </button>
            <button id="reset-filter-orders-button" class="material-button" onclick="resetOrdersFilter()">
              <span class="material-icons-round md-18">restart_alt</span>
            </button>
          </div>
        </nav>
        <div id="main-table">
          <div id="table-loading-overlay">
            <img src="${pageContext.request.contextPath}/common/assets/img/loader.svg" alt="Caricamento...">
          </div>
        </div>
      </div>
    </div>

    <jsp:include page="/common/components/main-footer/main-footer.jsp"/>
  </body>
</html>
